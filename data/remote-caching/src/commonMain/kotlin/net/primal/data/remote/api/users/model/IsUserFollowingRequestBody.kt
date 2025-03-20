package net.primal.data.remote.api.users.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class IsUserFollowingRequestBody(
    @SerialName("user_pubkey") val userId: String,
    @SerialName("pubkey") val targetUserId: String,
)
