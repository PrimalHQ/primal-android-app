package net.primal.domain.account.model

import net.primal.domain.nostr.NostrEvent

sealed class LocalSignerMethodResponse(
    open val eventId: String,
) {
    data class GetPublicKey(
        override val eventId: String,
        val pubkey: String,
    ) : LocalSignerMethodResponse(eventId = eventId)

    data class SignEvent(
        override val eventId: String,
        val signedEvent: NostrEvent,
    ) : LocalSignerMethodResponse(eventId = eventId)

    data class Nip44Encrypt(
        override val eventId: String,
        val ciphertext: String,
    ) : LocalSignerMethodResponse(eventId = eventId)

    data class Nip04Encrypt(
        override val eventId: String,
        val ciphertext: String,
    ) : LocalSignerMethodResponse(eventId = eventId)

    data class Nip44Decrypt(
        override val eventId: String,
        val plaintext: String,
    ) : LocalSignerMethodResponse(eventId = eventId)

    data class Nip04Decrypt(
        override val eventId: String,
        val plaintext: String,
    ) : LocalSignerMethodResponse(eventId = eventId)

    data class DecryptZapEvent(
        override val eventId: String,
        val signedEvent: NostrEvent,
    ) : LocalSignerMethodResponse(eventId = eventId)
}
