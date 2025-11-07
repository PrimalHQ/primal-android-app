package net.primal.data.account.remote.method.model

import kotlinx.serialization.Serializable

@Serializable
sealed class RemoteSignerMethod {
    abstract val id: String
    abstract val clientPubKey: String

    @Serializable
    data class Connect(
        override val id: String,
        override val clientPubKey: String,
        val remoteSignerPubkey: String,
        val secret: String?,
        val requestedPermissions: List<String>,
    ) : RemoteSignerMethod()

    @Serializable
    data class SignEvent(
        override val id: String,
        override val clientPubKey: String,
        val unsignedEvent: NostrUnsignedEventNoPubkey,
    ) : RemoteSignerMethod()

    @Serializable
    data class Ping(
        override val id: String,
        override val clientPubKey: String,
    ) : RemoteSignerMethod()

    @Serializable
    data class GetPublicKey(
        override val id: String,
        override val clientPubKey: String,
    ) : RemoteSignerMethod()

    @Serializable
    data class Nip04Encrypt(
        override val id: String,
        override val clientPubKey: String,
        val thirdPartyPubKey: String,
        val plaintext: String,
    ) : RemoteSignerMethod()

    @Serializable
    data class Nip04Decrypt(
        override val id: String,
        override val clientPubKey: String,
        val thirdPartyPubKey: String,
        val ciphertext: String,
    ) : RemoteSignerMethod()

    @Serializable
    data class Nip44Encrypt(
        override val id: String,
        override val clientPubKey: String,
        val thirdPartyPubKey: String,
        val plaintext: String,
    ) : RemoteSignerMethod()

    @Serializable
    data class Nip44Decrypt(
        override val id: String,
        override val clientPubKey: String,
        val thirdPartyPubKey: String,
        val ciphertext: String,
    ) : RemoteSignerMethod()
}
