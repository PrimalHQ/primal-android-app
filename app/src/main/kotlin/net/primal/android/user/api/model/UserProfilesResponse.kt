package net.primal.android.user.api.model

import kotlinx.serialization.Serializable
import net.primal.domain.PrimalEvent
import net.primal.domain.nostr.NostrEvent

@Serializable
data class UserProfilesResponse(
    val metadataEvents: List<NostrEvent> = emptyList(),
    val cdnResources: List<PrimalEvent> = emptyList(),
    val blossomServers: List<NostrEvent> = emptyList(),
    val userScores: PrimalEvent? = null,
    val primalUserNames: PrimalEvent? = null,
    val primalLegendProfiles: PrimalEvent? = null,
    val primalPremiumInfo: PrimalEvent? = null,
)
