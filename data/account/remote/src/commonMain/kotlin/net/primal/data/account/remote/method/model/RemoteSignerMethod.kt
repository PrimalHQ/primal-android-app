package net.primal.data.account.remote.method.model

import net.primal.domain.nostr.NostrUnsignedEvent

sealed class RemoteSignerMethod(open val id: String, open val clientPubKey: String) {
    data class Connect(
        override val id: String,
        override val clientPubKey: String,
        val remoteSignerPubkey: String,
        val secret: String,
        val requestedPermissions: List<String>,
    ) : RemoteSignerMethod(id = id, clientPubKey = clientPubKey)

    data class SignEvent(
        override val id: String,
        override val clientPubKey: String,
        val unsignedEvent: NostrUnsignedEvent,
    ) : RemoteSignerMethod(id = id, clientPubKey = clientPubKey)

    data class Ping(
        override val id: String,
        override val clientPubKey: String,
    ) : RemoteSignerMethod(id = id, clientPubKey = clientPubKey)

    data class GetPublicKey(
        override val id: String,
        override val clientPubKey: String,
    ) : RemoteSignerMethod(id = id, clientPubKey = clientPubKey)

    data class Nip04Encrypt(
        override val id: String,
        override val clientPubKey: String,
        val thirdPartyPubKey: String,
        val plaintext: String,
    ) : RemoteSignerMethod(id = id, clientPubKey = clientPubKey)

    data class Nip04Decrypt(
        override val id: String,
        override val clientPubKey: String,
        val thirdPartyPubKey: String,
        val ciphertext: String,
    ) : RemoteSignerMethod(id = id, clientPubKey = clientPubKey)

    data class Nip44Encrypt(
        override val id: String,
        override val clientPubKey: String,
        val thirdPartyPubKey: String,
        val plaintext: String,
    ) : RemoteSignerMethod(id = id, clientPubKey = clientPubKey)

    data class Nip44Decrypt(
        override val id: String,
        override val clientPubKey: String,
        val thirdPartyPubKey: String,
        val ciphertext: String,
    ) : RemoteSignerMethod(id = id, clientPubKey = clientPubKey)
}
