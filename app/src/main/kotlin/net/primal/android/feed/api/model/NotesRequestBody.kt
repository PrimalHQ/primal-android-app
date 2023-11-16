package net.primal.android.feed.api.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class NotesRequestBody(
    @SerialName("event_ids") val noteIds: List<String>,
    @SerialName("extended_response") val extendedResponse: Boolean = true,
)
