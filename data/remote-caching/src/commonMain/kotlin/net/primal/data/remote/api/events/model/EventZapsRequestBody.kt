package net.primal.data.remote.api.events.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class EventZapsRequestBody(
    @SerialName("event_id") val eventId: String,
    @SerialName("user_pubkey") val userId: String,
    @SerialName("limit") val limit: Int,
    @SerialName("offset") val offset: Int = 0,
)
