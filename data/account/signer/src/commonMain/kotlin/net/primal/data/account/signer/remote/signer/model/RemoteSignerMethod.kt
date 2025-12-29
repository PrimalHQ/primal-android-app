package net.primal.data.account.signer.remote.signer.model

import kotlinx.serialization.Serializable
import net.primal.data.account.signer.remote.signer.utils.PERM_ID_CONNECT
import net.primal.data.account.signer.remote.signer.utils.PERM_ID_GET_PUBLIC_KEY
import net.primal.data.account.signer.remote.signer.utils.PERM_ID_NIP04_DECRYPT
import net.primal.data.account.signer.remote.signer.utils.PERM_ID_NIP04_ENCRYPT
import net.primal.data.account.signer.remote.signer.utils.PERM_ID_NIP44_DECRYPT
import net.primal.data.account.signer.remote.signer.utils.PERM_ID_NIP44_ENCRYPT
import net.primal.data.account.signer.remote.signer.utils.PERM_ID_PING
import net.primal.data.account.signer.remote.signer.utils.PERM_ID_PREFIX_SIGN_EVENT

@Serializable
sealed class RemoteSignerMethod {
    abstract val id: String
    abstract val clientPubKey: String
    abstract val requestedAt: Long

    @Serializable
    data class Connect(
        override val id: String,
        override val clientPubKey: String,
        override val requestedAt: Long,
        val remoteSignerPubkey: String,
        val secret: String?,
        val requestedPermissions: List<String>,
    ) : RemoteSignerMethod()

    @Serializable
    data class SignEvent(
        override val id: String,
        override val clientPubKey: String,
        override val requestedAt: Long,
        val unsignedEvent: NostrUnsignedEventNoPubkey,
    ) : RemoteSignerMethod()

    @Serializable
    data class Ping(
        override val id: String,
        override val clientPubKey: String,
        override val requestedAt: Long,
    ) : RemoteSignerMethod()

    @Serializable
    data class GetPublicKey(
        override val id: String,
        override val clientPubKey: String,
        override val requestedAt: Long,
    ) : RemoteSignerMethod()

    @Serializable
    data class Nip04Encrypt(
        override val id: String,
        override val clientPubKey: String,
        override val requestedAt: Long,
        val thirdPartyPubKey: String,
        val plaintext: String,
    ) : RemoteSignerMethod()

    @Serializable
    data class Nip04Decrypt(
        override val id: String,
        override val clientPubKey: String,
        override val requestedAt: Long,
        val thirdPartyPubKey: String,
        val ciphertext: String,
    ) : RemoteSignerMethod()

    @Serializable
    data class Nip44Encrypt(
        override val id: String,
        override val clientPubKey: String,
        override val requestedAt: Long,
        val thirdPartyPubKey: String,
        val plaintext: String,
    ) : RemoteSignerMethod()

    @Serializable
    data class Nip44Decrypt(
        override val id: String,
        override val clientPubKey: String,
        override val requestedAt: Long,
        val thirdPartyPubKey: String,
        val ciphertext: String,
    ) : RemoteSignerMethod()

    fun getPermissionId(): String =
        when (this) {
            is Connect -> PERM_ID_CONNECT
            is GetPublicKey -> PERM_ID_GET_PUBLIC_KEY
            is Nip04Decrypt -> PERM_ID_NIP04_DECRYPT
            is Nip04Encrypt -> PERM_ID_NIP04_ENCRYPT
            is Nip44Decrypt -> PERM_ID_NIP44_DECRYPT
            is Nip44Encrypt -> PERM_ID_NIP44_ENCRYPT
            is Ping -> PERM_ID_PING
            is SignEvent -> "${PERM_ID_PREFIX_SIGN_EVENT}${this.unsignedEvent.kind}"
        }
}
