package net.primal.android.premium.api.model

import net.primal.android.nostr.model.NostrEvent
import net.primal.android.nostr.model.primal.PrimalEvent

data class PremiumLeaderboardResponse(
    val orderedPremiumLeaderboardEvent: PrimalEvent?,
    val primalPremiumInfoEvents: List<PrimalEvent>,
    val primalUsernames: List<PrimalEvent>,
    val cdnResources: List<PrimalEvent>,
    val userFollowersCounts: List<PrimalEvent>,
    val userScores: List<PrimalEvent>,
    val profileMetadatas: List<NostrEvent>,
)
