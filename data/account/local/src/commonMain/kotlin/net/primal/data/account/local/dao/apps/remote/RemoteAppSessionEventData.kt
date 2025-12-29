package net.primal.data.account.local.dao.apps.remote

import androidx.room.Entity
import androidx.room.PrimaryKey
import net.primal.data.account.local.dao.apps.AppRequestState
import net.primal.data.account.local.dao.apps.SignerMethodType
import net.primal.shared.data.local.encryption.Encryptable

@Entity
data class RemoteAppSessionEventData(
    @PrimaryKey
    val eventId: String,
    val sessionId: String,
    val clientPubKey: String,
    val signerPubKey: String,
    val requestState: AppRequestState,
    val requestedAt: Long,
    val completedAt: Long?,
    val requestType: SignerMethodType,
    val eventKind: Encryptable<Int>?,
    val requestPayload: Encryptable<String>?,
    val responsePayload: Encryptable<String>?,
)
