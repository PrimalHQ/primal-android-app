package net.primal.data.remote.api.broadcast.model

import kotlinx.serialization.Serializable
import net.primal.domain.nostr.NostrEvent

@Serializable
data class BroadcastRequestBody(
    val events: List<NostrEvent>,
    val relays: List<String>,
)
