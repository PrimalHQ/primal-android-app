package net.primal.data.remote.api.events.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class EventsNip47Request(
    @SerialName("event_ids") val eventIds: List<String>,
)
