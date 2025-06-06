package net.primal.data.remote.api.explore.model

import net.primal.domain.common.ContentPrimalPaging
import net.primal.domain.common.PrimalEvent
import net.primal.domain.nostr.NostrEvent

data class TrendingZapsResponse(
    val paging: ContentPrimalPaging?,
    val metadata: List<NostrEvent> = emptyList(),
    val cdnResources: List<PrimalEvent> = emptyList(),
    val primalLinkPreviews: List<PrimalEvent> = emptyList(),
    val usersScores: PrimalEvent? = null,
    val nostrZapEvents: List<NostrEvent> = emptyList(),
    val noteEvents: List<NostrEvent> = emptyList(),
    val primalUserNames: PrimalEvent? = null,
    val primalLegendProfiles: PrimalEvent? = null,
    val primalPremiumInfo: PrimalEvent? = null,
    val blossomServers: List<NostrEvent> = emptyList(),
)
