package net.primal.data.account.local.dao

import androidx.room.Entity
import androidx.room.PrimaryKey
import net.primal.shared.data.local.encryption.Encryptable

@Entity
data class SessionEventData(
    @PrimaryKey
    val eventId: String,
    val sessionId: String,
    val clientPubKey: Encryptable<String>,
    val requestState: RequestState,
    val requestedAt: Long,
    val completedAt: Long?,
    val requestType: SignerMethodType,
    val requestTypeId: Encryptable<String>,
    val eventKind: Encryptable<Int>?,
    val thirdPartyPubKey: Encryptable<String>?,
    val plaintext: Encryptable<String>?,
    val ciphertext: Encryptable<String>?,
    val responsePayload: Encryptable<String>?,
)
