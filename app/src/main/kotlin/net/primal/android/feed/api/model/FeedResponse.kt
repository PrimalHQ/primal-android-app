package net.primal.android.feed.api.model

import net.primal.android.nostr.model.NostrEvent
import net.primal.android.nostr.model.primal.PrimalEvent
import net.primal.android.nostr.model.primal.content.ContentPrimalPaging

data class FeedResponse(
    val paging: ContentPrimalPaging?,
    val metadata: List<NostrEvent>,
    val posts: List<NostrEvent>,
    val reposts: List<NostrEvent>,
    val referencedPosts: List<PrimalEvent>,
    val primalEventStats: List<PrimalEvent>,
    val primalEventUserStats: List<PrimalEvent>,
    val cdnResources: List<PrimalEvent>,
    val primalLinkPreviews: List<PrimalEvent>,
)
