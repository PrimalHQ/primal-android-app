package net.primal.android.nostr.repository

import javax.inject.Inject
import kotlinx.coroutines.withContext
import net.primal.android.core.coroutines.CoroutineDispatcherProvider
import net.primal.android.db.PrimalDatabase

class RelayHintsRepository @Inject constructor(
    private val database: PrimalDatabase,
    private val dispatchers: CoroutineDispatcherProvider,
) {
    suspend fun findRelaysByIds(eventIds: List<String>) =
        withContext(dispatchers.io()) {
            database.eventHints().findById(eventIds = eventIds)
        }
}
