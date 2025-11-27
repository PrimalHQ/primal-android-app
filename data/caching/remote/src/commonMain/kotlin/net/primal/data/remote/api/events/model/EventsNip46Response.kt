package net.primal.data.remote.api.events.model

import kotlinx.serialization.Serializable
import net.primal.domain.nostr.NostrEvent

@Serializable
data class EventsNip46Response(
    val nip46Events: List<NostrEvent>,
)
