package net.primal.wallet.data.repository.transactions

import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import io.github.aakira.napier.Napier
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import kotlinx.coroutines.withContext
import net.primal.core.utils.CurrencyConversionUtils.formatAsString
import net.primal.core.utils.CurrencyConversionUtils.toBtc
import net.primal.core.utils.coroutines.DispatcherProvider
import net.primal.core.utils.fold
import net.primal.core.utils.onFailure
import net.primal.core.utils.onSuccess
import net.primal.domain.wallet.TransactionsRequest
import net.primal.domain.wallet.WalletType
import net.primal.wallet.data.handler.TransactionsHandler
import net.primal.wallet.data.local.dao.WalletTransaction
import net.primal.wallet.data.local.db.WalletDatabase
import net.primal.wallet.data.repository.mappers.local.toDomain

@ExperimentalTime
@ExperimentalPagingApi
class TimestampBasedWalletTransactionsMediator internal constructor(
    private val walletId: String,
    private val dispatcherProvider: DispatcherProvider,
    private val transactionsHandler: TransactionsHandler,
    private val walletDatabase: WalletDatabase,
) : RemoteMediator<Int, WalletTransaction>() {

    private val lastRequests: MutableMap<LoadType, TransactionsRequest> = mutableMapOf()

    override suspend fun load(loadType: LoadType, state: PagingState<Int, WalletTransaction>): MediatorResult {
        val wallet = walletDatabase.wallet().findWallet(walletId = walletId)
            ?: run {
                Napier.d { "No wallet found. Exiting." }
                return MediatorResult.Success(endOfPaginationReached = true)
            }

        val walletSettings = walletDatabase.walletSettings().findWalletSettings(walletId = wallet.info.walletId)

        val timestamp: Long? = when (loadType) {
            LoadType.REFRESH -> null
            LoadType.PREPEND -> {
                state.firstItemOrNull()?.info?.updatedAt
                    ?: withContext(dispatcherProvider.io()) {
                        walletDatabase.walletTransactions().firstByWalletId(walletId = wallet.info.walletId)?.updatedAt
                    }
                    ?: run {
                        Napier.d { "Couldn't find first tx. Exiting." }
                        return MediatorResult.Success(endOfPaginationReached = true)
                    }
            }

            LoadType.APPEND -> {
                state.lastItemOrNull()?.info?.updatedAt
                    ?: withContext(dispatcherProvider.io()) {
                        walletDatabase.walletTransactions().lastByWalletId(walletId = wallet.info.walletId)?.updatedAt
                    }
                    ?: run {
                        Napier.d { "Couldn't find last tx. Exiting." }
                        return MediatorResult.Success(endOfPaginationReached = true)
                    }
            }
        }

        if (timestamp == null && loadType != LoadType.REFRESH) {
            Napier.d { "Remote key not found. Exiting with Error." }
            return MediatorResult.Error(IllegalStateException("Remote key not found."))
        }

        val timestamps = when (loadType) {
            LoadType.REFRESH -> null to null
            LoadType.PREPEND -> timestamp to Clock.System.now().epochSeconds
            LoadType.APPEND -> null to timestamp
        }

        val request = TransactionsRequest(
            limit = state.config.pageSize,
            since = timestamps.first,
            until = timestamps.second,
            minAmountInBtc = if (wallet.info.type == WalletType.PRIMAL) {
                walletSettings?.spamThresholdAmountInSats?.decrypted?.toBtc()?.formatAsString()
            } else {
                null
            },
        )

        Napier.d { "Fetching $loadType with request: $request" }
        if (loadType != LoadType.REFRESH && lastRequests[loadType] == request) {
            Napier.d { "Repeat request detected. Reached end. Exiting." }
            return MediatorResult.Success(endOfPaginationReached = true)
        }

        return transactionsHandler.fetchAndPersistLatestTransactions(
            wallet = wallet.toDomain(),
            request = request,
        ).onSuccess {
            Napier.d { "Transactions fetched and persisted. Continuing." }
            lastRequests[loadType] = request
        }.onFailure { error ->
            Napier.d(throwable = error) { "Error occurred while fetching transactions. Exiting with Error." }
        }.fold(
            onSuccess = { MediatorResult.Success(endOfPaginationReached = false) },
            onFailure = { MediatorResult.Error(it) },
        )
    }
}
