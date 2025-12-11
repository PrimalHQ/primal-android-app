package net.primal.domain.account.model

import net.primal.domain.nostr.NostrEvent
import net.primal.domain.nostr.NostrUnsignedEvent

sealed class LocalSignerMethod(
    open val eventId: String,
    open val packageName: String,
) {
    data class GetPublicKey(
        override val eventId: String,
        override val packageName: String,
        val permissions: List<AppPermission>,
    ) : LocalSignerMethod(eventId = eventId, packageName = packageName)

    data class SignEvent(
        override val eventId: String,
        override val packageName: String,
        val userPubKey: String,
        val unsignedEvent: NostrUnsignedEvent,
    ) : LocalSignerMethod(eventId = eventId, packageName = packageName)

    data class Nip44Decrypt(
        override val eventId: String,
        override val packageName: String,
        val userPubKey: String,
        val otherPubKey: String,
        val ciphertext: String,
    ) : LocalSignerMethod(eventId = eventId, packageName = packageName)

    data class Nip04Decrypt(
        override val eventId: String,
        override val packageName: String,
        val userPubKey: String,
        val otherPubKey: String,
        val ciphertext: String,
    ) : LocalSignerMethod(eventId = eventId, packageName = packageName)

    data class Nip44Encrypt(
        override val eventId: String,
        override val packageName: String,
        val userPubKey: String,
        val otherPubKey: String,
        val plaintext: String,
    ) : LocalSignerMethod(eventId = eventId, packageName = packageName)

    data class Nip04Encrypt(
        override val eventId: String,
        override val packageName: String,
        val userPubKey: String,
        val otherPubKey: String,
        val plaintext: String,
    ) : LocalSignerMethod(eventId = eventId, packageName = packageName)

    data class DecryptZapEvent(
        override val eventId: String,
        override val packageName: String,
        val userPubKey: String,
        val signedEvent: NostrEvent,
    ) : LocalSignerMethod(eventId = eventId, packageName = packageName)
}
