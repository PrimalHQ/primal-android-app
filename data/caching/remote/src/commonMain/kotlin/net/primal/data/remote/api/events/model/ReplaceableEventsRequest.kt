package net.primal.data.remote.api.events.model

import kotlinx.serialization.Serializable

@Serializable
data class ReplaceableEventsRequest(
    val events: List<ReplaceableEventRequest>,
)
