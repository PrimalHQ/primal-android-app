package net.primal.repository.processors.mappers

import net.primal.db.events.EventRelayHints
import net.primal.networking.model.primal.PrimalEvent

fun List<PrimalEvent>.flatMapAsEventHintsPO(): List<EventRelayHints> {
    val map = mutableMapOf<String, String>()
    this.mapNotNull { it.takeContentOrNull<Map<String, String>>() }
        .forEach { map.putAll(it) }

    val eventRelayHints = mutableListOf<EventRelayHints>()
    map.forEach { (eventId, relayHint) ->
        val eventHint = EventRelayHints(eventId = eventId, relays = listOf(relayHint))
        eventRelayHints.add(eventHint)
    }
    return eventRelayHints
}
