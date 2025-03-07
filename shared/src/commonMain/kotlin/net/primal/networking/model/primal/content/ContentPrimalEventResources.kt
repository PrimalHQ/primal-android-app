package net.primal.networking.model.primal.content

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ContentPrimalEventResources(
    val resources: List<EventResource>,
    @SerialName("thumbnails") val videoThumbnails: Map<String, String> = emptyMap(),
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
