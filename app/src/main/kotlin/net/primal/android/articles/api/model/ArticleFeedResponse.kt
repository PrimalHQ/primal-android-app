package net.primal.android.articles.api.model

import kotlinx.serialization.Serializable
import net.primal.android.nostr.model.NostrEvent
import net.primal.android.nostr.model.primal.PrimalEvent
import net.primal.android.nostr.model.primal.content.ContentPrimalPaging

@Serializable
data class ArticleFeedResponse(
    val paging: ContentPrimalPaging?,
    val metadata: List<NostrEvent>,
    val zaps: List<NostrEvent>,
    val longFormContents: List<NostrEvent>,
    val referencedEvents: List<PrimalEvent>,
    val primalEventStats: List<PrimalEvent>,
    val primalEventUserStats: List<PrimalEvent>,
    val primalUserScores: List<PrimalEvent>,
    val cdnResources: List<PrimalEvent>,
    val primalLinkPreviews: List<PrimalEvent>,
    val primalRelayHints: List<PrimalEvent>,
    val primalLongFormWords: List<PrimalEvent>,
)
