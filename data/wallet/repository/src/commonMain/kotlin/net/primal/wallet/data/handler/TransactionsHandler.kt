package net.primal.wallet.data.handler

import io.github.aakira.napier.Napier
import kotlin.coroutines.cancellation.CancellationException
import kotlin.time.Clock
import kotlinx.coroutines.withContext
import net.primal.core.lightning.LightningPayHelper
import net.primal.core.utils.Result
import net.primal.core.utils.coroutines.DispatcherProvider
import net.primal.core.utils.runCatching
import net.primal.domain.nostr.utils.parseAsLNUrlOrNull
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
    val lightningPayHelper: LightningPayHelper,
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

            val enrichedTransactions = resolveOtherUserIds(transactions)
            val otherUserIds = enrichedTransactions.mapNotNull { tx ->
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
                        data = enrichedTransactions.map { it.toWalletTransactionData() },
                    )

                    // Update NwcInvoice state for settled incoming transactions
                    val settledTransactions = enrichedTransactions.filter { tx ->
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
                    val untilId = enrichedTransactions.maxOfOrNull { it.createdAt }
                    if (sinceId != null && untilId != null) {
                        val remoteKeys = enrichedTransactions.map { tx ->
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
                transactionsCount = enrichedTransactions.size,
            )
        }

    private suspend fun resolveOtherUserIds(transactions: List<Transaction>): List<Transaction> {
        val unresolvedAddresses = transactions
            .filterIsInstance<Transaction.Lightning>()
            .filter { it.otherUserId == null && it.otherLightningAddress != null }
            .map { it.otherLightningAddress!! }
            .distinct()

        if (unresolvedAddresses.isEmpty()) return transactions

        val addressToPubkey = mutableMapOf<String, String>()

        // 1. Local DB lookup (instant)
        for (address in unresolvedAddresses) {
            val profile = profileRepository.findProfileDataByLightningAddress(address)
            if (profile != null) {
                addressToPubkey[address] = profile.profileId
            }
        }

        // 2. LNURL resolution for still-unresolved addresses
        val stillUnresolved = unresolvedAddresses.filter { it !in addressToPubkey }
        for (address in stillUnresolved) {
            val lnUrl = address.parseAsLNUrlOrNull() ?: continue
            val pubkey = try {
                lightningPayHelper.fetchPayRequest(lnUrl).nostrPubkey
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Napier.w(throwable = e) { "Failed to resolve nostrPubkey for $address" }
                null
            }
            if (pubkey != null) {
                addressToPubkey[address] = pubkey
            }
        }

        if (addressToPubkey.isEmpty()) return transactions

        return transactions.map { tx ->
            if (tx is Transaction.Lightning && tx.otherUserId == null && tx.otherLightningAddress != null) {
                val resolvedPubkey = addressToPubkey[tx.otherLightningAddress]
                if (resolvedPubkey != null) tx.copy(otherUserId = resolvedPubkey) else tx
            } else {
                tx
            }
        }
    }

    companion object {
        private const val TAG = "TransactionsHandler"
    }
}
