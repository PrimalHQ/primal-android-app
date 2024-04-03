package net.primal.android.nostr.ext

import net.primal.android.nostr.db.EventHints
import net.primal.android.nostr.model.primal.PrimalEvent

fun List<PrimalEvent>.flatMapAsEventHintsPO(): List<EventHints> {
    val map = mutableMapOf<String, String>()
    this.mapNotNull { it.takeContentOrNull<Map<String, String>>() }
        .forEach { map.putAll(it) }

    val eventHints = mutableListOf<EventHints>()
    map.forEach { (eventId, relayHint) ->
        val eventHint = EventHints(eventId = eventId, relays = listOf(relayHint))
        eventHints.add(eventHint)
    }
    return eventHints
}
