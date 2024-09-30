package net.primal.android.feeds.api

import net.primal.android.nostr.model.NostrEvent
import net.primal.android.nostr.model.primal.PrimalEvent

data class DvmFeedsResponse(
    val scores: List<PrimalEvent> = emptyList(),
    val dvmHandlers: List<NostrEvent> = emptyList(),
    val feedMetadatas: List<PrimalEvent> = emptyList(),
    val feedFollowActions: List<PrimalEvent> = emptyList(),
    val feedUserStats: List<PrimalEvent> = emptyList(),
    val userMetadatas: List<NostrEvent> = emptyList(),
    val cdnResources: List<PrimalEvent> = emptyList(),
    val userScores: List<PrimalEvent> = emptyList(),
)
