package net.primal.domain.links

data class CdnResource(
    val url: String,
    val contentType: String? = null,
    val variants: List<CdnResourceVariant>? = null,
)
