package net.primal.android.nostr.db

suspend fun eventHintsUpserter(
    dao: EventHintsDao,
    eventId: String,
    reducer: EventHints.() -> EventHints,
) {
    val existingEventHints = dao.findById(eventId = eventId)
    val updated = (existingEventHints ?: EventHints(eventId = eventId)).reducer()
    if (existingEventHints != null) {
        dao.update(data = updated)
    } else {
        dao.insert(data = updated)
    }
}

suspend fun eventHintsUpserter(
    dao: EventHintsDao,
    eventIds: List<String>,
    reducer: EventHints.() -> EventHints,
) {
    val existingEventHintsMap = dao.findById(eventIds = eventIds).associateBy { it.eventId }

    val updates = mutableListOf<EventHints>()
    val inserts = mutableListOf<EventHints>()
    eventIds.forEach { eventId ->
        val existing = existingEventHintsMap[eventId]
        if (existing != null) {
            updates.add(existing.reducer())
        } else {
            inserts.add(EventHints(eventId = eventId).reducer())
        }
    }
    dao.update(data = updates)
    dao.insert(data = inserts)
}
