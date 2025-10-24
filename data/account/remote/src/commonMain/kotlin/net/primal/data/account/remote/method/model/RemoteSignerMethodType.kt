package net.primal.data.account.remote.method.model

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

    @SerialName("nip_04_encrypt")
    Nip04Encrypt,

    @SerialName("nip_04_decrypt")
    Nip04Decrypt,

    @SerialName("nip_44_encrypt")
    Nip44Encrypt,

    @SerialName("nip_44_decrypt")
    Nip44Decrypt,
}
