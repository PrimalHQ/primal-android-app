package net.primal.android.profile.api

import kotlinx.serialization.Serializable

@Serializable
data class ProfileWellKnownProfileResponse(
    val names: Map<String, String>,
)
