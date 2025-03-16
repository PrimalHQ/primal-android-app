package net.primal.android.user.api.model

import kotlinx.serialization.Serializable
import net.primal.domain.PrimalEvent
import net.primal.domain.nostr.NostrEvent

@Serializable
data class UserProfileResponse(
    val metadata: NostrEvent? = null,
    val profileStats: PrimalEvent? = null,
    val cdnResources: List<PrimalEvent> = emptyList(),
    val primalUserNames: PrimalEvent? = null,
    val primalLegendProfiles: PrimalEvent? = null,
    val primalPremiumInfo: PrimalEvent? = null,
    val blossomServers: List<NostrEvent> = emptyList(),
)
