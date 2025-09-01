package net.primal.data.remote.api.stream.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class LiveEventsFromFollowsRequest(
    @SerialName("user_pubkey") val pubkey: String,
)
