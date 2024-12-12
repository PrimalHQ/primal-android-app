package net.primal.android.articles.api.model

import kotlinx.serialization.Serializable
import net.primal.android.nostr.model.NostrEvent
import net.primal.android.nostr.model.primal.PrimalEvent

@Serializable
data class ArticleHighlightsResponse(
    val highlights: List<NostrEvent>,
    val legendProfiles: PrimalEvent?,
    val primalPremiumInfo: PrimalEvent?,
    val primalUserNames: PrimalEvent?,
    val primalUserScores: PrimalEvent?,
    val cdnResources: List<PrimalEvent>,
    val profileMetadatas: List<NostrEvent>,
    val eventStats: List<PrimalEvent>,
    val relayHints: List<PrimalEvent>,
    val highlightComments: List<NostrEvent>,
    val zaps: List<NostrEvent>,
    val primalLongFormContentWordsCount: List<PrimalEvent>,
    val referencedEvents: List<PrimalEvent>,
)
