package net.primal.data.repository.mappers.remote

import net.primal.data.local.dao.events.EventRelayHints
import net.primal.domain.PrimalEvent
import net.primal.domain.serialization.takeContentOrNull

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
