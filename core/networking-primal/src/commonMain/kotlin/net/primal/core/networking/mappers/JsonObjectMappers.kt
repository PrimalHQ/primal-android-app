package net.primal.core.networking.mappers

import io.github.aakira.napier.Napier
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.decodeFromJsonElement
import net.primal.core.networking.serialization.SocketsJson
import net.primal.domain.common.PrimalEvent
import net.primal.domain.nostr.NostrEvent

fun JsonObject?.asNostrEventOrNull(): NostrEvent? {
    return try {
        if (this != null) SocketsJson.decodeFromJsonElement(this) else null
    } catch (error: IllegalArgumentException) {
        Napier.w(error) { "Unable to map as NostrEvent." }
        this?.let(SocketsJson::encodeToString)?.let { Napier.w { it } }
        null
    }
}

fun JsonObject?.asPrimalEventOrNull(): PrimalEvent? {
    return try {
        if (this != null) SocketsJson.decodeFromJsonElement(this) else null
    } catch (error: IllegalArgumentException) {
        Napier.w(error) { "Unable map as PrimalEvent." }
        this?.let(SocketsJson::encodeToString)?.let { Napier.w { it } }
        null
    }
}
