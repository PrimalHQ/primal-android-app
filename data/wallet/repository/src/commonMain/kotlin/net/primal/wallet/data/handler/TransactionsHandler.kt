package net.primal.wallet.data.handler

import io.github.aakira.napier.Napier
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
import net.primal.wallet.data.local.db.WalletDatabase
import net.primal.wallet.data.repository.mappers.local.extractPaymentHash
import net.primal.wallet.data.repository.mappers.local.extractPreimage
import net.primal.wallet.data.repository.mappers.local.toWalletTransactionData
import net.primal.wallet.data.service.factory.WalletServiceFactory

internal class TransactionsHandler(
    val dispatchers: DispatcherProvider,
    val walletServiceFactory: WalletServiceFactory,
    val walletDatabase: WalletDatabase,
    val profileRepository: ProfileRepository,
) {
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
                walletDatabase.walletTransactions().upsertAll(
                    data = transactions.map { it.toWalletTransactionData() },
                )

                // Update NwcInvoice state for settled incoming transactions
                val settledTransactions = transactions.filter { tx ->
                    tx.state == TxState.SUCCEEDED &&
                        tx.type == TxType.DEPOSIT &&
                        tx.extractPaymentHash() != null
                }

                for (tx in settledTransactions) {
                    val paymentHash = tx.extractPaymentHash() ?: continue
                    val settledAt = tx.completedAt ?: kotlin.time.Clock.System.now().epochSeconds
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

            if (otherUserIds.isNotEmpty()) {
                profileRepository.fetchProfiles(profileIds = otherUserIds)
            }
        }

    companion object {
        private const val TAG = "TransactionsHandler"
    }
}
