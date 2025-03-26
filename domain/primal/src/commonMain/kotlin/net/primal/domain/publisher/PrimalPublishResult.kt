package net.primal.domain.publisher

import net.primal.domain.nostr.NostrEvent

data class PrimalPublishResult(
    val nostrEvent: NostrEvent,
    val imported: Boolean,
)
