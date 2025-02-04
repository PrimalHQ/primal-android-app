package net.primal.android.explore.api.model

import net.primal.android.nostr.model.NostrEvent
import net.primal.android.nostr.model.primal.PrimalEvent

data class UsersResponse(
    val contactsMetadata: List<NostrEvent> = emptyList(),
    val userScores: PrimalEvent?,
    val followerCounts: PrimalEvent?,
    val cdnResources: List<PrimalEvent> = emptyList(),
    val primalUserNames: PrimalEvent? = null,
    val primalLegendProfiles: PrimalEvent? = null,
    val primalPremiumInfo: PrimalEvent? = null,
    val blossomServers: List<NostrEvent> = emptyList(),
)
