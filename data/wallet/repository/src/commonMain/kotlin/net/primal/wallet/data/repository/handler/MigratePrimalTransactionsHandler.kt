package net.primal.wallet.data.repository.handler

import io.github.aakira.napier.Napier
import kotlinx.coroutines.withContext
import net.primal.core.utils.coroutines.DispatcherProvider
import net.primal.domain.profile.ProfileRepository
import net.primal.domain.wallet.SubWallet
import net.primal.wallet.data.local.db.WalletDatabase
import net.primal.wallet.data.remote.api.PrimalWalletApi
import net.primal.wallet.data.remote.model.TransactionsRequestBody
import net.primal.wallet.data.repository.mappers.local.toWalletTransactionData
import net.primal.wallet.data.repository.mappers.remote.mapForMigration

class MigratePrimalTransactionsHandler(
    private val dispatcherProvider: DispatcherProvider,
    private val walletDatabase: WalletDatabase,
    private val primalWalletApi: PrimalWalletApi,
    private val profileRepository: ProfileRepository? = null,
) {

    private companion object {
        private const val DEFAULT_PAGE_SIZE = 50
    }

    /**
     * Migrates Primal wallet transactions to Spark wallet.
     *
     * @param userId The user ID
     * @param targetWalletId The Spark wallet ID to migrate transactions to
     * @param maxPages Maximum pages to fetch (null = fetch all, for background mode)
     * @param onPageFetched Callback invoked after each page is fetched
     *
     * - Starts from most recent transactions, syncs towards the past
     * - Resumable: saves progress after each page
     * - If interrupted, will continue from last saved position
     */
    suspend fun invoke(
        userId: String,
        targetWalletId: String,
        maxPages: Int? = null,
        onPageFetched: ((Int) -> Unit)? = null,
    ): Result<Unit> =
        withContext(dispatcherProvider.io()) {
            runCatching {
                Napier.d { "Starting Primal transaction migration for user $userId to wallet $targetWalletId" }

                // Get resume point (null = start from most recent)
                var until: Long? = walletDatabase.wallet()
                    .findSparkWalletData(targetWalletId)
                    ?.primalTxsMigratedUntil

                if (until != null) {
                    Napier.d { "Resuming migration from timestamp $until" }
                }

                var pageCount = 0

                do {
                    val previousUntil = until

                    val response = primalWalletApi.getTransactions(
                        userId = userId,
                        body = TransactionsRequestBody(
                            subWallet = SubWallet.Open,
                            limit = DEFAULT_PAGE_SIZE,
                            until = until,
                        ),
                    )

                    if (response.transactions.isEmpty()) {
                        Napier.d { "No more transactions to migrate" }
                        until = null
                        break
                    }

                    Napier.d { "Fetched ${response.transactions.size} transactions for page ${pageCount + 1}" }

                    // Map transactions for migration (sets walletType = SPARK)
                    val mapped = response.transactions
                        .mapForMigration(targetWalletId = targetWalletId, userId = userId)
                        .map { it.toWalletTransactionData() }

                    walletDatabase.walletTransactions().upsertAll(mapped)

                    // Fetch profiles for other users in transactions (for UI enrichment)
                    if (profileRepository != null) {
                        val otherUserIds = response.transactions.mapNotNull { it.otherPubkey }
                        if (otherUserIds.isNotEmpty()) {
                            profileRepository.fetchMissingProfiles(profileIds = otherUserIds)
                            Napier.d { "Fetched ${otherUserIds.size} profiles for transaction enrichment" }
                        }
                    }

                    // Save progress after each page (resume point for next iteration)
                    until = response.paging?.untilId

                    // Prevent infinite loop: if cursor didn't change, we're stuck
                    if (until != null && until == previousUntil) {
                        Napier.w { "Pagination cursor unchanged ($until), breaking to prevent infinite loop" }
                        until = null
                        break
                    }

                    walletDatabase.wallet().updatePrimalTxsMigratedUntil(
                        walletId = targetWalletId,
                        until = until,
                    )

                    pageCount++
                    onPageFetched?.invoke(pageCount)

                    // Stop if we've fetched enough pages (for foreground migration)
                    if (maxPages != null && pageCount >= maxPages) {
                        Napier.d { "Reached max pages limit ($maxPages), stopping" }
                        break
                    }
                } while (until != null)

                // Only mark as fully migrated if we fetched ALL pages (until == null)
                if (until == null) {
                    walletDatabase.wallet().updatePrimalTxsMigrated(
                        walletId = targetWalletId,
                        migrated = true,
                    )
                    Napier.i { "Transaction migration completed - all pages fetched" }
                } else {
                    Napier.i { "Transaction migration paused - $pageCount pages fetched, more remaining" }
                }
            }
        }
}
