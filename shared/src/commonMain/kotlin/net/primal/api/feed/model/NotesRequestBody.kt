package net.primal.api.feed.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class NotesRequestBody(
    @SerialName("event_ids") val noteIds: List<String>,
    @SerialName("extended_response") val extendedResponse: Boolean = true,
)
