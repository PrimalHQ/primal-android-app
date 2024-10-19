package net.primal.android.stats.api.model

import net.primal.android.nostr.model.NostrEvent
import net.primal.android.nostr.model.primal.PrimalEvent

data class EventActionsResponse(
    val profiles: List<NostrEvent>,
    val userScores: PrimalEvent?,
    val userFollowersCount: PrimalEvent?,
    val cdnResources: List<PrimalEvent>,
    val primalUserNames: List<PrimalEvent>,
)
