package net.primal.data.remote.api.users.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UserProfilesRequestBody(
    @SerialName("pubkeys") val userIds: Set<String> = emptySet(),
)
