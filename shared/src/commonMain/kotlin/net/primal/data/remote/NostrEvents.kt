package net.primal.data.remote

import io.github.aakira.napier.Napier
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.decodeFromJsonElement
import net.primal.data.remote.model.ContentPrimalPaging
import net.primal.data.serialization.NostrJson
import net.primal.domain.PrimalEvent
import net.primal.domain.nostr.NostrEvent


// TODO Where does this belongs?

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
