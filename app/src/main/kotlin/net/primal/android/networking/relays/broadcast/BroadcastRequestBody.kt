package net.primal.android.networking.relays.broadcast

import kotlinx.serialization.Serializable
import net.primal.domain.nostr.NostrEvent

@Serializable
data class BroadcastRequestBody(
    val events: List<NostrEvent>,
    val relays: List<String>,
)
