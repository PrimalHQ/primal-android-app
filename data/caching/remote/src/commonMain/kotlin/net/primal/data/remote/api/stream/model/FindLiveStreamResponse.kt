package net.primal.data.remote.api.stream.model

import net.primal.domain.nostr.NostrEvent

data class FindLiveStreamResponse(
    val liveActivity: NostrEvent?,
)
