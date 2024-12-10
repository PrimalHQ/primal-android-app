package net.primal.android.explore.api.model

import net.primal.android.nostr.model.NostrEvent
import net.primal.android.nostr.model.primal.PrimalEvent
import net.primal.android.nostr.model.primal.content.ContentPrimalPaging

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
)
