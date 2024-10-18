package net.primal.android.note.repository

import javax.inject.Inject
import net.primal.android.db.PrimalDatabase

class EventRepository @Inject constructor(
    private val database: PrimalDatabase,
) {

    fun observeEventStats(eventIds: List<String>) = database.eventStats().observeStats(eventIds)

    fun observeUserEventStatus(eventIds: List<String>, userId: String) =
        database.eventUserStats().observeStats(eventIds, userId)
}
