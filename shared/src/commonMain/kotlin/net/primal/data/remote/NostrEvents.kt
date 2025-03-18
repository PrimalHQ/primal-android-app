package net.primal.data.remote

import net.primal.data.remote.model.ContentPrimalPaging
import net.primal.domain.nostr.NostrEvent


// TODO Where does this belongs?


fun List<NostrEvent>.orderByPagingIfNotNull(pagingEvent: ContentPrimalPaging?): List<NostrEvent> {
    if (pagingEvent == null) return this

    val eventsMap = this.associateBy { it.id }
    return pagingEvent.elements.mapNotNull { eventsMap[it] }
}
