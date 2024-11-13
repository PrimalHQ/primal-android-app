package net.primal.android.premium.manage.media.ui

import java.time.Instant

data class MediaUiItem(
    val mediaId: String,
    val thumbnailUrl: String? = null,
    val mediaUrl: String,
    val sizeInBytes: Long,
    val type: MediaType,
    val createdAt: Instant? = null,
)

enum class MediaType {
    Image,
    Video,
    Other,
}
