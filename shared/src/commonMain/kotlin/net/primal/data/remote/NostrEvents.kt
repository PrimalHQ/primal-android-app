package net.primal.data.remote

import io.github.aakira.napier.Napier
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.decodeFromJsonElement
import net.primal.data.remote.model.ContentPrimalPaging
import net.primal.data.serialization.NostrJson
import net.primal.domain.PrimalEvent
import net.primal.domain.nostr.NostrEvent


// TODO Where does this belongs?


fun List<NostrEvent>.orderByPagingIfNotNull(pagingEvent: ContentPrimalPaging?): List<NostrEvent> {
    if (pagingEvent == null) return this

    val eventsMap = this.associateBy { it.id }
    return pagingEvent.elements.mapNotNull { eventsMap[it] }
}
