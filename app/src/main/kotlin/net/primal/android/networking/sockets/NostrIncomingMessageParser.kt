package net.primal.android.networking.sockets

import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import net.primal.android.core.serialization.json.NostrJson
import net.primal.android.core.serialization.json.decodeFromStringOrNull
import net.primal.android.nostr.ext.asNostrEventOrNull
import net.primal.android.nostr.ext.asPrimalEventOrNull
import net.primal.android.nostr.ext.isNotPrimalEventKind
import net.primal.android.nostr.ext.isNotUnknown
import net.primal.android.nostr.ext.isPrimalEventKind
import net.primal.android.nostr.model.NostrEvent
import net.primal.android.nostr.model.NostrEventKind
import net.primal.android.nostr.model.primal.PrimalEvent
import timber.log.Timber

fun String.parseIncomingMessage(): NostrIncomingMessage? {
    val jsonArray = NostrJson.decodeFromStringOrNull<JsonArray>(this)
    val verbElement = jsonArray?.elementAtOrNull(0) ?: return null

    return try {
        when (verbElement.toIncomingMessageType()) {
            NostrVerb.Incoming.EVENT -> jsonArray.takeAsEventIncomingMessage()
            NostrVerb.Incoming.EOSE -> jsonArray.takeAsEoseIncomingMessage()
            NostrVerb.Incoming.OK -> jsonArray.takeAsOkIncomingMessage()
            NostrVerb.Incoming.NOTICE -> jsonArray.takeAsNoticeIncomingMessage()
            NostrVerb.Incoming.AUTH -> jsonArray.takeAsAuthIncomingMessage()
            NostrVerb.Incoming.COUNT -> jsonArray.takeAsCountIncomingMessage()
            NostrVerb.Incoming.EVENTS -> jsonArray.takeAsEventsIncomingMessage()
        }
    } catch (error: Exception) {
        Timber.w(error)
        null
    }
}

private fun JsonArray.takeAsAuthIncomingMessage(): NostrIncomingMessage? {
    val challenge = elementAtOrNull(1) ?: return null
    return NostrIncomingMessage.AuthMessage(
        challenge = challenge.jsonPrimitive.content,
    )
}

private fun JsonArray.takeAsCountIncomingMessage(): NostrIncomingMessage? {
    val subscriptionId = elementAtOrNull(1)?.toSubscriptionId()
    val count = elementAtOrNull(2)
        ?.jsonObject
        ?.get("count")
        ?.jsonPrimitive?.intOrNull

    return if (subscriptionId != null && count != null) {
        NostrIncomingMessage.CountMessage(
            subscriptionId = subscriptionId,
            count = count,
        )
    } else {
        null
    }
}

private fun JsonArray.takeAsEoseIncomingMessage(): NostrIncomingMessage? {
    val subscriptionElement = elementAtOrNull(1) ?: return null
    return NostrIncomingMessage.EoseMessage(
        subscriptionId = subscriptionElement.toSubscriptionId(),
    )
}

private fun JsonArray.takeAsEventIncomingMessage(): NostrIncomingMessage? {
    val subscriptionId = elementAtOrNull(1)?.toSubscriptionId()
    val event = elementAtOrNull(2)?.jsonObject
    val kind = event?.getMessageNostrEventKind()

    if (subscriptionId == null || kind == null) return null

    val nostrEvent = if (kind.isNotUnknown() && kind.isNotPrimalEventKind()) {
        event.asNostrEventOrNull()
    } else {
        null
    }

    val primalEvent = if (kind.isPrimalEventKind()) {
        event.asPrimalEventOrNull()
    } else {
        null
    }

    return NostrIncomingMessage.EventMessage(
        subscriptionId = subscriptionId,
        nostrEvent = nostrEvent,
        primalEvent = primalEvent,
    )
}

private fun JsonArray.takeAsEventsIncomingMessage(): NostrIncomingMessage? {
    val subscriptionId = elementAtOrNull(1)?.toSubscriptionId()
    val events = elementAtOrNull(2)?.jsonArray

    if (subscriptionId == null || events == null) return null

    val nostrEvents = mutableListOf<NostrEvent>()
    val primalEvents = mutableListOf<PrimalEvent>()

    events.map { it.jsonObject }.forEach { jsonEvent ->
        val kind = jsonEvent.getMessageNostrEventKind()
        when {
            kind.isNotUnknown() && kind.isNotPrimalEventKind() -> {
                val nostrEvent = jsonEvent.asNostrEventOrNull()
                if (nostrEvent != null) {
                    nostrEvents.add(nostrEvent)
                } else {
                    Timber.w("Unable to process as nostr event: $jsonEvent")
                }
            }

            kind.isPrimalEventKind() -> {
                val primalEvent = jsonEvent.asPrimalEventOrNull()
                if (primalEvent != null) {
                    primalEvents.add(primalEvent)
                } else {
                    Timber.w("Unable to process as primal event: $jsonEvent")
                }
            }
        }
    }

    return NostrIncomingMessage.EventsMessage(
        subscriptionId = subscriptionId,
        nostrEvents = nostrEvents,
        primalEvents = primalEvents,
    )
}

private fun JsonObject.getMessageNostrEventKind(): NostrEventKind {
    val kind = this["kind"]?.jsonPrimitive?.content?.toIntOrNull()
    return if (kind != null) NostrEventKind.valueOf(kind) else NostrEventKind.Unknown
}

private fun JsonArray.takeAsNoticeIncomingMessage(): NostrIncomingMessage {
    val subscriptionId = elementAtOrNull(1)?.toSubscriptionId()
    val messageText = elementAtOrNull(2)?.jsonPrimitive?.content
    return NostrIncomingMessage.NoticeMessage(subscriptionId = subscriptionId, message = messageText)
}

private fun JsonArray.takeAsOkIncomingMessage(): NostrIncomingMessage? {
    val eventId = elementAtOrNull(1)?.jsonPrimitive?.content
    val success = elementAtOrNull(2)?.jsonPrimitive?.booleanOrNull
    val message = elementAtOrNull(3)?.jsonPrimitive?.content

    return if (eventId != null && success != null) {
        NostrIncomingMessage.OkMessage(
            eventId = eventId,
            success = success,
            message = message,
        )
    } else {
        null
    }
}

private fun JsonElement.toIncomingMessageType(): NostrVerb.Incoming {
    return when (this.jsonPrimitive.content) {
        "EVENT" -> NostrVerb.Incoming.EVENT
        "EOSE" -> NostrVerb.Incoming.EOSE
        "OK" -> NostrVerb.Incoming.OK
        "AUTH" -> NostrVerb.Incoming.AUTH
        "COUNT" -> NostrVerb.Incoming.COUNT
        "EVENTS" -> NostrVerb.Incoming.EVENTS
        else -> NostrVerb.Incoming.NOTICE
    }
}

private fun JsonElement.toSubscriptionId(): String = this.jsonPrimitive.content
