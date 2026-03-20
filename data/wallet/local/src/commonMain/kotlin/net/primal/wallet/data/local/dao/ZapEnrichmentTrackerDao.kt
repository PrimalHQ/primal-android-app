package net.primal.wallet.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface ZapEnrichmentTrackerDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAllIgnoreConflicts(data: List<ZapEnrichmentTracker>)

    @Query(
        """
        SELECT t.transactionId, t.invoice, t.createdAt AS transactionCreatedAt
        FROM WalletTransactionData t
        LEFT JOIN ZapEnrichmentTracker z ON t.transactionId = z.transactionId
        WHERE z.transactionId IS NULL
            AND t.txKind IN ('LIGHTNING', 'SPARK')
            AND t.invoice IS NOT NULL
            AND t.walletType != 'PRIMAL'
            AND (t.createdAt >= :minCreatedAt OR t.walletType = 'NWC')
        ORDER BY t.createdAt DESC
        """,
    )
    suspend fun findUntrackedCandidates(minCreatedAt: Long): List<UntrackedCandidate>

    @Query(
        """
        SELECT * FROM ZapEnrichmentTracker
        WHERE status = 'PENDING'
            AND attempts < :maxAttempts
            AND (
                (attempts = 0) OR
                (attempts = 1 AND lastAttemptAt <= :threshold1) OR
                (attempts = 2 AND lastAttemptAt <= :threshold2) OR
                (attempts = 3 AND lastAttemptAt <= :threshold3)
            )
        ORDER BY transactionCreatedAt DESC
        LIMIT :limit
        """,
    )
    suspend fun findEligiblePending(
        maxAttempts: Int,
        threshold1: Long,
        threshold2: Long,
        threshold3: Long,
        limit: Int,
    ): List<ZapEnrichmentTracker>

    @Query(
        """
        UPDATE ZapEnrichmentTracker
        SET status = 'ENRICHED'
        WHERE transactionId = :transactionId
        """,
    )
    suspend fun markEnriched(transactionId: String)

    @Query(
        """
        UPDATE ZapEnrichmentTracker
        SET attempts = attempts + 1,
            lastAttemptAt = :attemptAt,
            status = CASE WHEN attempts + 1 >= :maxAttempts THEN 'NOT_A_ZAP' ELSE status END
        WHERE transactionId = :transactionId
        """,
    )
    suspend fun incrementAttempt(
        transactionId: String,
        attemptAt: Long,
        maxAttempts: Int,
    )

    @Query(
        """
        DELETE FROM ZapEnrichmentTracker
        WHERE transactionId IN (
            SELECT transactionId FROM WalletTransactionData WHERE userId = :userId
        )
        """,
    )
    suspend fun deleteByUserId(userId: String)

    @Query("UPDATE ZapEnrichmentTracker SET lastAttemptAt = :lastAttemptAt WHERE transactionId = :transactionId")
    suspend fun updateLastAttemptAt(transactionId: String, lastAttemptAt: Long)

    @Query(
        """
        UPDATE ZapEnrichmentTracker
        SET attemptHistory = CASE
            WHEN attemptHistory = '' THEN :entryJson
            ELSE attemptHistory || ',' || :entryJson
        END
        WHERE transactionId = :transactionId
        """,
    )
    suspend fun appendAttemptHistory(transactionId: String, entryJson: String)

    @Query("SELECT * FROM ZapEnrichmentTracker WHERE transactionId = :transactionId")
    suspend fun findByTransactionId(transactionId: String): ZapEnrichmentTracker?

    @Query("SELECT * FROM ZapEnrichmentTracker")
    suspend fun findAll(): List<ZapEnrichmentTracker>
}

data class UntrackedCandidate(
    val transactionId: String,
    val invoice: String,
    val transactionCreatedAt: Long,
)
