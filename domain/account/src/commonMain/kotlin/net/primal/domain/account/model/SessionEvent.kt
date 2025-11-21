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
    ) : SessionEvent()

    data class Decrypt(
        override val eventId: String,
        override val sessionId: String,
        override val requestState: RequestState,
        override val requestedAt: Long,
        override val completedAt: Long?,
        override val requestTypeId: String,
    ) : SessionEvent()

    data class SignEvent(
        override val eventId: String,
        override val sessionId: String,
        override val requestState: RequestState,
        override val requestedAt: Long,
        override val completedAt: Long?,
        val eventKind: Int,
        val signedNostrEventJson: String?,
        val unsignedNostrEventJson: String,
    ) : SessionEvent() {
        override val requestTypeId: String = "sign_event:$eventKind"
    }
}
