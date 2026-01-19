package net.primal.domain.account.handler

import net.primal.core.utils.Result
import net.primal.domain.nostr.NostrEvent

interface Nip46EventsHandler {
    suspend fun fetchNip46Events(eventIds: List<String>): Result<List<NostrEvent>>
}
