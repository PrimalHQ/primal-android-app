package net.primal.wallet.data.handler

import kotlin.time.Clock
import kotlinx.coroutines.withContext
import net.primal.core.utils.Result
import net.primal.core.utils.coroutines.DispatcherProvider
import net.primal.core.utils.runCatching
import net.primal.domain.profile.ProfileRepository
import net.primal.domain.transactions.Transaction
import net.primal.domain.wallet.TransactionsRequest
import net.primal.domain.wallet.TxState
import net.primal.domain.wallet.TxType
import net.primal.domain.wallet.Wallet
import net.primal.shared.data.local.db.withTransaction
import net.primal.wallet.data.local.dao.WalletTransactionRemoteKey
import net.primal.wallet.data.local.db.WalletDatabase
import net.primal.wallet.data.repository.mappers.local.extractPaymentHash
import net.primal.wallet.data.repository.mappers.local.extractPreimage
import net.primal.wallet.data.repository.mappers.local.toWalletTransactionData
import net.primal.wallet.data.service.factory.WalletServiceFactory

data class TransactionsFetchResult(
    val nextCursor: Long?,
    val transactionsCount: Int,
)

internal class TransactionsHandler(
    val dispatchers: DispatcherProvider,
    val walletServiceFactory: WalletServiceFactory,
    val walletDatabase: WalletDatabase,
    val profileRepository: ProfileRepository,
) {
    suspend fun fetchAndPersistLatestTransactions(
        wallet: Wallet,
        request: TransactionsRequest,
        clearWalletId: String? = null,
    ): Result<TransactionsFetchResult> =
        runCatching {
            val page = withContext(dispatchers.io()) {
                val service = walletServiceFactory.getServiceForWallet(wallet)
                service.fetchTransactions(wallet = wallet, request = request).getOrThrow()
            }

            val transactions = page.transactions

            val otherUserIds = transactions.mapNotNull { tx ->
                when (tx) {
                    is Transaction.Lightning -> tx.otherUserId
                    is Transaction.Zap -> tx.otherUserId
                    else -> null
                }
            }

            // Fetch profiles BEFORE writing transactions to DB.
            // The DB write triggers Room's InvalidationTracker which invalidates the
            // PagingSource. The mediator must return immediately after the write so
            // Paging can process the pending APPEND boundary. Any delay between the
            // write and the mediator return creates a race where APPEND is never re-triggered.
            if (otherUserIds.isNotEmpty()) {
                withContext(dispatchers.io()) {
                    profileRepository.fetchProfiles(profileIds = otherUserIds)
                }
            }

            withContext(dispatchers.io()) {
                walletDatabase.withTransaction {
                    if (clearWalletId != null) {
                        walletDatabase.walletTransactions().deleteByWalletId(walletId = clearWalletId)
                        walletDatabase.walletTransactionRemoteKeys().deleteByWalletId(walletId = clearWalletId)
                    }

                    walletDatabase.walletTransactions().upsertAll(
                        data = transactions.map { it.toWalletTransactionData() },
                    )

                    // Update NwcInvoice state for settled incoming transactions
                    val settledTransactions = transactions.filter { tx ->
                        tx.state == TxState.SUCCEEDED && tx.type == TxType.DEPOSIT
                    }

                    for (tx in settledTransactions) {
                        val paymentHash = tx.extractPaymentHash() ?: continue
                        val settledAt = tx.completedAt ?: tx.updatedAt
                        val preimage = tx.extractPreimage()

                        walletDatabase.nwcInvoices().markSettledWithDetails(
                            paymentHash = paymentHash,
                            settledAt = settledAt,
                            preimage = preimage,
                        )
                    }

                    // Persist remote keys for pagination
                    val sinceId = page.nextCursor
                    val untilId = transactions.maxOfOrNull { it.createdAt }
                    if (sinceId != null && untilId != null) {
                        val remoteKeys = transactions.map { tx ->
                            WalletTransactionRemoteKey(
                                walletId = wallet.walletId,
                                transactionId = tx.transactionId,
                                sinceId = sinceId,
                                untilId = untilId,
                                cachedAt = Clock.System.now().epochSeconds,
                            )
                        }
                        walletDatabase.walletTransactionRemoteKeys().upsert(remoteKeys)
                    }
                }
            }

            TransactionsFetchResult(
                nextCursor = page.nextCursor,
                transactionsCount = transactions.size,
            )
        }

    companion object {
        private const val TAG = "TransactionsHandler"
    }
}
