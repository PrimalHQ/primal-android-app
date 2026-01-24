package net.primal.wallet.data.repository.transactions

import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import io.github.aakira.napier.Napier
import net.primal.core.utils.coroutines.DispatcherProvider
import net.primal.core.utils.fold
import net.primal.core.utils.onFailure
import net.primal.core.utils.onSuccess
import net.primal.domain.wallet.TransactionsRequest
import net.primal.wallet.data.handler.TransactionsHandler
import net.primal.wallet.data.local.dao.WalletTransactionData
import net.primal.wallet.data.local.db.WalletDatabase
import net.primal.wallet.data.repository.mappers.local.toDomain

@ExperimentalPagingApi
class OffsetBasedWalletTransactionsMediator internal constructor(
    private val walletId: String,
    private val dispatcherProvider: DispatcherProvider,
    private val transactionsHandler: TransactionsHandler,
    private val walletDatabase: WalletDatabase,
) : RemoteMediator<Int, WalletTransactionData>() {

    private val lastRequests: MutableMap<LoadType, TransactionsRequest> = mutableMapOf()

    override suspend fun load(loadType: LoadType, state: PagingState<Int, WalletTransactionData>): MediatorResult {
        val wallet = walletDatabase.wallet().findWallet(walletId = walletId)
            ?: run {
                Napier.d { "No wallet found. Exiting." }
                return MediatorResult.Success(endOfPaginationReached = true)
            }

        val request = TransactionsRequest(
            limit = 100,
            offset = 0,
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
            onSuccess = { MediatorResult.Success(endOfPaginationReached = true) },
            onFailure = { MediatorResult.Error(it) },
        )
    }
}
