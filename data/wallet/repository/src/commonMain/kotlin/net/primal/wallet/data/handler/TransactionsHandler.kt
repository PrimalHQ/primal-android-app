package net.primal.wallet.data.handler

import io.github.aakira.napier.Napier
import kotlinx.coroutines.withContext
import kotlinx.datetime.Clock
import net.primal.core.utils.Result
import net.primal.core.utils.coroutines.DispatcherProvider
import net.primal.core.utils.runCatching
import net.primal.domain.profile.ProfileRepository
import net.primal.domain.wallet.Wallet
import net.primal.shared.data.local.db.withTransaction
import net.primal.wallet.data.local.dao.WalletTransactionCrossRef
import net.primal.wallet.data.local.dao.WalletTransactionRemoteKey
import net.primal.wallet.data.local.db.WalletDatabase
import net.primal.wallet.data.model.Transaction
import net.primal.wallet.data.model.TransactionsRequest
import net.primal.wallet.data.repository.mappers.local.toNostrTransactionData
import net.primal.wallet.data.repository.mappers.local.toPrimalTransactionData
import net.primal.wallet.data.repository.mappers.local.toWalletTransactionData
import net.primal.wallet.data.service.WalletService

internal class TransactionsHandler(
    val dispatchers: DispatcherProvider,
    val primalWalletService: WalletService,
    val nostrWalletService: WalletService,
    val walletDatabase: WalletDatabase,
    val profileRepository: ProfileRepository,
) {
    suspend fun fetchAndPersistLatestTransactions(wallet: Wallet, request: TransactionsRequest): Result<Unit> =
        runCatching {
            val transactions = when (wallet) {
                is Wallet.NWC -> nostrWalletService.fetchTransactions(wallet = wallet, request = request)
                is Wallet.Primal -> primalWalletService.fetchTransactions(wallet = wallet, request = request)
            }.getOrThrow()

            Napier.d(tag = "paging") { "Got ${transactions.size} transactions." }
            val otherUserIds = transactions.filterIsInstance<Transaction.Primal>()
                .mapNotNull { it.otherUserId }

            persistTransactions(transactions = transactions)
            persistConnections(transactions = transactions)

            if (otherUserIds.isNotEmpty()) {
                profileRepository.fetchProfiles(profileIds = otherUserIds)
            }
        }

    private suspend fun persistTransactions(transactions: List<Transaction>) =
        withContext(dispatchers.io()) {
            walletDatabase.withTransaction {
                walletDatabase.walletTransactions().upsertAll(
                    data = transactions.map { it.toWalletTransactionData() },
                )

                walletDatabase.walletTransactions().upsertAllPrimalTransactions(
                    data = transactions.filterIsInstance<Transaction.Primal>()
                        .map { it.toPrimalTransactionData() },
                )

                walletDatabase.walletTransactions().upsertAllNostrTransactions(
                    data = transactions.filterIsInstance<Transaction.NWC>()
                        .map { it.toNostrTransactionData() },
                )
            }
        }

    private suspend fun persistConnections(transactions: List<Transaction>) =
        withContext(dispatchers.io()) {
            walletDatabase.withTransaction {
                val sinceId = transactions.minOf { it.updatedAt }
                val untilId = transactions.maxOf { it.updatedAt }
                walletDatabase.walletConnections().upsertRemoteKeys(
                    data = transactions.map {
                        WalletTransactionRemoteKey(
                            transactionId = it.transactionId,
                            walletId = it.walletId,
                            cachedAt = Clock.System.now().epochSeconds,
                            sinceId = sinceId,
                            untilId = untilId,
                        )
                    },
                )

                walletDatabase.walletConnections().upsertCrossRefs(
                    data = transactions.map {
                        WalletTransactionCrossRef(
                            walletId = it.walletId,
                            transactionId = it.transactionId,
                        )
                    },
                )
            }
        }
}
