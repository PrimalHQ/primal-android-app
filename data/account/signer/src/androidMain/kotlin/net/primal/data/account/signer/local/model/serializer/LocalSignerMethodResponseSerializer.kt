package net.primal.data.account.signer.local.model.serializer

import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerializationException
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonDecoder
import kotlinx.serialization.json.JsonEncoder
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import net.primal.data.account.signer.local.model.LocalSignerMethodResponse
import net.primal.data.account.signer.local.model.LocalSignerMethodType
import net.primal.domain.nostr.NostrEvent

object LocalSignerMethodResponseSerializer : KSerializer<LocalSignerMethodResponse> {
    override val descriptor: SerialDescriptor =
        buildClassSerialDescriptor("LocalSignerMethodResponse")

    override fun serialize(encoder: Encoder, value: LocalSignerMethodResponse) {
        val jsonEncoder = encoder as? JsonEncoder
            ?: error("LocalSignerMethodResponse supports JSON only")
        val obj: JsonObject = when (value) {
            is LocalSignerMethodResponse.Error -> buildJsonObject {
                put("eventId", JsonPrimitive(value.eventId))
                put("error", JsonPrimitive(value.message))
            }

            is LocalSignerMethodResponse.Success.GetPublicKey -> buildJsonObject {
                put("type", JsonPrimitive(LocalSignerMethodType.GetPublicKey.name))
                put("eventId", JsonPrimitive(value.eventId))
                put("pubkey", JsonPrimitive(value.pubkey))
            }

            is LocalSignerMethodResponse.Success.SignEvent -> buildJsonObject {
                put("type", JsonPrimitive(LocalSignerMethodType.SignEvent.name))
                put("eventId", JsonPrimitive(value.eventId))
                val ev =
                    jsonEncoder.json.encodeToJsonElement(NostrEvent.serializer(), value.signedEvent)
                put("signedEvent", ev)
            }

            is LocalSignerMethodResponse.Success.Nip44Encrypt -> buildJsonObject {
                put("type", JsonPrimitive(LocalSignerMethodType.Nip44Encrypt.name))
                put("eventId", JsonPrimitive(value.eventId))
                put("ciphertext", JsonPrimitive(value.ciphertext))
            }

            is LocalSignerMethodResponse.Success.Nip04Encrypt -> buildJsonObject {
                put("type", JsonPrimitive(LocalSignerMethodType.Nip04Encrypt.name))
                put("eventId", JsonPrimitive(value.eventId))
                put("ciphertext", JsonPrimitive(value.ciphertext))
            }

            is LocalSignerMethodResponse.Success.Nip44Decrypt -> buildJsonObject {
                put("type", JsonPrimitive(LocalSignerMethodType.Nip44Decrypt.name))
                put("eventId", JsonPrimitive(value.eventId))
                put("plaintext", JsonPrimitive(value.plaintext))
            }

            is LocalSignerMethodResponse.Success.Nip04Decrypt -> buildJsonObject {
                put("type", JsonPrimitive(LocalSignerMethodType.Nip04Decrypt.name))
                put("eventId", JsonPrimitive(value.eventId))
                put("plaintext", JsonPrimitive(value.plaintext))
            }

            is LocalSignerMethodResponse.Success.DecryptZapEvent -> buildJsonObject {
                put("type", JsonPrimitive(LocalSignerMethodType.DecryptZapEvent.name))
                put("eventId", JsonPrimitive(value.eventId))
                val ev =
                    jsonEncoder.json.encodeToJsonElement(NostrEvent.serializer(), value.signedEvent)
                put("signedEvent", ev)
            }
        }

        jsonEncoder.encodeJsonElement(obj)
    }

    override fun deserialize(decoder: Decoder): LocalSignerMethodResponse {
        val jsonDecoder = decoder as? JsonDecoder
            ?: error("LocalSignerMethodResponse supports JSON only")
        val obj = jsonDecoder.decodeJsonElement().jsonObject

        val error = obj["error"]?.jsonPrimitive?.content
        if (error != null) {
            val eventIdForError = obj["eventId"]?.jsonPrimitive?.content
                ?: throw SerializationException("Missing 'eventId'")
            return LocalSignerMethodResponse.Error(eventId = eventIdForError, message = error)
        }

        val typeName = obj["type"]?.jsonPrimitive?.content
            ?: throw SerializationException("Missing 'type'")
        val eventId = obj["eventId"]?.jsonPrimitive?.content
            ?: throw SerializationException("Missing 'eventId'")

        return when (typeName) {
            LocalSignerMethodType.GetPublicKey.name -> {
                val pubkey = obj["pubkey"]?.jsonPrimitive?.content
                    ?: throw SerializationException("Missing 'pubkey'")
                LocalSignerMethodResponse.Success.GetPublicKey(eventId = eventId, pubkey = pubkey)
            }

            LocalSignerMethodType.SignEvent.name -> {
                val evObj = obj["signedEvent"]?.jsonObject
                    ?: throw SerializationException("Missing 'signedEvent'")
                val ev = jsonDecoder.json.decodeFromJsonElement(NostrEvent.serializer(), evObj)
                LocalSignerMethodResponse.Success.SignEvent(eventId = eventId, signedEvent = ev)
            }

            LocalSignerMethodType.Nip44Encrypt.name -> {
                val ciphertext = obj["ciphertext"]?.jsonPrimitive?.content
                    ?: throw SerializationException("Missing 'ciphertext'")
                LocalSignerMethodResponse.Success.Nip44Encrypt(eventId = eventId, ciphertext = ciphertext)
            }

            LocalSignerMethodType.Nip04Encrypt.name -> {
                val ciphertext = obj["ciphertext"]?.jsonPrimitive?.content
                    ?: throw SerializationException("Missing 'ciphertext'")
                LocalSignerMethodResponse.Success.Nip04Encrypt(eventId = eventId, ciphertext = ciphertext)
            }

            LocalSignerMethodType.Nip44Decrypt.name -> {
                val plaintext = obj["plaintext"]?.jsonPrimitive?.content
                    ?: throw SerializationException("Missing 'plaintext'")
                LocalSignerMethodResponse.Success.Nip44Decrypt(eventId = eventId, plaintext = plaintext)
            }

            LocalSignerMethodType.Nip04Decrypt.name -> {
                val plaintext = obj["plaintext"]?.jsonPrimitive?.content
                    ?: throw SerializationException("Missing 'plaintext'")
                LocalSignerMethodResponse.Success.Nip04Decrypt(eventId = eventId, plaintext = plaintext)
            }

            LocalSignerMethodType.DecryptZapEvent.name -> {
                val evObj = obj["signedEvent"]?.jsonObject
                    ?: throw SerializationException("Missing 'signedEvent'")
                val ev = jsonDecoder.json.decodeFromJsonElement(NostrEvent.serializer(), evObj)
                LocalSignerMethodResponse.Success.DecryptZapEvent(eventId = eventId, signedEvent = ev)
            }

            else -> throw SerializationException("Unsupported LocalSignerMethodResponse type: $typeName")
        }
    }
}
