package net.primal.android.nostr.model.primal.content

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ContentPrimalLinkPreviews(
    @SerialName("event_id") val eventId: String,
    val resources: List<LinkPreview>,
)

@Serializable
data class LinkPreview(
    val url: String,
    @SerialName("mimetype") val mimeType: String? = null,
    @SerialName("md_title") val title: String? = null,
    @SerialName("md_description") val description: String? = null,
    @SerialName("md_image") val thumbnailUrl: String? = null,
    @SerialName("icon_url") val iconUrl: String? = null,
)
