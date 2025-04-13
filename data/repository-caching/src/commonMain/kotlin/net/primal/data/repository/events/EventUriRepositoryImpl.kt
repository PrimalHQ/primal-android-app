package net.primal.data.repository.events

import kotlinx.coroutines.withContext
import net.primal.core.utils.coroutines.DispatcherProvider
import net.primal.data.local.db.PrimalDatabase
import net.primal.data.repository.mappers.local.asEventLinkDO
import net.primal.domain.links.EventLink
import net.primal.domain.links.EventUriRepository
import net.primal.domain.links.EventUriType

class EventUriRepositoryImpl(
    private val dispatcherProvider: DispatcherProvider,
    private val database: PrimalDatabase,
) : EventUriRepository {

    override suspend fun loadEventLinks(noteId: String, types: List<EventUriType>): List<EventLink> {
        return withContext(dispatcherProvider.io()) {
            database.eventUris()
                .loadEventUris(noteId = noteId, types = types)
                .map { it.asEventLinkDO() }
        }
    }
}
