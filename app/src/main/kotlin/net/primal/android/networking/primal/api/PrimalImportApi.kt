package net.primal.android.networking.primal.api

import net.primal.domain.nostr.NostrEvent

interface PrimalImportApi {

    suspend fun importEvents(events: List<NostrEvent>): Boolean
}
