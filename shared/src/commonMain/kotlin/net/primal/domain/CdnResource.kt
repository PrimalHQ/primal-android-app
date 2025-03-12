package net.primal.domain

data class CdnResource(
    val url: String,
    val contentType: String? = null,
    val variants: List<CdnResourceVariant>? = null,
)
