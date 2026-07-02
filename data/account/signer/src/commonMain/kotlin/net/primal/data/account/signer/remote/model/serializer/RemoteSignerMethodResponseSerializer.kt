package net.primal.data.account.signer.remote.model.serializer

import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerializationException
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonDecoder
import kotlinx.serialization.json.JsonEncoder
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import net.primal.data.account.signer.remote.model.RemoteSignerMethodResponse

object RemoteSignerMethodResponseSerializer : KSerializer<RemoteSignerMethodResponse> {
    override val descriptor: SerialDescriptor =
        buildClassSerialDescriptor("RemoteSignerMethodResponse")

    override fun serialize(encoder: Encoder, value: RemoteSignerMethodResponse) {
        val jsonEncoder = encoder as? JsonEncoder
            ?: error("RemoteSignerMethodResponse supports JSON only")
        val obj: JsonObject = when (value) {
            is RemoteSignerMethodResponse.Success -> buildJsonObject {
                put("id", JsonPrimitive(value.id))
                put("result", value.result?.let { JsonPrimitive(it) } ?: JsonNull)
            }

            is RemoteSignerMethodResponse.Error -> buildJsonObject {
                put("id", JsonPrimitive(value.id))
                put("error", JsonPrimitive(value.error))
            }
        }
        jsonEncoder.encodeJsonElement(obj)
    }

    override fun deserialize(decoder: Decoder): RemoteSignerMethodResponse {
        val jsonDecoder = decoder as? JsonDecoder
            ?: error("RemoteSignerMethodResponse supports JSON only")
        val obj = jsonDecoder.decodeJsonElement().jsonObject

        val id = obj["id"]?.jsonPrimitive?.content
            ?: throw SerializationException("Missing 'id'")

        val errorContent = obj["error"]?.jsonPrimitive?.content
        val resultElement = obj["result"]
        return when {
            errorContent != null -> {
                RemoteSignerMethodResponse.Error(
                    id = id,
                    clientPubKey = "",
                    error = errorContent,
                )
            }

            resultElement != null -> {
                RemoteSignerMethodResponse.Success(
                    id = id,
                    clientPubKey = "",
                    result = if (resultElement is JsonNull) null else resultElement.jsonPrimitive.content,
                )
            }

            else -> throw SerializationException("Unsupported shape: expected 'result' or 'error'")
        }
    }
}
