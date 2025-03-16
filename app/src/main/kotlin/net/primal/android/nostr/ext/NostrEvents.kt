package net.primal.android.nostr.ext

import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.decodeFromJsonElement
import net.primal.android.core.serialization.json.NostrJson
import net.primal.android.nostr.model.primal.content.ContentPrimalPaging
import net.primal.domain.PrimalEvent
import net.primal.domain.nostr.NostrEvent
import timber.log.Timber

fun JsonObject?.asNostrEventOrNull(): NostrEvent? {
    return try {
        if (this != null) NostrJson.decodeFromJsonElement(this) else null
    } catch (error: IllegalArgumentException) {
        Timber.w(error)
        Timber.w(this?.let(NostrJson::encodeToString))
        null
    }
}

fun JsonObject?.asPrimalEventOrNull(): PrimalEvent? {
    return try {
        if (this != null) NostrJson.decodeFromJsonElement(this) else null
    } catch (error: IllegalArgumentException) {
        Timber.w(error)
        Timber.w(this?.let(NostrJson::encodeToString))
        null
    }
}

fun List<NostrEvent>.orderByPagingIfNotNull(pagingEvent: ContentPrimalPaging?): List<NostrEvent> {
    if (pagingEvent == null) return this

    val eventsMap = this.associateBy { it.id }
    return pagingEvent.elements.mapNotNull { eventsMap[it] }
}
