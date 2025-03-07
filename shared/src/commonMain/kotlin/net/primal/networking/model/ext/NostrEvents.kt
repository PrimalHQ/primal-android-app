package net.primal.networking.model.ext

import io.github.aakira.napier.Napier
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.decodeFromJsonElement
import net.primal.networking.model.NostrEvent
import net.primal.networking.model.primal.PrimalEvent
import net.primal.networking.model.primal.content.ContentPrimalPaging
import net.primal.serialization.json.NostrJson

fun JsonObject?.asNostrEventOrNull(): NostrEvent? {
    return try {
        if (this != null) NostrJson.decodeFromJsonElement(this) else null
    } catch (error: IllegalArgumentException) {
        Napier.w(error) { "Unable to map as NostrEvent." }
        this?.let(NostrJson::encodeToString)?.let { Napier.w { it } }
        null
    }
}

fun JsonObject?.asPrimalEventOrNull(): PrimalEvent? {
    return try {
        if (this != null) NostrJson.decodeFromJsonElement(this) else null
    } catch (error: IllegalArgumentException) {
        Napier.w(error) { "Unable map as PrimalEvent." }
        this?.let(NostrJson::encodeToString)?.let { Napier.w { it } }
        null
    }
}

fun List<NostrEvent>.orderByPagingIfNotNull(pagingEvent: ContentPrimalPaging?): List<NostrEvent> {
    if (pagingEvent == null) return this

    val eventsMap = this.associateBy { it.id }
    return pagingEvent.elements.mapNotNull { eventsMap[it] }
}
