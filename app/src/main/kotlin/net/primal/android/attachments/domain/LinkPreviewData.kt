package net.primal.android.attachments.domain

data class LinkPreviewData(
    val url: String,
    val mimeType: String? = null,
    val title: String? = null,
    val description: String? = null,
    val thumbnailUrl: String? = null,
    val authorAvatarUrl: String? = null,
)
