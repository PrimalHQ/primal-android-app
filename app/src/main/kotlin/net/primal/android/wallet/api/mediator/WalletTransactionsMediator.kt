package net.primal.android.wallet.api.mediator

import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import java.time.Instant
import kotlinx.coroutines.withContext
import net.primal.android.core.coroutines.CoroutineDispatcherProvider
import net.primal.android.nostr.ext.mapAsWalletTransactionPO
import net.primal.android.user.accounts.UserAccountsStore
import net.primal.android.user.db.UsersDatabase
import net.primal.android.wallet.api.WalletApi
import net.primal.android.wallet.api.model.TransactionsRequestBody
import net.primal.android.wallet.db.WalletTransactionData
import net.primal.android.wallet.domain.SubWallet
import net.primal.core.networking.sockets.errors.WssException
import net.primal.core.utils.CurrencyConversionUtils.formatAsString
import net.primal.core.utils.CurrencyConversionUtils.toBtc
import net.primal.domain.repository.ProfileRepository
import timber.log.Timber

@ExperimentalPagingApi
class WalletTransactionsMediator(
    private val userId: String,
    private val dispatcherProvider: CoroutineDispatcherProvider,
    private val accountsStore: UserAccountsStore,
    private val usersDatabase: UsersDatabase,
    private val walletApi: WalletApi,
    private val profileRepository: ProfileRepository,
) : RemoteMediator<Int, WalletTransactionData>() {

    private val lastRequests: MutableMap<LoadType, TransactionsRequestBody> = mutableMapOf()

    override suspend fun load(loadType: LoadType, state: PagingState<Int, WalletTransactionData>): MediatorResult {
        val walletLightningAddress = accountsStore.findByIdOrNull(userId)?.primalWallet?.lightningAddress
            ?: return MediatorResult.Success(endOfPaginationReached = true)

        val timestamp: Long? = when (loadType) {
            LoadType.REFRESH -> null
            LoadType.PREPEND -> {
                state.firstItemOrNull()?.updatedAt
                    ?: withContext(dispatcherProvider.io()) {
                        usersDatabase.walletTransactions().firstByUserId(userId = userId)?.updatedAt
                    }
                    ?: return MediatorResult.Success(endOfPaginationReached = true)
            }

            LoadType.APPEND -> {
                state.lastItemOrNull()?.updatedAt
                    ?: withContext(dispatcherProvider.io()) {
                        usersDatabase.walletTransactions().lastByUserId(userId = userId)?.updatedAt
                    }
                    ?: return MediatorResult.Success(endOfPaginationReached = true)
            }
        }

        if (timestamp == null && loadType != LoadType.REFRESH) {
            return MediatorResult.Error(IllegalStateException("Remote key not found."))
        }

        val initialRequestBody = TransactionsRequestBody(
            subWallet = SubWallet.Open,
            limit = state.config.pageSize,
            minAmountInBtc = accountsStore.findByIdOrNull(userId)
                ?.primalWalletSettings
                ?.spamThresholdAmountInSats?.toBtc()?.formatAsString(),
        )

        val requestBody = when (loadType) {
            LoadType.REFRESH -> initialRequestBody
            LoadType.PREPEND -> initialRequestBody.copy(since = timestamp, until = Instant.now().epochSecond)
            LoadType.APPEND -> initialRequestBody.copy(until = timestamp)
        }

        if (loadType != LoadType.REFRESH && lastRequests[loadType] == requestBody) {
            return MediatorResult.Success(endOfPaginationReached = true)
        }

        val response = try {
            withContext(dispatcherProvider.io()) {
                walletApi.getTransactions(userId = userId, body = requestBody)
            }
        } catch (error: WssException) {
            Timber.w(error)
            return MediatorResult.Error(error)
        }

        lastRequests[loadType] = requestBody

        val transactions = response.transactions
            .mapAsWalletTransactionPO(walletAddress = walletLightningAddress)

        withContext(dispatcherProvider.io()) {
            usersDatabase.walletTransactions().upsertAll(data = transactions)
        }

        val mentionedUserIds = transactions.mapNotNull { it.otherUserId }.toSet()
        profileRepository.fetchProfiles(profileIds = mentionedUserIds)

        return MediatorResult.Success(endOfPaginationReached = false)
    }
}
