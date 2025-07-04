package net.primal.android.events.ui

import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import net.primal.domain.links.CdnResourceVariant

fun List<CdnResourceVariant>?.findNearestOrNull(maxWidthPx: Int): CdnResourceVariant? {
    return this?.sortedBy { it.width }?.find { it.width >= maxWidthPx }
        ?: this?.maxByOrNull { it.width }
}

fun CdnResourceVariant?.calculateImageSize(
    maxWidth: Int,
    maxHeight: Int,
    density: Float,
): DpSize {
    if (this == null) return DpSize(maxWidth.dp, maxWidth.dp)

    val variantWidth = (width / density).toInt()
    val variantHeight = (height / density).toInt()

    val heightByAspectRation = ((maxWidth * variantHeight) / variantWidth)

    return DpSize(
        width = when {
            else -> maxWidth.dp
        },
        height = when {
            variantHeight == 0 -> maxWidth.dp
            else -> minOf(heightByAspectRation, maxHeight).dp
        },
    )
}
