package net.primal.android.nostr.ext

import net.primal.android.nostr.db.EventHints
import net.primal.android.nostr.model.primal.PrimalEvent

fun PrimalEvent?.flatMapAsEventHintsPO(): List<EventHints> {
    val map = this.takeContentOrNull<Map<String, String>>() ?: return emptyList()
    val eventHints = mutableListOf<EventHints>()
    map.forEach { (eventId, relayHint) ->
        val eventHint = EventHints(eventId = eventId, relays = listOf(relayHint))
        eventHints.add(eventHint)
    }
    return eventHints
}
