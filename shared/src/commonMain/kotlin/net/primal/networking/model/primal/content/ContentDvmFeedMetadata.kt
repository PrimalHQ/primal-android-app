package net.primal.networking.model.primal.content

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ContentDvmFeedMetadata(
    @SerialName("event_id") val eventId: String,
    @SerialName("kind") val kind: String,
)
