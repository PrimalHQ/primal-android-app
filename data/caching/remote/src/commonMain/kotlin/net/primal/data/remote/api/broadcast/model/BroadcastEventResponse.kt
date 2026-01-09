package net.primal.data.remote.api.broadcast.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class BroadcastEventResponse(
    @SerialName("event_id") val eventId: String,
    val responses: List<List<String>> = emptyList(),
)
