package net.primal.data.remote.api.users.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class FollowListRequestBody(
    val pubkey: String,
    @SerialName("extended_response") val extendedResponse: Boolean = true,
)
