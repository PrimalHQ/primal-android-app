package net.primal.data.account.local.dao

import androidx.room.Entity
import androidx.room.PrimaryKey
import net.primal.shared.data.local.encryption.Encryptable

@Entity
data class LocalAppSessionEventData(
    @PrimaryKey val eventId: String,
    val sessionId: String,
    val appPackageName: String,
    val requestState: LocalRequestState,
    val processedAt: Long,
    val requestType: LocalSignerMethodType,
    val eventKind: Encryptable<Int>?,
    val requestPayload: Encryptable<String>?,
    val responsePayload: Encryptable<String>?,
)
