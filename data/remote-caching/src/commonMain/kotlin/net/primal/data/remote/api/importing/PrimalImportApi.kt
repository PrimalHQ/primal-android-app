package net.primal.data.remote.api.importing

import net.primal.domain.nostr.NostrEvent

interface PrimalImportApi {

    suspend fun importEvents(events: List<NostrEvent>): Boolean
}
