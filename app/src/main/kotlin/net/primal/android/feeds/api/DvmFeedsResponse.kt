package net.primal.android.feeds.api

import net.primal.android.nostr.model.NostrEvent
import net.primal.android.nostr.model.primal.PrimalEvent

data class DvmFeedsResponse(
    val scores: List<PrimalEvent> = emptyList(),
    val dvmHandlers: List<NostrEvent> = emptyList(),
)
