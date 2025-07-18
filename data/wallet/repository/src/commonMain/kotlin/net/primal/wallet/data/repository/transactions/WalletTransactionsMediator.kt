package net.primal.wallet.data.repository.transactions

import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import kotlinx.coroutines.withContext
import kotlinx.datetime.Clock
import net.primal.core.utils.CurrencyConversionUtils.formatAsString
import net.primal.core.utils.CurrencyConversionUtils.toBtc
import net.primal.core.utils.coroutines.DispatcherProvider
import net.primal.core.utils.fold
import net.primal.core.utils.onSuccess
import net.primal.domain.wallet.SubWallet
import net.primal.domain.wallet.WalletType
import net.primal.domain.wallet.model.TransactionsRequest
import net.primal.wallet.data.handler.TransactionsHandler
import net.primal.wallet.data.local.dao.WalletTransaction
import net.primal.wallet.data.local.db.WalletDatabase
import net.primal.wallet.data.repository.mappers.local.toDomain

@ExperimentalPagingApi
class WalletTransactionsMediator(
    private val walletId: String,
    private val dispatcherProvider: DispatcherProvider,
    private val transactionsHandler: TransactionsHandler,
    private val walletDatabase: WalletDatabase,
) : RemoteMediator<Int, WalletTransaction>() {

    private val lastRequests: MutableMap<LoadType, TransactionsRequest> = mutableMapOf()

    override suspend fun load(loadType: LoadType, state: PagingState<Int, WalletTransaction>): MediatorResult {
        val wallet = walletDatabase.wallet().findWallet(walletId = walletId)
            ?: return MediatorResult.Success(endOfPaginationReached = true)

        val timestamp: Long? = when (loadType) {
            LoadType.REFRESH -> null
            LoadType.PREPEND -> {
                state.firstItemOrNull()?.info?.createdAt
                    ?: withContext(dispatcherProvider.io()) {
                        walletDatabase.walletTransactions().firstByWalletId(walletId = wallet.info.walletId)?.createdAt
                    }
                    ?: return MediatorResult.Success(endOfPaginationReached = true)
            }

            LoadType.APPEND -> {
                state.lastItemOrNull()?.info?.createdAt
                    ?: withContext(dispatcherProvider.io()) {
                        walletDatabase.walletTransactions().lastByWalletId(walletId = wallet.info.walletId)?.createdAt
                    }
                    ?: return MediatorResult.Success(endOfPaginationReached = true)
            }
        }

        if (timestamp == null && loadType != LoadType.REFRESH) {
            return MediatorResult.Error(IllegalStateException("Remote key not found."))
        }

        val walletSettings = walletDatabase.walletSettings().findWalletSettings(walletId = wallet.info.walletId)

        val timestamps = when (loadType) {
            LoadType.REFRESH -> null to null
            LoadType.PREPEND -> timestamp to Clock.System.now().epochSeconds
            LoadType.APPEND -> null to timestamp
        }

        val request = when (wallet.info.type) {
            WalletType.PRIMAL -> TransactionsRequest.Primal(
                subWallet = SubWallet.Open,
                minAmountInBtc = walletSettings?.spamThresholdAmountInSats?.toBtc()?.formatAsString(),
                limit = state.config.pageSize,
                since = timestamps.first,
                until = timestamps.second,
            )

            WalletType.NWC -> TransactionsRequest.NWC(
                limit = state.config.pageSize,
                since = timestamps.first,
                until = timestamps.second,
            )
        }

        if (loadType != LoadType.REFRESH && lastRequests[loadType] == request) {
            return MediatorResult.Success(endOfPaginationReached = true)
        }

        return transactionsHandler.fetchAndPersistLatestTransactions(
            wallet = wallet.toDomain(),
            request = request,
        ).onSuccess {
            lastRequests[loadType] = request
        }.fold(
            onSuccess = { MediatorResult.Success(endOfPaginationReached = false) },
            onFailure = { MediatorResult.Error(it) },
        )
    }
}
