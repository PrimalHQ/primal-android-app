package net.primal.android.feed.api.model

import net.primal.android.nostr.model.NostrEvent
import net.primal.android.nostr.model.primal.NostrPrimalEvent
import net.primal.android.nostr.model.primal.content.ContentPrimalPaging

data class FeedResponse(
    val paging: ContentPrimalPaging?,
    val nostrEvents: List<NostrEvent>,
    val primalEvents: List<NostrPrimalEvent>,
)

