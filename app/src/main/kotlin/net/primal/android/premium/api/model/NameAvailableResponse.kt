package net.primal.android.premium.api.model

import kotlinx.serialization.Serializable
import net.primal.android.nostr.model.primal.PrimalEvent

@Serializable
data class NameAvailableResponse(
    val membershipAvailableEvent: PrimalEvent?,
)
