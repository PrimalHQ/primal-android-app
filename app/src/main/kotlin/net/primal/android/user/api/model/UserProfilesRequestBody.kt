package net.primal.android.user.api.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UserProfilesRequestBody(
    @SerialName("pubkeys") val userIds: Set<String> = emptySet(),
)
