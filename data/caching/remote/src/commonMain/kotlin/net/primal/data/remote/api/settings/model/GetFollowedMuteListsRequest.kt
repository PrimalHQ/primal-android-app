package net.primal.data.remote.api.settings.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class GetFollowedMuteListsRequest(
    val pubkey: String,
    @SerialName("extended_response") val extendedResponse: Boolean = false,
)
