package net.primal.domain.account.model

sealed class SessionEvent {
    abstract val eventId: String
    abstract val sessionId: String
    abstract val requestState: RequestState
    abstract val requestedAt: Long
    abstract val completedAt: Long?
    abstract val requestTypeId: String

    data class GetPublicKey(
        override val eventId: String,
        override val sessionId: String,
        override val requestState: RequestState,
        override val requestedAt: Long,
        override val completedAt: Long?,
        val publicKey: String?,
    ) : SessionEvent() {
        override val requestTypeId: String = "get_public_key"
    }

    data class Encrypt(
        override val eventId: String,
        override val sessionId: String,
        override val requestState: RequestState,
        override val requestedAt: Long,
        override val completedAt: Long?,
        override val requestTypeId: String,
        val thirdPartyPubKey: String,
        val plaintext: String,
        val encryptedPayload: String?,
    ) : SessionEvent()

    data class Decrypt(
        override val eventId: String,
        override val sessionId: String,
        override val requestState: RequestState,
        override val requestedAt: Long,
        override val completedAt: Long?,
        override val requestTypeId: String,
        val thirdPartyPubKey: String,
        val ciphertext: String,
        val decryptedPayload: String?,
    ) : SessionEvent()

    data class SignEvent(
        override val eventId: String,
        override val sessionId: String,
        override val requestState: RequestState,
        override val requestedAt: Long,
        override val completedAt: Long?,
        val eventKind: Int,
        val signedNostrEventJson: String?,
    ) : SessionEvent() {
        override val requestTypeId: String = "sign_event:$eventKind"
    }
}
