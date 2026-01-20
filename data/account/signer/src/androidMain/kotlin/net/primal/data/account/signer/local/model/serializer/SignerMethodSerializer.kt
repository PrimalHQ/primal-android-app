package net.primal.data.account.signer.local.model.serializer

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import net.primal.data.account.signer.local.model.SignerMethod

object SignerMethodSerializer : KSerializer<SignerMethod> {
    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("SignerMethod", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: SignerMethod) {
        encoder.encodeString(value.method)
    }

    override fun deserialize(decoder: Decoder): SignerMethod {
        val decoded = decoder.decodeString()

        return SignerMethod.Companion.fromString(decoded)
    }
}
