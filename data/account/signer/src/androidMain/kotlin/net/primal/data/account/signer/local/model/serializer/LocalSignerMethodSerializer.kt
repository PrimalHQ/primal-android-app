package net.primal.data.account.signer.local.model.serializer

import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerializationException
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonDecoder
import kotlinx.serialization.json.JsonEncoder
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.long
import net.primal.data.account.signer.local.model.LocalSignerMethod
import net.primal.data.account.signer.local.model.LocalSignerMethodType
import net.primal.domain.account.model.AppPermission
import net.primal.domain.nostr.NostrEvent
import net.primal.domain.nostr.NostrUnsignedEvent

object LocalSignerMethodSerializer : KSerializer<LocalSignerMethod> {
    override val descriptor: SerialDescriptor =
        buildClassSerialDescriptor("LocalSignerMethod")

    override fun serialize(encoder: Encoder, value: LocalSignerMethod) {
        val jsonEncoder = encoder as? JsonEncoder
            ?: error("LocalSignerMethod supports JSON only")

        val obj: JsonObject = when (value) {
            is LocalSignerMethod.GetPublicKey -> buildJsonObject {
                put("type", JsonPrimitive(LocalSignerMethodType.GetPublicKey.name))
                put("eventId", JsonPrimitive(value.eventId))
                put("packageName", JsonPrimitive(value.packageName))
                put("requestedAt", JsonPrimitive(value.requestedAt))

                val perms = jsonEncoder.json.encodeToJsonElement(
                    ListSerializer(AppPermission.serializer()),
                    value.permissions,
                )
                put("permissions", perms)
            }

            is LocalSignerMethod.SignEvent -> buildJsonObject {
                put("type", JsonPrimitive(LocalSignerMethodType.SignEvent.name))
                put("eventId", JsonPrimitive(value.eventId))
                put("packageName", JsonPrimitive(value.packageName))
                put("requestedAt", JsonPrimitive(value.requestedAt))
                put("userPubKey", JsonPrimitive(value.userPubKey))
                val unsigned = jsonEncoder.json.encodeToJsonElement(
                    NostrUnsignedEvent.serializer(),
                    value.unsignedEvent,
                )
                put("unsignedEvent", unsigned)
            }

            is LocalSignerMethod.Nip44Decrypt -> buildJsonObject {
                put("type", JsonPrimitive(LocalSignerMethodType.Nip44Decrypt.name))
                put("eventId", JsonPrimitive(value.eventId))
                put("packageName", JsonPrimitive(value.packageName))
                put("requestedAt", JsonPrimitive(value.requestedAt))
                put("userPubKey", JsonPrimitive(value.userPubKey))
                put("otherPubKey", JsonPrimitive(value.otherPubKey))
                put("ciphertext", JsonPrimitive(value.ciphertext))
            }

            is LocalSignerMethod.Nip04Decrypt -> buildJsonObject {
                put("type", JsonPrimitive(LocalSignerMethodType.Nip04Decrypt.name))
                put("eventId", JsonPrimitive(value.eventId))
                put("packageName", JsonPrimitive(value.packageName))
                put("requestedAt", JsonPrimitive(value.requestedAt))
                put("userPubKey", JsonPrimitive(value.userPubKey))
                put("otherPubKey", JsonPrimitive(value.otherPubKey))
                put("ciphertext", JsonPrimitive(value.ciphertext))
            }

            is LocalSignerMethod.Nip44Encrypt -> buildJsonObject {
                put("type", JsonPrimitive(LocalSignerMethodType.Nip44Encrypt.name))
                put("eventId", JsonPrimitive(value.eventId))
                put("packageName", JsonPrimitive(value.packageName))
                put("requestedAt", JsonPrimitive(value.requestedAt))
                put("userPubKey", JsonPrimitive(value.userPubKey))
                put("otherPubKey", JsonPrimitive(value.otherPubKey))
                put("plaintext", JsonPrimitive(value.plaintext))
            }

            is LocalSignerMethod.Nip04Encrypt -> buildJsonObject {
                put("type", JsonPrimitive(LocalSignerMethodType.Nip04Encrypt.name))
                put("eventId", JsonPrimitive(value.eventId))
                put("packageName", JsonPrimitive(value.packageName))
                put("requestedAt", JsonPrimitive(value.requestedAt))
                put("userPubKey", JsonPrimitive(value.userPubKey))
                put("otherPubKey", JsonPrimitive(value.otherPubKey))
                put("plaintext", JsonPrimitive(value.plaintext))
            }

            is LocalSignerMethod.DecryptZapEvent -> buildJsonObject {
                put("type", JsonPrimitive(LocalSignerMethodType.DecryptZapEvent.name))
                put("eventId", JsonPrimitive(value.eventId))
                put("packageName", JsonPrimitive(value.packageName))
                put("requestedAt", JsonPrimitive(value.requestedAt))
                put("userPubKey", JsonPrimitive(value.userPubKey))
                val ev =
                    jsonEncoder.json.encodeToJsonElement(NostrEvent.serializer(), value.signedEvent)
                put("signedEvent", ev)
            }
        }

        jsonEncoder.encodeJsonElement(obj)
    }

    override fun deserialize(decoder: Decoder): LocalSignerMethod {
        val jsonDecoder = decoder as? JsonDecoder
            ?: error("LocalSignerMethod supports JSON only")
        val obj = jsonDecoder.decodeJsonElement().jsonObject

        val typeName = obj["type"]?.jsonPrimitive?.content
            ?: throw SerializationException("Missing 'type'")

        val eventId = obj["eventId"]?.jsonPrimitive?.content
            ?: throw SerializationException("Missing 'eventId'")
        val packageName = obj["packageName"]?.jsonPrimitive?.content
            ?: throw SerializationException("Missing 'packageName'")
        val requestedAt = obj["requestedAt"]?.jsonPrimitive?.long
            ?: throw SerializationException("Missing 'requestedAt'")

        return when (typeName) {
            LocalSignerMethodType.GetPublicKey.name -> {
                val perms = obj["permissions"]?.jsonArray ?: buildJsonArray {}
                val permissions = jsonDecoder.json.decodeFromJsonElement(
                    ListSerializer(AppPermission.serializer()),
                    perms,
                )
                LocalSignerMethod.GetPublicKey(
                    eventId = eventId,
                    packageName = packageName,
                    requestedAt = requestedAt,
                    permissions = permissions,
                )
            }

            LocalSignerMethodType.SignEvent.name -> {
                val userPubKey = obj["userPubKey"]?.jsonPrimitive?.content
                    ?: throw SerializationException("Missing 'userPubKey'")
                val unsigned = obj["unsignedEvent"]?.jsonObject
                    ?: throw SerializationException("Missing 'unsignedEvent'")
                val unsignedEvent = jsonDecoder.json.decodeFromJsonElement(
                    NostrUnsignedEvent.serializer(),
                    unsigned,
                )
                LocalSignerMethod.SignEvent(
                    eventId = eventId,
                    packageName = packageName,
                    requestedAt = requestedAt,
                    userPubKey = userPubKey,
                    unsignedEvent = unsignedEvent,
                )
            }

            LocalSignerMethodType.Nip44Decrypt.name -> {
                val userPubKey = obj["userPubKey"]?.jsonPrimitive?.content
                    ?: throw SerializationException("Missing 'userPubKey'")
                val otherPubKey = obj["otherPubKey"]?.jsonPrimitive?.content
                    ?: throw SerializationException("Missing 'otherPubKey'")
                val ciphertext = obj["ciphertext"]?.jsonPrimitive?.content
                    ?: throw SerializationException("Missing 'ciphertext'")
                LocalSignerMethod.Nip44Decrypt(
                    eventId = eventId,
                    packageName = packageName,
                    requestedAt = requestedAt,
                    userPubKey = userPubKey,
                    otherPubKey = otherPubKey,
                    ciphertext = ciphertext,
                )
            }

            LocalSignerMethodType.Nip04Decrypt.name -> {
                val userPubKey = obj["userPubKey"]?.jsonPrimitive?.content
                    ?: throw SerializationException("Missing 'userPubKey'")
                val otherPubKey = obj["otherPubKey"]?.jsonPrimitive?.content
                    ?: throw SerializationException("Missing 'otherPubKey'")
                val ciphertext = obj["ciphertext"]?.jsonPrimitive?.content
                    ?: throw SerializationException("Missing 'ciphertext'")
                LocalSignerMethod.Nip04Decrypt(
                    eventId = eventId,
                    packageName = packageName,
                    requestedAt = requestedAt,
                    userPubKey = userPubKey,
                    otherPubKey = otherPubKey,
                    ciphertext = ciphertext,
                )
            }

            LocalSignerMethodType.Nip44Encrypt.name -> {
                val userPubKey = obj["userPubKey"]?.jsonPrimitive?.content
                    ?: throw SerializationException("Missing 'userPubKey'")
                val otherPubKey = obj["otherPubKey"]?.jsonPrimitive?.content
                    ?: throw SerializationException("Missing 'otherPubKey'")
                val plaintext = obj["plaintext"]?.jsonPrimitive?.content
                    ?: throw SerializationException("Missing 'plaintext'")
                LocalSignerMethod.Nip44Encrypt(
                    eventId = eventId,
                    packageName = packageName,
                    requestedAt = requestedAt,
                    userPubKey = userPubKey,
                    otherPubKey = otherPubKey,
                    plaintext = plaintext,
                )
            }

            LocalSignerMethodType.Nip04Encrypt.name -> {
                val userPubKey = obj["userPubKey"]?.jsonPrimitive?.content
                    ?: throw SerializationException("Missing 'userPubKey'")
                val otherPubKey = obj["otherPubKey"]?.jsonPrimitive?.content
                    ?: throw SerializationException("Missing 'otherPubKey'")
                val plaintext = obj["plaintext"]?.jsonPrimitive?.content
                    ?: throw SerializationException("Missing 'plaintext'")
                LocalSignerMethod.Nip04Encrypt(
                    eventId = eventId,
                    packageName = packageName,
                    requestedAt = requestedAt,
                    userPubKey = userPubKey,
                    otherPubKey = otherPubKey,
                    plaintext = plaintext,
                )
            }

            LocalSignerMethodType.DecryptZapEvent.name -> {
                val userPubKey = obj["userPubKey"]?.jsonPrimitive?.content
                    ?: throw SerializationException("Missing 'userPubKey'")
                val evObj = obj["signedEvent"]?.jsonObject
                    ?: throw SerializationException("Missing 'signedEvent'")
                val signedEvent = jsonDecoder.json.decodeFromJsonElement(NostrEvent.serializer(), evObj)
                LocalSignerMethod.DecryptZapEvent(
                    eventId = eventId,
                    packageName = packageName,
                    requestedAt = requestedAt,
                    userPubKey = userPubKey,
                    signedEvent = signedEvent,
                )
            }

            else -> throw SerializationException("Unsupported LocalSignerMethod type: $typeName")
        }
    }
}
