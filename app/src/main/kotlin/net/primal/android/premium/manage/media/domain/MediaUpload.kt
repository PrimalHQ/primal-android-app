package net.primal.android.premium.manage.media.domain

import net.primal.android.events.domain.CdnResource

data class MediaUpload(
    val url: String,
    val sizeInBytes: Long,
    val createdAt: Long?,
    val cdnResource: CdnResource?,
    val mimetype: String?,
)
