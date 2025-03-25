package net.primal.android.events.repository

import javax.inject.Inject
import kotlinx.coroutines.withContext
import net.primal.android.core.coroutines.CoroutineDispatcherProvider
import net.primal.android.db.PrimalDatabase
import net.primal.android.events.db.EventUri
import net.primal.domain.EventUriType

class EventUriRepository @Inject constructor(
    private val dispatchers: CoroutineDispatcherProvider,
    private val database: PrimalDatabase,
) {

    suspend fun loadEventUris(noteId: String, types: List<EventUriType>): List<EventUri> {
        return withContext(dispatchers.io()) {
            database.eventUris().loadEventUris(noteId = noteId, types = types)
        }
    }
}
