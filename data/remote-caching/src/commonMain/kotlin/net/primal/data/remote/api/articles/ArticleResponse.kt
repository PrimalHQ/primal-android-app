package net.primal.data.remote.api.articles

import kotlinx.serialization.Serializable
import net.primal.data.remote.model.ContentPrimalPaging
import net.primal.domain.PrimalEvent
import net.primal.domain.nostr.NostrEvent

@Serializable
data class ArticleResponse(
    val paging: ContentPrimalPaging?,
    val metadata: List<NostrEvent>,
    val zaps: List<NostrEvent>,
    val notes: List<NostrEvent>,
    val articles: List<NostrEvent>,
    val primalUserScores: List<PrimalEvent>,
    val referencedEvents: List<PrimalEvent>,
    val primalEventStats: List<PrimalEvent>,
    val primalEventUserStats: List<PrimalEvent>,
    val cdnResources: List<PrimalEvent>,
    val primalLinkPreviews: List<PrimalEvent>,
    val primalRelayHints: List<PrimalEvent>,
    val primalLongFormWords: List<PrimalEvent>,
    val primalUserNames: PrimalEvent?,
    val primalLegendProfiles: PrimalEvent?,
    val primalPremiumInfo: PrimalEvent?,
    val blossomServers: List<NostrEvent>,
)
