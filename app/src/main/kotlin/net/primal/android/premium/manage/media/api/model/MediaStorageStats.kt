package net.primal.android.premium.manage.media.api.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class MediaStorageStats(
    @SerialName("video") val videosInBytes: Long,
    @SerialName("image") val imagesInBytes: Long,
    @SerialName("other") val otherFilesInBytes: Long,
)
