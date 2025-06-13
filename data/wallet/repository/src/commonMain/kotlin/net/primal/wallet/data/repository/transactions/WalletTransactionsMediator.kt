package net.primal.wallet.data.repository.transactions

import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import io.github.aakira.napier.Napier
import kotlinx.coroutines.withContext
import kotlinx.datetime.Clock
import net.primal.core.utils.CurrencyConversionUtils.formatAsString
import net.primal.core.utils.CurrencyConversionUtils.toBtc
import net.primal.core.utils.coroutines.DispatcherProvider
import net.primal.domain.common.exception.NetworkException
import net.primal.domain.profile.ProfileRepository
import net.primal.domain.wallet.SubWallet
import net.primal.domain.wallet.WalletType
import net.primal.wallet.data.local.dao.WalletTransactionData
import net.primal.wallet.data.local.db.WalletDatabase
import net.primal.wallet.data.remote.api.PrimalWalletApi
import net.primal.wallet.data.remote.model.TransactionsRequestBody
import net.primal.wallet.data.repository.mappers.remote.mapAsWalletTransactionPO

@ExperimentalPagingApi
class WalletTransactionsMediator(
    private val userId: String,
    private val dispatcherProvider: DispatcherProvider,
    private val walletDatabase: WalletDatabase,
    private val primalWalletApi: PrimalWalletApi,
    private val profileRepository: ProfileRepository,
) : RemoteMediator<Int, WalletTransactionData>() {

    private val lastRequests: MutableMap<LoadType, TransactionsRequestBody> = mutableMapOf()

    override suspend fun load(loadType: LoadType, state: PagingState<Int, WalletTransactionData>): MediatorResult {
        val primalWallet = walletDatabase.wallet().findWalletInfo(userId = userId, type = WalletType.PRIMAL)
        val walletLightningAddress = primalWallet?.lightningAddress
            ?: return MediatorResult.Success(endOfPaginationReached = true)

        val timestamp: Long? = when (loadType) {
            LoadType.REFRESH -> null
            LoadType.PREPEND -> {
                state.firstItemOrNull()?.updatedAt
                    ?: withContext(dispatcherProvider.io()) {
                        walletDatabase.walletTransactions().firstByUserId(userId = userId)?.updatedAt
                    }
                    ?: return MediatorResult.Success(endOfPaginationReached = true)
            }

            LoadType.APPEND -> {
                state.lastItemOrNull()?.updatedAt
                    ?: withContext(dispatcherProvider.io()) {
                        walletDatabase.walletTransactions().lastByUserId(userId = userId)?.updatedAt
                    }
                    ?: return MediatorResult.Success(endOfPaginationReached = true)
            }
        }

        if (timestamp == null && loadType != LoadType.REFRESH) {
            return MediatorResult.Error(IllegalStateException("Remote key not found."))
        }

        val walletSettings = walletDatabase.walletSettings().findWalletSettings(walletId = primalWallet.localId)
        val initialRequestBody = TransactionsRequestBody(
            subWallet = SubWallet.Open,
            limit = state.config.pageSize,
            minAmountInBtc = walletSettings?.spamThresholdAmountInSats?.toBtc()?.formatAsString(),
        )

        val requestBody = when (loadType) {
            LoadType.REFRESH -> initialRequestBody
            LoadType.PREPEND -> initialRequestBody.copy(since = timestamp, until = Clock.System.now().epochSeconds)
            LoadType.APPEND -> initialRequestBody.copy(until = timestamp)
        }

        if (loadType != LoadType.REFRESH && lastRequests[loadType] == requestBody) {
            return MediatorResult.Success(endOfPaginationReached = true)
        }

        val response = try {
            withContext(dispatcherProvider.io()) {
                primalWalletApi.getTransactions(userId = userId, body = requestBody)
            }
        } catch (error: NetworkException) {
            Napier.w(error) { "Failed to fetch transactions" }
            return MediatorResult.Error(error)
        }

        lastRequests[loadType] = requestBody

        val transactions = response.transactions
            .mapAsWalletTransactionPO(walletAddress = walletLightningAddress)

        withContext(dispatcherProvider.io()) {
            walletDatabase.walletTransactions().upsertAll(data = transactions)
        }

        val mentionedUserIds = transactions.mapNotNull { it.otherUserId }
        if (mentionedUserIds.isNotEmpty()) {
            runCatching {
                profileRepository.fetchProfiles(profileIds = mentionedUserIds)
            }
        }

        return MediatorResult.Success(endOfPaginationReached = false)
    }
}
