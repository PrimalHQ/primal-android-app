package net.primal.data.remote.api.events.model

import net.primal.data.remote.model.ContentPrimalPaging
import net.primal.domain.PrimalEvent
import net.primal.domain.nostr.NostrEvent

data class EventZapsResponse(
    val paging: ContentPrimalPaging?,
    val zaps: List<NostrEvent>,
    val profiles: List<NostrEvent>,
    val userScores: PrimalEvent?,
    val cdnResources: List<PrimalEvent>,
    val blossomServers: List<NostrEvent> = emptyList(),
    val primalUserNames: PrimalEvent? = null,
    val primalLegendProfiles: PrimalEvent? = null,
    val primalPremiumInfo: PrimalEvent? = null,
)
