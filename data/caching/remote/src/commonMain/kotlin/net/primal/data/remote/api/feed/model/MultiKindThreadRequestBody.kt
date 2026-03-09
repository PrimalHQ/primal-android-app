package net.primal.data.remote.api.feed.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class MultiKindThreadRequestBody(
    @SerialName("event_id") val eventId: String,
    @SerialName("user_pubkey") val userPubKey: String,
    @SerialName("kinds") val kinds: List<Int>,
    @SerialName("limit") val limit: Int,
)
