package net.primal.android.user.api.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class IsUserFollowingRequestBody(
    @SerialName("user_pubkey") val userId: String,
    @SerialName("pubkey") val targetUserId: String,
)
