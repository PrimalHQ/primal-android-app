package net.primal.android.attachments.domain

data class CdnResource(
    val eventId: String,
    val url: String,
    val contentType: String? = null,
    val variants: List<CdnResourceVariant>? = null,
)
