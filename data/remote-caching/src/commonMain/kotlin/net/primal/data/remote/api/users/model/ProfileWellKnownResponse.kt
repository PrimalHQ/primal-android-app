package net.primal.data.remote.api.users.model

import kotlinx.serialization.Serializable

@Serializable
data class ProfileWellKnownResponse(
    val names: Map<String, String>,
)
