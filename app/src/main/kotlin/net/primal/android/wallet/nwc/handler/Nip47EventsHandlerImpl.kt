package net.primal.android.wallet.nwc.handler

import kotlinx.coroutines.withContext
import net.primal.core.utils.Result
import net.primal.core.utils.coroutines.DispatcherProvider
import net.primal.core.utils.map
import net.primal.data.remote.api.events.EventStatsApi
import net.primal.domain.connections.nostr.handler.Nip47EventsHandler
import net.primal.domain.nostr.NostrEvent

class Nip47EventsHandlerImpl(
    private val eventStatsApi: EventStatsApi,
    private val dispatchers: DispatcherProvider,
) : Nip47EventsHandler {
    override suspend fun fetchNip47Events(eventIds: List<String>): Result<List<NostrEvent>> =
        withContext(dispatchers.io()) {
            eventStatsApi.getNip47Events(eventIds = eventIds).map { it.nip47Events }
        }
}
