package net.primal.domain

import kotlinx.serialization.Serializable

@Serializable
data class EventLink(
    val eventId: String,
    val position: Int,
    val url: String,
    val type: EventUriType,
    val mimeType: String? = null,
    val variants: List<CdnResourceVariant>? = null,
    val title: String? = null,
    val description: String? = null,
    val thumbnail: String? = null,
    val authorAvatarUrl: String? = null,
)
