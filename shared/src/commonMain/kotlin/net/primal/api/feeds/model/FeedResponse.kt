package net.primal.api.feeds.model

import net.primal.networking.model.NostrEvent
import net.primal.networking.model.primal.PrimalEvent
import net.primal.networking.model.primal.content.ContentPrimalPaging

data class FeedResponse(
    val paging: ContentPrimalPaging?,
    val metadata: List<NostrEvent>,
    val notes: List<NostrEvent>,
    val articles: List<NostrEvent>,
    val reposts: List<NostrEvent>,
    val zaps: List<NostrEvent>,
    val referencedEvents: List<PrimalEvent>,
    val primalEventStats: List<PrimalEvent>,
    val primalEventUserStats: List<PrimalEvent>,
    val cdnResources: List<PrimalEvent>,
    val primalLinkPreviews: List<PrimalEvent>,
    val primalRelayHints: List<PrimalEvent>,
    val blossomServers: List<NostrEvent>,
    val primalUserNames: PrimalEvent? = null,
    val primalLegendProfiles: PrimalEvent? = null,
    val primalPremiumInfo: PrimalEvent? = null,
    val genericReposts: List<NostrEvent> = emptyList(),
    val pictureNotes: List<NostrEvent> = emptyList(),
)
