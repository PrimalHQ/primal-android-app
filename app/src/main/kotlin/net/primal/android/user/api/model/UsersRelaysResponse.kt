package net.primal.android.user.api.model

import kotlinx.serialization.Serializable
import net.primal.android.nostr.model.primal.PrimalEvent

@Serializable
data class UsersRelaysResponse(
    val cachedRelayListEvents: List<PrimalEvent> = emptyList(),
)
