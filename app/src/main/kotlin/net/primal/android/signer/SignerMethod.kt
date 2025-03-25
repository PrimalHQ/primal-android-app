package net.primal.android.signer

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import net.primal.android.signer.SignerMethod.entries

@Serializable(with = SignerMethod.Companion::class)
enum class SignerMethod(val method: String) {
    GET_PUBLIC_KEY("get_public_key"),
    SIGN_EVENT("sign_event"),
    NIP04_DECRYPT("nip04_decrypt"),
    NIP04_ENCRYPT("nip04_encrypt"),
    ;

    companion object : KSerializer<SignerMethod> {
        override val descriptor: SerialDescriptor =
            PrimitiveSerialDescriptor("SignerMethod", PrimitiveKind.STRING)

        override fun serialize(encoder: Encoder, value: SignerMethod) {
            encoder.encodeString(value.method)
        }

        override fun deserialize(decoder: Decoder): SignerMethod {
            val decoded = decoder.decodeString()
            return entries.firstOrNull { it.method == decoded }
                ?: throw IllegalArgumentException("Unknown method: $decoded")
        }
    }
}
