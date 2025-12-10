package net.primal.domain.account.model

import net.primal.domain.nostr.NostrUnsignedEvent

sealed class LocalSignerMethod(
    open val eventId: String,
) {
    data class GetPublicKey(
        override val eventId: String,
        val permissions: List<AppPermission>,
    ) : LocalSignerMethod(eventId = eventId)

    data class SignEvent(
        override val eventId: String,
        val userPubKey: String,
        val unsignedEvent: NostrUnsignedEvent,
    ) : LocalSignerMethod(eventId = eventId)

    data class Nip44Decrypt(
        override val eventId: String,
        val userPubKey: String,
        val otherPubKey: String,
        val ciphertext: String,
    ) : LocalSignerMethod(eventId = eventId)

    data class Nip04Decrypt(
        override val eventId: String,
        val userPubKey: String,
        val otherPubKey: String,
        val ciphertext: String,
    ) : LocalSignerMethod(eventId = eventId)

    data class Nip44Encrypt(
        override val eventId: String,
        val userPubKey: String,
        val otherPubKey: String,
        val plaintext: String,
    ) : LocalSignerMethod(eventId = eventId)

    data class Nip04Encrypt(
        override val eventId: String,
        val userPubKey: String,
        val otherPubKey: String,
        val plaintext: String,
    ) : LocalSignerMethod(eventId = eventId)

    data class DecryptZapEvent(
        override val eventId: String,
        val userPubKey: String,
        val unsignedEvent: NostrUnsignedEvent,
    ) : LocalSignerMethod(eventId = eventId)
}
