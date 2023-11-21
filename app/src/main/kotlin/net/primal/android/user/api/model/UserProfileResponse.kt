package net.primal.android.user.api.model

import kotlinx.serialization.Serializable
import net.primal.android.nostr.model.NostrEvent
import net.primal.android.nostr.model.primal.PrimalEvent

@Serializable
data class UserProfileResponse(
    val metadata: NostrEvent? = null,
    val profileStats: PrimalEvent? = null,
    val cdnResources: List<PrimalEvent> = emptyList(),
)
