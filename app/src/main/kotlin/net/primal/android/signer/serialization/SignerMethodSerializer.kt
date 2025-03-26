package net.primal.android.signer.serialization

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import net.primal.android.signer.SignerMethod
import net.primal.android.signer.SignerMethod.entries

object SignerMethodSerializer : KSerializer<SignerMethod> {
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
