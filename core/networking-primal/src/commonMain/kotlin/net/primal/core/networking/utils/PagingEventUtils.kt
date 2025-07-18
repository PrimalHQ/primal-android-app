package net.primal.core.networking.utils

import net.primal.domain.common.ContentPrimalPaging
import net.primal.domain.nostr.NostrEvent

fun List<NostrEvent>.orderByPagingIfNotNull(pagingEvent: ContentPrimalPaging?): List<NostrEvent> {
    if (pagingEvent == null) return this

    val eventsMap = this.associateBy { it.id }
    return pagingEvent.elements.mapNotNull { eventsMap[it] }
}

fun <T> List<T>.orderByPagingIfNotNull(pagingEvent: ContentPrimalPaging?, keySelector: T.() -> String): List<T> {
    if (pagingEvent == null) return this

    val map = this.associateBy(keySelector = keySelector)
    return pagingEvent.elements.mapNotNull { map[it] }
}
