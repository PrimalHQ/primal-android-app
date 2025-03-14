package net.primal.data.remote.api.feed.model

import net.primal.data.remote.model.ContentPrimalPaging
import net.primal.domain.PrimalEvent
import net.primal.domain.nostr.NostrEvent

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
