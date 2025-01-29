package net.primal.android.premium.api.model

import net.primal.android.nostr.model.NostrEvent
import net.primal.android.nostr.model.primal.PrimalEvent

data class LegendLeaderboardResponse(
    val orderedLegendLeaderboardEvent: PrimalEvent?,
    val primalPremiumInfoEvents: List<PrimalEvent>,
    val primalLegendProfiles: List<PrimalEvent>,
    val primalUsernames: List<PrimalEvent>,
    val cdnResources: List<PrimalEvent>,
    val userFollowersCounts: List<PrimalEvent>,
    val userScores: List<PrimalEvent>,
    val profileMetadatas: List<NostrEvent>,
)
