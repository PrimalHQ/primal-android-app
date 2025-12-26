package net.primal.data.account.local.dao.apps.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import net.primal.data.account.local.dao.apps.AppRequestState
import net.primal.shared.data.local.encryption.Encryptable

@Entity
data class LocalAppSessionEventData(
    @PrimaryKey val eventId: String,
    val sessionId: String,
    val appIdentifier: String,
    val requestState: AppRequestState,
    val requestedAt: Long,
    val completedAt: Long?,
    val requestType: LocalSignerMethodType,
    val eventKind: Encryptable<Int>?,
    val requestPayload: Encryptable<String>?,
    val responsePayload: Encryptable<String>?,
)
