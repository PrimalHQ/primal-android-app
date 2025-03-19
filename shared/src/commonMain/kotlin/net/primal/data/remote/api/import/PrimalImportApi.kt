package net.primal.data.remote.api.import

import net.primal.domain.nostr.NostrEvent

internal interface PrimalImportApi {

    suspend fun importEvents(events: List<NostrEvent>): Boolean
}
