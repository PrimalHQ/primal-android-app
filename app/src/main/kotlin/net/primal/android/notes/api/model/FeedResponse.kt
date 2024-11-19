package net.primal.android.notes.api.model

import net.primal.android.nostr.model.NostrEvent
import net.primal.android.nostr.model.primal.PrimalEvent
import net.primal.android.nostr.model.primal.content.ContentPrimalPaging

data class FeedResponse(
    val paging: ContentPrimalPaging?,
    val metadata: List<NostrEvent>,
    val posts: List<NostrEvent>,
    val articles: List<NostrEvent>,
    val reposts: List<NostrEvent>,
    val zaps: List<NostrEvent>,
    val referencedEvents: List<PrimalEvent>,
    val primalEventStats: List<PrimalEvent>,
    val primalEventUserStats: List<PrimalEvent>,
    val cdnResources: List<PrimalEvent>,
    val primalLinkPreviews: List<PrimalEvent>,
    val primalRelayHints: List<PrimalEvent>,
    val primalUserNames: PrimalEvent? = null,
    val primalLegendProfiles: PrimalEvent? = null,
)
