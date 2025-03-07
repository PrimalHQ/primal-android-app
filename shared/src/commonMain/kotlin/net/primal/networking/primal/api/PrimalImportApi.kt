package net.primal.networking.primal.api


import net.primal.networking.model.NostrEvent

interface PrimalImportApi {

    
    suspend fun importEvents(events: List<NostrEvent>): Boolean
}
