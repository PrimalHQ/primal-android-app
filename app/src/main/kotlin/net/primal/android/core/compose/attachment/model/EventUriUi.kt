package net.primal.android.core.compose.attachment.model

import net.primal.domain.CdnResourceVariant
import net.primal.domain.EventLink
import net.primal.domain.EventUriType

data class EventUriUi(
    val eventId: String,
    val url: String,
    val type: EventUriType,
    val mimeType: String? = null,
    val variants: List<CdnResourceVariant>? = null,
    val title: String? = null,
    val description: String? = null,
    val thumbnailUrl: String? = null,
    val authorAvatarUrl: String? = null,
    val position: Int,
)

fun EventUriUi.isMediaUri() = type == EventUriType.Image || type == EventUriType.Video

fun EventLink.asEventUriUiModel() =
    EventUriUi(
        eventId = this.eventId,
        url = this.url,
        mimeType = this.mimeType,
        type = this.type,
        variants = this.variants ?: emptyList(),
        title = this.title,
        description = this.description,
        thumbnailUrl = this.thumbnail,
        authorAvatarUrl = this.authorAvatarUrl,
        position = this.position,
    )
