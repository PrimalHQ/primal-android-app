package net.primal.android.premium.manage.media.domain

import net.primal.domain.links.CdnResource

data class MediaUpload(
    val url: String,
    val sizeInBytes: Long,
    val createdAt: Long?,
    val cdnResource: CdnResource?,
    val mimetype: String?,
)
