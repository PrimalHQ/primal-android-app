package net.primal.android.networking.sockets

import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import net.primal.android.networking.sockets.model.IncomingMessage
import net.primal.android.nostr.model.NostrVerb
import net.primal.android.serialization.NostrJson
import java.util.UUID

fun String.toIncomingMessage(): IncomingMessage? {
    val jsonArray = NostrJson.decodeFromString<JsonArray>(this)

    val typeElement = jsonArray.elementAtOrNull(0)
    val subscriptionElement = jsonArray.elementAtOrNull(1)
    val dataElement = jsonArray.elementAtOrNull(2)

    if (typeElement == null || subscriptionElement == null) return null

    return IncomingMessage(
        type = typeElement.toIncomingMessageType(),
        subscriptionId = subscriptionElement.toSubscriptionId(),
        data = dataElement?.jsonObject
    )
}

private fun JsonElement.toIncomingMessageType(): NostrVerb.Incoming {
    return when (this.jsonPrimitive.content) {
        "EVENT" -> NostrVerb.Incoming.EVENT
        "EOSE" -> NostrVerb.Incoming.EOSE
        "NOTICE" -> NostrVerb.Incoming.NOTICE
        "OK" -> NostrVerb.Incoming.OK
        else -> NostrVerb.Incoming.NOTICE
    }
}

private fun JsonElement.toSubscriptionId(): UUID {
    return UUID.fromString(this.jsonPrimitive.content)
}
