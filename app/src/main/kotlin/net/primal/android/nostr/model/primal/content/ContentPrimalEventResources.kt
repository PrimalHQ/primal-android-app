package net.primal.android.nostr.model.primal.content

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ContentPrimalEventResources(
    @SerialName("event_id") val eventId: String,
    val resources: List<EventResource>,
    @SerialName("thumbnails") val videoThumbnails: Map<String, String>,
)

@Serializable
data class EventResource(
    val url: String,
    @SerialName("mt") val mimeType: String? = null,
    val variants: List<EventResourceVariant>,
)

@Serializable
data class EventResourceVariant(
    @SerialName("mt") val mimeType: String,
    @SerialName("s") val size: String,
    @SerialName("w") val width: Int,
    @SerialName("h") val height: Int,
    @SerialName("media_url") val mediaUrl: String,
)
