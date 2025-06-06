package net.primal.data.remote.api.feeds.model

import net.primal.domain.common.PrimalEvent
import net.primal.domain.nostr.NostrEvent

data class DvmFeedsResponse(
    val scores: List<PrimalEvent> = emptyList(),
    val dvmHandlers: List<NostrEvent> = emptyList(),
    val feedMetadata: List<PrimalEvent> = emptyList(),
    val feedFollowActions: List<PrimalEvent> = emptyList(),
    val feedUserStats: List<PrimalEvent> = emptyList(),
    val userMetadata: List<NostrEvent> = emptyList(),
    val cdnResources: List<PrimalEvent> = emptyList(),
    val userScores: List<PrimalEvent> = emptyList(),
    val primalUserNames: PrimalEvent? = null,
    val primalLegendProfiles: PrimalEvent? = null,
    val primalPremiumInfo: PrimalEvent? = null,
    val blossomServers: List<NostrEvent> = emptyList(),
)
