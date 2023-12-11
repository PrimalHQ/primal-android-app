package net.primal.android.wallet.domain.serializer

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonDecoder
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonPrimitive
import net.primal.android.wallet.domain.SubWallet

object SubWalletSerializer : KSerializer<SubWallet> {

    override val descriptor = PrimitiveSerialDescriptor("SubWalletSerializer", PrimitiveKind.INT)

    override fun serialize(encoder: Encoder, value: SubWallet) {
        encoder.encodeInt(value.id)
    }

    override fun deserialize(decoder: Decoder): SubWallet {
        return when (decoder) {
            is JsonDecoder -> {
                val jsonElement = decoder.decodeJsonElement()
                if (jsonElement.jsonPrimitive.isString) {
                    when (val value = jsonElement.jsonPrimitive.content) {
                        "ZAPPING" -> SubWallet.Open
                        else -> throw IllegalArgumentException("Invalid SubWallet value: $value")
                    }
                } else {
                    when (val value = jsonElement.jsonPrimitive.intOrNull) {
                        1 -> SubWallet.Open
                        else -> throw IllegalArgumentException("Invalid SubWallet value: $value")
                    }
                }
            }
            else -> error("Only JSON supported for deserialization.")
        }
    }
}
