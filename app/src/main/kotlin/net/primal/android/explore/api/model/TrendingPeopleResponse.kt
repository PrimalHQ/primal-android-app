package net.primal.android.explore.api.model

import net.primal.android.nostr.model.primal.content.ContentPrimalPaging
import net.primal.domain.PrimalEvent
import net.primal.domain.nostr.NostrEvent

data class TrendingPeopleResponse(
    val paging: ContentPrimalPaging?,
    val metadata: List<NostrEvent> = emptyList(),
    val cdnResources: List<PrimalEvent> = emptyList(),
    val usersFollowStats: PrimalEvent?,
    val usersScores: PrimalEvent?,
    val usersFollowCount: PrimalEvent?,
    val primalUserNames: PrimalEvent? = null,
    val primalLegendProfiles: PrimalEvent? = null,
    val primalPremiumInfo: PrimalEvent? = null,
    val blossomServers: List<NostrEvent> = emptyList(),
)
