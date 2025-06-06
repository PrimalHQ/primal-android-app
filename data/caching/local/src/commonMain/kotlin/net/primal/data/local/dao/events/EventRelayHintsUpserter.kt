package net.primal.data.local.dao.events

suspend fun eventRelayHintsUpserter(
    dao: EventRelayHintsDao,
    eventIds: List<String>,
    reducer: EventRelayHints.() -> EventRelayHints,
) {
    val existingEventHintsMap = dao.findById(eventIds = eventIds).associateBy { it.eventId }

    val updates = mutableListOf<EventRelayHints>()
    val inserts = mutableListOf<EventRelayHints>()
    eventIds.forEach { eventId ->
        val existing = existingEventHintsMap[eventId]
        if (existing != null) {
            updates.add(existing.reducer())
        } else {
            inserts.add(EventRelayHints(eventId = eventId).reducer())
        }
    }
    dao.update(data = updates)
    dao.insert(data = inserts)
}
