package net.primal.android.networking.sockets.model

import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.jsonPrimitive
import net.primal.android.nostr.model.NostrEvent
import net.primal.android.nostr.model.NostrEventKind
import net.primal.android.nostr.model.NostrKindEventRange
import net.primal.android.nostr.model.NostrVerb
import net.primal.android.nostr.model.primal.NostrPrimalEvent
import net.primal.android.serialization.NostrJson


fun IncomingMessage.isSafeEvent() = type == NostrVerb.Incoming.EVENT && data != null

fun IncomingMessage.isEose() = type == NostrVerb.Incoming.EOSE

fun IncomingMessage.isNotice() = type == NostrVerb.Incoming.NOTICE

fun IncomingMessage.getMessageNostrEventKind(): NostrEventKind {
    val kind = data?.get("kind")?.jsonPrimitive?.content?.toIntOrNull()
    return if (kind != null) NostrEventKind.valueOf(kind) else NostrEventKind.Unknown
}

fun NostrEventKind.isPrimalEventKind() = value in NostrKindEventRange.PrimalEvents

fun NostrEventKind.isUnknown() = this == NostrEventKind.Unknown

fun NostrEventKind.isNotUnknown() = this != NostrEventKind.Unknown

fun IncomingMessage.dataAsNostrEventOrNull(): NostrEvent? {
    return try {
        if (data != null) NostrJson.decodeFromJsonElement(data) else null
    } catch (error: IllegalArgumentException) {
        null
    }
}

fun IncomingMessage.dataAsNostrPrimalEventOrNull(): NostrPrimalEvent? {
    return try {
        if (data != null) NostrJson.decodeFromJsonElement(data) else null
    } catch (error: IllegalArgumentException) {
        null
    }
}