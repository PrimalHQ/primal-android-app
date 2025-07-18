package net.primal.wallet.data.handler

import kotlinx.coroutines.withContext
import net.primal.core.utils.Result
import net.primal.core.utils.coroutines.DispatcherProvider
import net.primal.core.utils.runCatching
import net.primal.domain.profile.ProfileRepository
import net.primal.domain.wallet.Wallet
import net.primal.shared.data.local.db.withTransaction
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

            val otherUserIds = transactions.filterIsInstance<Transaction.Primal>()
                .mapNotNull { it.otherUserId }

            persistTransactions(transactions = transactions)

            profileRepository.fetchProfiles(profileIds = otherUserIds)
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
}
