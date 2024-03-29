package net.primal.android.user.api.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class FollowListRequestBody(
    val pubkey: String,
    @SerialName("extended_response") val extendedResponse: Boolean = true,
)
