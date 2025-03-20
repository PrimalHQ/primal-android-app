package net.primal.data.remote.api.users.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UserProfileFollowedByRequestBody(
    @SerialName("pubkey") val profileId: String,
    @SerialName("user_pubkey") val userId: String,
    val limit: Int = 5,
)
