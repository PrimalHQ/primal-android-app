package net.primal.wallet.data.local.dao

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

enum class ZapEnrichmentStatus {
    PENDING,
    ENRICHED,
    NOT_A_ZAP,
}

@Serializable
enum class EnrichmentAttemptVerdict {
    NETWORK_ERROR,
    NO_ZAP_REQUEST,
    MALFORMED_ZAP_REQUEST,
}

@Serializable
data class EnrichmentAttemptEntry(
    val attempt: Int,
    val timestamp: Long,
    val verdict: EnrichmentAttemptVerdict,
    val detail: String? = null,
)

@Entity(
    indices = [
        Index("invoice"),
        Index(value = ["status", "lastAttemptAt"]),
    ],
)
data class ZapEnrichmentTracker(
    @PrimaryKey
    val transactionId: String,
    val invoice: String,
    val transactionCreatedAt: Long,
    val status: ZapEnrichmentStatus = ZapEnrichmentStatus.PENDING,
    val attempts: Int = 0,
    val lastAttemptAt: Long = 0,
    @ColumnInfo(defaultValue = "")
    val attemptHistory: String = "",
)
