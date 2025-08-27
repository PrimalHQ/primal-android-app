package net.primal.data.remote.api.events.model

import net.primal.domain.common.PrimalEvent
import net.primal.domain.nostr.NostrEvent

data class ReplaceableEventResponse(
    val metadata: List<NostrEvent>,
    val articles: List<NostrEvent>,
    val liveActivity: List<NostrEvent>,
    val cdnResources: List<PrimalEvent>,
    val blossomServers: List<NostrEvent> = emptyList(),
    val eventStats: List<PrimalEvent> = emptyList(),
    val wordCount: List<PrimalEvent> = emptyList(),
    val relayHints: List<PrimalEvent> = emptyList(),
    val primalUserNames: PrimalEvent? = null,
    val primalLegendProfiles: PrimalEvent? = null,
    val primalPremiumInfo: PrimalEvent? = null,
)
