package net.primal.android.networking.primal.api

import net.primal.android.nostr.model.NostrEvent

interface PrimalImportApi {

    suspend fun importEvents(events: List<NostrEvent>): Boolean
}
