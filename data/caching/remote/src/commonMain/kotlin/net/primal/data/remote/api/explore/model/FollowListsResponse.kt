package net.primal.data.remote.api.explore.model

import net.primal.domain.common.ContentPrimalPaging
import net.primal.domain.common.PrimalEvent
import net.primal.domain.nostr.NostrEvent

data class FollowListsResponse(
    val pagingEvent: ContentPrimalPaging?,
    val metadata: List<NostrEvent>,
    val primalUserNames: PrimalEvent?,
    val primalPremiumInfo: PrimalEvent?,
    val primalLegendProfiles: PrimalEvent?,
    val followListEvents: List<NostrEvent>,
    val primalUserScores: PrimalEvent?,
    val primalUserFollowersCounts: PrimalEvent?,
    val blossomServers: List<NostrEvent>,
    val cdnResources: List<PrimalEvent>,
)
