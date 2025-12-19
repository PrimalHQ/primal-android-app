package net.primal.android.signer.model

import kotlinx.serialization.Serializable
import net.primal.android.signer.model.serialization.SignerMethodSerializer

@Serializable(with = SignerMethodSerializer::class)
enum class SignerMethod(val method: String) {
    GET_PUBLIC_KEY("get_public_key"),
    SIGN_EVENT("sign_event"),
    NIP04_DECRYPT("nip04_decrypt"),
    NIP04_ENCRYPT("nip04_encrypt"),
    NIP44_DECRYPT("nip44_decrypt"),
    NIP44_ENCRYPT("nip44_encrypt"),
    DECRYPT_ZAP_EVENT("decrypt_zap_event"),
    ;

    companion object {
        fun fromString(method: String) =
            entries.firstOrNull { it.method == method }
                ?: error("Couldn't parse $method as valid method name.")
    }
}
