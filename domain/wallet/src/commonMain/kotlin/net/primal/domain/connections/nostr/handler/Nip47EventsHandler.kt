package net.primal.domain.connections.nostr.handler

import net.primal.core.utils.Result
import net.primal.domain.nostr.NostrEvent

interface Nip47EventsHandler {
    suspend fun fetchNip47Events(eventIds: List<String>): Result<List<NostrEvent>>
}
