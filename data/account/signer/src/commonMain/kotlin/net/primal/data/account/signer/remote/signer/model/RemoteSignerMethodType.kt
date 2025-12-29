package net.primal.data.account.signer.remote.signer.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class RemoteSignerMethodType {
    @SerialName("connect")
    Connect,

    @SerialName("ping")
    Ping,

    @SerialName("sign_event")
    SignEvent,

    @SerialName("get_public_key")
    GetPublicKey,

    @SerialName("nip04_encrypt")
    Nip04Encrypt,

    @SerialName("nip04_decrypt")
    Nip04Decrypt,

    @SerialName("nip44_encrypt")
    Nip44Encrypt,

    @SerialName("nip44_decrypt")
    Nip44Decrypt,
}
