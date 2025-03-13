package net.primal.android.events.api.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class EventActionsRequestBody(
    @SerialName("event_id") val eventId: String,
    @SerialName("kind") val kind: Int,
    @SerialName("limit") val limit: Int,
    @SerialName("offset") val offset: Int = 0,
)
