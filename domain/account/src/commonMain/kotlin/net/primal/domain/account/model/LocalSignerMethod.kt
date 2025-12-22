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
        val name: String?,
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

    fun getPermissionId() =
        when (this) {
            is DecryptZapEvent -> "decrypt_zap_event"
            is GetPublicKey -> "get_public_key"
            is Nip04Decrypt -> "nip04_decrypt"
            is Nip04Encrypt -> "nip04_encrypt"
            is Nip44Decrypt -> "nip44_decrypt"
            is Nip44Encrypt -> "nip44_encrypt"
            is SignEvent -> "sign_event:${this.unsignedEvent.kind}"
        }

    fun extractUserPubKey() =
        when (this) {
            is DecryptZapEvent -> this.userPubKey
            is GetPublicKey -> null
            is Nip04Decrypt -> this.userPubKey
            is Nip04Encrypt -> this.userPubKey
            is Nip44Decrypt -> this.userPubKey
            is Nip44Encrypt -> this.userPubKey
            is SignEvent -> this.userPubKey
        }

    fun getIdentifier() =
        when (this) {
            is DecryptZapEvent -> LocalApp.identifierOf(packageName = this.packageName, userId = this.userPubKey)
            is GetPublicKey -> this.packageName
            is Nip04Decrypt -> LocalApp.identifierOf(packageName = this.packageName, userId = this.userPubKey)
            is Nip04Encrypt -> LocalApp.identifierOf(packageName = this.packageName, userId = this.userPubKey)
            is Nip44Decrypt -> LocalApp.identifierOf(packageName = this.packageName, userId = this.userPubKey)
            is Nip44Encrypt -> LocalApp.identifierOf(packageName = this.packageName, userId = this.userPubKey)
            is SignEvent -> LocalApp.identifierOf(packageName = this.packageName, userId = this.userPubKey)
        }
}
