package net.primal.data.remote.api.stream.model

import net.primal.domain.nostr.NostrEvent

data class LiveFeedResponse(
    val zaps: List<NostrEvent> = emptyList(),
)
