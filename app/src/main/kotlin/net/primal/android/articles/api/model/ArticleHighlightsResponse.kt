package net.primal.android.articles.api.model

import kotlinx.serialization.Serializable
import net.primal.domain.PrimalEvent
import net.primal.domain.nostr.NostrEvent

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
    val blossomServers: List<NostrEvent>,
)
