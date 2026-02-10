package net.primal.wallet.data.handler

import io.github.aakira.napier.Napier
import kotlin.time.Clock
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.withContext
import net.primal.core.utils.Result
import net.primal.core.utils.coroutines.DispatcherProvider
import net.primal.core.utils.onFailure
import net.primal.core.utils.runCatching
import net.primal.domain.profile.ProfileRepository
import net.primal.domain.transactions.Transaction
import net.primal.domain.wallet.TransactionsRequest
import net.primal.domain.wallet.TxState
import net.primal.domain.wallet.TxType
import net.primal.domain.wallet.Wallet
import net.primal.shared.data.local.db.withTransaction
import net.primal.wallet.data.local.dao.ReceiveRequestType
import net.primal.wallet.data.local.db.WalletDatabase
import net.primal.wallet.data.repository.mappers.local.extractPaymentHash
import net.primal.wallet.data.repository.mappers.local.extractPreimage
import net.primal.wallet.data.repository.mappers.local.toWalletTransactionData
import net.primal.wallet.data.service.MempoolApiService
import net.primal.wallet.data.service.factory.WalletServiceFactory
import net.primal.wallet.data.service.outputAddresses

internal class TransactionsHandler(
    val dispatchers: DispatcherProvider,
    val walletServiceFactory: WalletServiceFactory,
    val walletDatabase: WalletDatabase,
    val profileRepository: ProfileRepository,
) {

    private val mempoolApiService: MempoolApiService = MempoolApiService()
    private val backgroundScope = CoroutineScope(dispatchers.io() + SupervisorJob())

    suspend fun fetchAndPersistLatestTransactions(wallet: Wallet, request: TransactionsRequest): Result<Unit> =
        runCatching {
            val transactions = withContext(dispatchers.io()) {
                val service = walletServiceFactory.getServiceForWallet(wallet)
                service.fetchTransactions(wallet = wallet, request = request).getOrThrow()
            }

            val otherUserIds = transactions.mapNotNull { tx ->
                when (tx) {
                    is Transaction.Lightning -> tx.otherUserId
                    is Transaction.Zap -> tx.otherUserId
                    else -> null
                }
            }

            withContext(dispatchers.io()) {
                walletDatabase.withTransaction {
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

                        Napier.d(tag = TAG) {
                            "Marking NwcInvoice as settled: paymentHash=$paymentHash, settledAt=$settledAt"
                        }
                        walletDatabase.nwcInvoices().markSettledWithDetails(
                            paymentHash = paymentHash,
                            settledAt = settledAt,
                            preimage = preimage,
                        )
                    }
                }
            }

            //  With a static address, historical deposits match unfulfilled requests
            //  causing premature fulfillment. Once addresses rotate, each address maps
            //  to exactly one deposit and this logic works correctly.
//             backgroundScope.launch {
//                 withTimeout(MARK_FULFILLED_TIMEOUT) {
//                     markFulfilledAddresses(wallet, transactions)
//                 }
//             }

            if (otherUserIds.isNotEmpty()) {
                profileRepository.fetchProfiles(profileIds = otherUserIds)
            }
        }

    private suspend fun markFulfilledAddresses(wallet: Wallet, transactions: List<Transaction>) {
        val onChainDepositTxIds = transactions
            .filterIsInstance<Transaction.OnChain>()
            .filter { it.type == TxType.DEPOSIT }
            .map { it.transactionId }

        if (onChainDepositTxIds.isEmpty()) return

        val unfulfilledRequests = walletDatabase.receiveRequests().findUnfulfilled(
            walletId = wallet.walletId,
            type = ReceiveRequestType.ON_CHAIN,
            createdAfter = 0,
            limit = MAX_UNFULFILLED_TO_CHECK,
        )

        if (unfulfilledRequests.isEmpty()) return

        val unfulfilledAddresses = unfulfilledRequests.associate { it.payload to it.id }

        val now = Clock.System.now().epochSeconds

        for (txId in onChainDepositTxIds) {
            runCatching {
                val mempoolTx = mempoolApiService.getTransaction(txId) ?: return@runCatching
                val matchingAddress = mempoolTx.outputAddresses().firstOrNull { it in unfulfilledAddresses }
                if (matchingAddress != null) {
                    val requestId = unfulfilledAddresses[matchingAddress] ?: return@runCatching
                    Napier.i(tag = TAG) {
                        "Marking address as fulfilled from confirmed deposit: txid=$txId, address=$matchingAddress"
                    }
                    walletDatabase.receiveRequests().markFulfilled(id = requestId, fulfilledAt = now)
                }
            }.onFailure { error ->
                Napier.w(tag = TAG) { "Failed to look up tx $txId for address fulfillment: ${error.message}" }
            }
        }
    }

    companion object {
        private const val TAG = "TransactionsHandler"
        private const val MAX_UNFULFILLED_TO_CHECK = 10
//         private val MARK_FULFILLED_TIMEOUT = 30.seconds
    }
}
