package net.primal.domain

fun List<CdnResourceVariant>?.findNearestOrNull(maxWidthPx: Int): CdnResourceVariant? {
    return this?.sortedBy { it.width }?.find { it.width >= maxWidthPx }
        ?: this?.maxByOrNull { it.width }
}
