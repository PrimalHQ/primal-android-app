package net.primal.android.wallet.domain.serializer

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonDecoder
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonPrimitive
import net.primal.android.wallet.domain.WalletKycLevel

object WalletKycLevelSerializer : KSerializer<WalletKycLevel> {

    override val descriptor = PrimitiveSerialDescriptor("WalletKycLevelSerializer", PrimitiveKind.INT)

    override fun serialize(encoder: Encoder, value: WalletKycLevel) {
        encoder.encodeInt(value.id)
    }

    override fun deserialize(decoder: Decoder): WalletKycLevel {
        return when (decoder) {
            is JsonDecoder -> {
                val jsonElement = decoder.decodeJsonElement()
                if (jsonElement.jsonPrimitive.isString) {
                    when (val value = jsonElement.jsonPrimitive.content) {
                        "NONE" -> WalletKycLevel.None
                        "EMAIL" -> WalletKycLevel.Email
                        else -> throw IllegalArgumentException("Invalid WalletKycLevel value: $value")
                    }
                } else {
                    when (val value = jsonElement.jsonPrimitive.intOrNull) {
                        0 -> WalletKycLevel.None
                        2 -> WalletKycLevel.Email
                        else -> throw IllegalArgumentException("Invalid WalletKycLevel value: $value")
                    }
                }
            }
            else -> error("Only JSON supported for deserialization.")
        }
    }
}
