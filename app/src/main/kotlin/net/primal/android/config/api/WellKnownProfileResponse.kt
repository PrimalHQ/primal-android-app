package net.primal.android.config.api

import kotlinx.serialization.Serializable

@Serializable
data class WellKnownProfileResponse(
    val names: Map<String, String>,
)
