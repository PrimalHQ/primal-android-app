package net.primal.domain.account.model

import kotlinx.serialization.Serializable
import net.primal.domain.account.model.serializer.LocalSignerMethodResponseSerializer
import net.primal.domain.nostr.NostrEvent

@Serializable(with = LocalSignerMethodResponseSerializer::class)
sealed class LocalSignerMethodResponse(
    open val eventId: String,
) {
    sealed class Success(override val eventId: String) : LocalSignerMethodResponse(eventId = eventId) {
        data class GetPublicKey(
            override val eventId: String,
            val pubkey: String,
        ) : Success(eventId = eventId)

        data class SignEvent(
            override val eventId: String,
            val signedEvent: NostrEvent,
        ) : Success(eventId = eventId)

        data class Nip44Encrypt(
            override val eventId: String,
            val ciphertext: String,
        ) : Success(eventId = eventId)

        data class Nip04Encrypt(
            override val eventId: String,
            val ciphertext: String,
        ) : Success(eventId = eventId)

        data class Nip44Decrypt(
            override val eventId: String,
            val plaintext: String,
        ) : Success(eventId = eventId)

        data class Nip04Decrypt(
            override val eventId: String,
            val plaintext: String,
        ) : Success(eventId = eventId)

        data class DecryptZapEvent(
            override val eventId: String,
            val signedEvent: NostrEvent,
        ) : Success(eventId = eventId)
    }

    data class Error(
        override val eventId: String,
        val message: String,
    ) : LocalSignerMethodResponse(eventId = eventId)
}
