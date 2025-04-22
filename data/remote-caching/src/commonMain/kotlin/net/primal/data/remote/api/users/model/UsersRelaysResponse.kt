package net.primal.data.remote.api.users.model

import kotlinx.serialization.Serializable
import net.primal.domain.common.PrimalEvent

@Serializable
data class UsersRelaysResponse(
    val cachedRelayListEvents: List<PrimalEvent> = emptyList(),
)
