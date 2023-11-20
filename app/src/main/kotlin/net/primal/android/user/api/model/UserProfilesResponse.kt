package net.primal.android.user.api.model

import kotlinx.serialization.Serializable
import net.primal.android.nostr.model.NostrEvent
import net.primal.android.nostr.model.primal.PrimalEvent

@Serializable
data class UserProfilesResponse(
    val metadataEvents: List<NostrEvent> = emptyList(),
    val cdnResources: List<PrimalEvent> = emptyList(),
    val userScores: PrimalEvent? = null,
)
