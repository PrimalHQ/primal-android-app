package net.primal.android.premium.api.model

import kotlinx.serialization.Serializable

@Serializable
data class NameAvailableResponse(
    val available: Boolean,
)
