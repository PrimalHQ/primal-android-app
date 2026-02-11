package net.primal.data.remote.api.events.model

import net.primal.domain.nostr.NostrEvent

data class EventsNip47Response(
    val nip47Events: List<NostrEvent>,
)
