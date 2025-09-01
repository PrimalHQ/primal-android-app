package net.primal.data.remote.api.stream.model

import kotlinx.serialization.Serializable
import net.primal.domain.nostr.NostrEvent

@Serializable
data class LiveEventsFromFollowsResponse(
    val liveActivity: List<NostrEvent>,
)
