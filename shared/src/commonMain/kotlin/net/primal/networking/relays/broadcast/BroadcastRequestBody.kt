package net.primal.networking.relays.broadcast

import kotlinx.serialization.Serializable
import net.primal.networking.model.NostrEvent

@Serializable
data class BroadcastRequestBody(
    val events: List<NostrEvent>,
    val relays: List<String>,
)
