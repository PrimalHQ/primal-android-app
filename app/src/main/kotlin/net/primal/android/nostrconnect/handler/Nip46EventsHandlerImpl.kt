package net.primal.android.nostrconnect.handler

import kotlinx.coroutines.withContext
import net.primal.core.utils.Result
import net.primal.core.utils.coroutines.DispatcherProvider
import net.primal.core.utils.map
import net.primal.data.remote.api.events.EventStatsApi
import net.primal.domain.account.handler.Nip46EventsHandler
import net.primal.domain.nostr.NostrEvent

class Nip46EventsHandlerImpl(
    private val eventStatsApi: EventStatsApi,
    private val dispatchers: DispatcherProvider,
) : Nip46EventsHandler {
    override suspend fun fetchNip46Events(eventIds: List<String>): Result<List<NostrEvent>> =
        withContext(dispatchers.io()) {
            eventStatsApi.getNip46Events(eventIds = eventIds).map { it.nip46Events }
        }
}
