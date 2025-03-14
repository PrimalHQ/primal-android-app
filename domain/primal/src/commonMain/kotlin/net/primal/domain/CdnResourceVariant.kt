package net.primal.domain

import kotlinx.serialization.Serializable

@Serializable
data class CdnResourceVariant(
    val width: Int,
    val height: Int,
    val mediaUrl: String,
)

fun List<CdnResourceVariant>?.findNearestOrNull(maxWidthPx: Int): CdnResourceVariant? {
    return this?.sortedBy { it.width }
        ?.find { it.width >= maxWidthPx }
        ?: this?.maxByOrNull { it.width }
}
