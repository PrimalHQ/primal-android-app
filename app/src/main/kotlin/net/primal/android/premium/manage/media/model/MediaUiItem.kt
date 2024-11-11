package net.primal.android.premium.manage.media.model

import java.time.Instant

data class MediaUiItem(
    val mediaId: String,
    val thumbnailUrl: String? = null,
    val mediaUrl: String,
    val sizeInBytes: Long,
    val type: MediaType,
    val date: Instant,
)

enum class MediaType {
    Image,
    Video,
}
