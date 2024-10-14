package net.primal.android.explore.api.model

import net.primal.android.nostr.model.NostrEvent
import net.primal.android.nostr.model.primal.PrimalEvent
import net.primal.android.nostr.model.primal.content.ContentPrimalPaging

data class TrendingPeopleResponse(
    val paging: ContentPrimalPaging?,
    val metadata: List<NostrEvent> = emptyList(),
    val cdnResources: List<PrimalEvent> = emptyList(),
    val usersFollowStats: PrimalEvent?,
    val usersScores: PrimalEvent?,
    val usersFollowCount: PrimalEvent?,
    val primalUserNames: List<PrimalEvent> = emptyList(),
)
