package net.primal.android.premium.manage.media.api.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class MediaUploadInfo(
    @SerialName("url") val url: String,
    @SerialName("size") val sizeInBytes: Long,
    @SerialName("mimetype") val mimetype: String? = null,
    @SerialName("created_at") val createdAt: Long? = null,
)
