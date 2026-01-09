package net.primal.data.remote.api.broadcast

import net.primal.data.remote.api.broadcast.model.BroadcastEventResponse
import net.primal.domain.nostr.NostrEvent

interface BroadcastApi {

    suspend fun broadcastEvents(events: List<NostrEvent>, relays: List<String>): List<BroadcastEventResponse>
}
