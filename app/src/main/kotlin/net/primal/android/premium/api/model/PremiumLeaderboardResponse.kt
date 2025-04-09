package net.primal.android.premium.api.model

import net.primal.domain.PrimalEvent
import net.primal.domain.nostr.NostrEvent

data class PremiumLeaderboardResponse(
    val nostrEvents: List<NostrEvent>,
    val primalEvents: List<PrimalEvent>,
    val orderedPremiumLeaderboardEvent: PrimalEvent?,
    val primalPremiumInfoEvents: List<PrimalEvent>,
    val primalUsernames: List<PrimalEvent>,
    val cdnResources: List<PrimalEvent>,
    val userFollowersCounts: List<PrimalEvent>,
    val userScores: List<PrimalEvent>,
    val profileMetadatas: List<NostrEvent>,
)
