package net.primal.android.wallet.domain.serializer

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonDecoder
import kotlinx.serialization.json.jsonPrimitive
import net.primal.android.wallet.domain.Network

object NetworkSerializer : KSerializer<Network> {

    override val descriptor = PrimitiveSerialDescriptor("NetworkSerializer", PrimitiveKind.INT)

    override fun serialize(encoder: Encoder, value: Network) {
        encoder.encodeString(
            when (value) {
                Network.Lightning -> "lightning"
                Network.Bitcoin -> "onchain"
            },
        )
    }

    override fun deserialize(decoder: Decoder): Network {
        return when (decoder) {
            is JsonDecoder -> {
                val jsonElement = decoder.decodeJsonElement()
                if (jsonElement.jsonPrimitive.isString) {
                    when (jsonElement.jsonPrimitive.content) {
                        "onchain" -> Network.Bitcoin
                        "lightning" -> Network.Lightning
                        else -> Network.Lightning
                    }
                } else {
                    Network.Lightning
                }
            }
            else -> error("Only JSON supported for deserialization.")
        }
    }
}
