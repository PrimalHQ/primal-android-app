package net.primal.data.repository.events

import kotlinx.coroutines.withContext
import net.primal.core.utils.coroutines.DispatcherProvider
import net.primal.data.local.db.PrimalDatabase
import net.primal.data.repository.mappers.local.asEventRelayHintsDO
import net.primal.domain.events.EventRelayHintsRepository

class EventRelayHintsRepositoryImpl(
    private val dispatcherProvider: DispatcherProvider,
    private val database: PrimalDatabase,
) : EventRelayHintsRepository {

    override suspend fun findRelaysByIds(eventIds: List<String>) =
        withContext(dispatcherProvider.io()) {
            database.eventHints().findById(eventIds = eventIds)
                .map { it.asEventRelayHintsDO() }
        }
}
