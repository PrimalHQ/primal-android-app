package net.primal.domain.links

import kotlinx.serialization.Serializable

@Serializable
data class CdnResourceVariant(
    val width: Int,
    val height: Int,
    val mediaUrl: String,
)
