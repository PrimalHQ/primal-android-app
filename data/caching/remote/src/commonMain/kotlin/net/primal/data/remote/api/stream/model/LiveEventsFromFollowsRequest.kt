package net.primal.data.remote.api.stream.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class LiveEventsFromFollowsRequest(
    @SerialName("user_pubkey") val pubkey: String,
)

@Serializable
data class FindLiveStreamRequestBody(
    @SerialName("host_pubkey") val hostPubkey: String,
    val identifier: String,
)
