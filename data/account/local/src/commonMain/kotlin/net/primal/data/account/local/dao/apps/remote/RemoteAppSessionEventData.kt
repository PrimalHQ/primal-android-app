package net.primal.data.account.local.dao.apps.remote

import androidx.room.Entity
import androidx.room.PrimaryKey
import net.primal.shared.data.local.encryption.Encryptable

@Entity
data class RemoteAppSessionEventData(
    @PrimaryKey
    val eventId: String,
    val sessionId: String,
    val clientPubKey: String,
    val signerPubKey: String,
    val requestState: RemoteAppRequestState,
    val requestedAt: Long,
    val completedAt: Long?,
    val requestType: RemoteSignerMethodType,
    val eventKind: Encryptable<Int>?,
    val requestPayload: Encryptable<String>?,
    val responsePayload: Encryptable<String>?,
)
