package net.primal.data.account.remote.command.model

import kotlinx.serialization.Serializable
import net.primal.data.account.remote.command.serializer.NostrCommandSerializer

@Serializable(with = NostrCommandSerializer::class)
enum class NostrCommandMethod(val method: String) {
    Connect("connect"),
    Ping("ping"),
    SignEvent("sign_event"),
    GetPublicKey("get_public_key"),
    Nip04Encrypt("nip_04_encrypt"),
    Nip04Decrypt("nip_04_decrypt"),
    Nip44Encrypt("nip_44_encrypt"),
    Nip44Decrypt("nip_44_decrypt"),
}
