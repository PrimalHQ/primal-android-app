package net.primal.android.stats.api.model

import net.primal.android.nostr.model.NostrEvent
import net.primal.android.nostr.model.primal.PrimalEvent
import net.primal.android.nostr.model.primal.content.ContentPrimalPaging

data class EventZapsResponse(
    val paging: ContentPrimalPaging?,
    val zaps: List<NostrEvent>,
    val profiles: List<NostrEvent>,
    val userScores: PrimalEvent?,
    val cdnResources: List<PrimalEvent>,
)
