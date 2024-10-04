package net.primal.android.explore.api.model

import net.primal.android.nostr.model.NostrEvent
import net.primal.android.nostr.model.primal.PrimalEvent

data class TrendingPeoplesResponse(
    val metadatas: List<NostrEvent> = emptyList(),
    val cdnResources: List<PrimalEvent> = emptyList(),
    val usersFollowStats: PrimalEvent?,
    val usersScores: PrimalEvent?,
    val usersFollowCount: PrimalEvent?,
    val primalPaging: PrimalEvent?,
)
