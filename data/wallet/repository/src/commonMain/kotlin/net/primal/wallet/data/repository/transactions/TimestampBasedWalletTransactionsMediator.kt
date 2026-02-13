package net.primal.wallet.data.repository.transactions

import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import io.github.aakira.napier.Napier
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
import net.primal.wallet.data.local.dao.WalletTransactionData
import net.primal.wallet.data.local.dao.WalletTransactionRemoteKey
import net.primal.wallet.data.local.db.WalletDatabase
import net.primal.wallet.data.repository.mappers.local.toDomain

@ExperimentalTime
@ExperimentalPagingApi
class TimestampBasedWalletTransactionsMediator internal constructor(
    private val walletId: String,
    private val dispatcherProvider: DispatcherProvider,
    private val transactionsHandler: TransactionsHandler,
    private val walletDatabase: WalletDatabase,
) : RemoteMediator<Int, WalletTransactionData>() {

    private companion object {
        private const val TAG = "WalletTxMediator"
    }

    private val lastRequests: MutableMap<LoadType, TransactionsRequest> = mutableMapOf()

    override suspend fun initialize(): InitializeAction {
        // Only explicit pagingItems.refresh() (pull-to-refresh) triggers REFRESH.
        // Automatic loads use APPEND, including the initial load from empty DB.
        return InitializeAction.SKIP_INITIAL_REFRESH
    }

    override suspend fun load(loadType: LoadType, state: PagingState<Int, WalletTransactionData>): MediatorResult {
        Napier.d(tag = TAG) { "load($loadType) called" }

        if (loadType == LoadType.PREPEND) {
            return MediatorResult.Success(endOfPaginationReached = true)
        }

        if (loadType == LoadType.REFRESH) {
            lastRequests.clear()
        }

        // For APPEND, look up the cursor from DB-backed remote keys.
        // If no remote key found (empty DB or initial load), fetch from the beginning.
        val remoteKey = if (loadType == LoadType.APPEND) {
            findLastRemoteKey(state)
        } else {
            null
        }

        val wallet = withContext(dispatcherProvider.io()) {
            walletDatabase.wallet().findWallet(walletId = walletId)
        } ?: return MediatorResult.Success(endOfPaginationReached = true)

        val walletSettings = withContext(dispatcherProvider.io()) {
            walletDatabase.walletSettings().findWalletSettings(walletId = wallet.info.walletId)
        }

        val request = TransactionsRequest(
            limit = state.config.pageSize,
            since = null,
            until = remoteKey?.sinceId,
            minAmountInBtc = if (wallet.info.type == WalletType.PRIMAL) {
                walletSettings?.spamThresholdAmountInSats?.decrypted?.toBtc()?.formatAsString()
            } else {
                null
            },
        )

        if (lastRequests[loadType] == request) {
            Napier.d(tag = TAG) { "Repeat request for $loadType. End of pagination." }
            return MediatorResult.Success(endOfPaginationReached = true)
        }

        return transactionsHandler.fetchAndPersistLatestTransactions(
            wallet = wallet.toDomain(),
            request = request,
            clearWalletId = if (loadType == LoadType.REFRESH) wallet.info.walletId else null,
        ).onSuccess { result ->
            Napier.d(tag = TAG) {
                "$loadType fetched ${result.transactionsCount} txs, nextCursor=${result.nextCursor}"
            }
            lastRequests[loadType] = request
        }.onFailure { error ->
            Napier.w(tag = TAG, throwable = error) { "$loadType failed." }
        }.fold(
            onSuccess = { result ->
                MediatorResult.Success(
                    endOfPaginationReached = result.transactionsCount == 0 || result.nextCursor == null,
                )
            },
            onFailure = { MediatorResult.Error(it) },
        )
    }

    /**
     * Find the remote key for the oldest loaded item (APPEND cursor).
     * PagingSource sorts by updatedAt DESC, so lastItemOrNull() is the oldest visible item.
     * Fallback: firstByWalletId (oldest in DB by updatedAt ASC) for when PagingState is empty.
     */
    private suspend fun findLastRemoteKey(
        state: PagingState<Int, WalletTransactionData>,
    ): WalletTransactionRemoteKey? {
        val lastTransactionId = state.lastItemOrNull()?.transactionId
            ?: withContext(dispatcherProvider.io()) {
                walletDatabase.walletTransactions().firstByWalletId(walletId = walletId)?.transactionId
            }
            ?: return null

        return withContext(dispatcherProvider.io()) {
            walletDatabase.walletTransactionRemoteKeys().find(
                walletId = walletId,
                transactionId = lastTransactionId,
            )
        }
    }
}
