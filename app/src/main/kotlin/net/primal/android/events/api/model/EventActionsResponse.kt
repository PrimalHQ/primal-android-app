package net.primal.android.events.api.model

import net.primal.domain.PrimalEvent
import net.primal.domain.nostr.NostrEvent

data class EventActionsResponse(
    val profiles: List<NostrEvent>,
    val userScores: PrimalEvent?,
    val userFollowersCount: PrimalEvent?,
    val cdnResources: List<PrimalEvent>,
    val actions: List<NostrEvent>,
    val blossomServers: List<NostrEvent> = emptyList(),
    val primalUserNames: PrimalEvent? = null,
    val primalLegendProfiles: PrimalEvent? = null,
    val primalPremiumInfo: PrimalEvent? = null,
)
