package net.primal.android.user.api.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UserProfileFollowedByRequestBody(
    @SerialName("pubkey") val userProfileId: String,
    @SerialName("user_pubkey") val userId: String,
    val limit: Int = 5,
)
