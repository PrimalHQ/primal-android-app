package net.primal.android.user.api.model

import kotlinx.serialization.Serializable
import net.primal.domain.PrimalEvent

@Serializable
data class UsersRelaysResponse(
    val cachedRelayListEvents: List<PrimalEvent> = emptyList(),
)
