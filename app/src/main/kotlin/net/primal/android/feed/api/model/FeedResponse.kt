package net.primal.android.feed.api.model

import net.primal.android.nostr.model.NostrEvent
import net.primal.android.nostr.model.primal.PrimalEvent
import net.primal.android.nostr.model.primal.content.ContentPrimalPaging

data class FeedResponse(
    val paging: ContentPrimalPaging?,

    val allNostrEvents: List<NostrEvent>,
    val metadata: List<NostrEvent>,
    val shortTextNotes: List<NostrEvent>,
    val reposts: List<NostrEvent>,

    val allPrimalEvents: List<PrimalEvent>,
    val eventStats: List<PrimalEvent>,
    val eventUserStats: List<PrimalEvent>,
    val eventResources: List<PrimalEvent>,
    val referencedEvents: List<PrimalEvent>,
)
