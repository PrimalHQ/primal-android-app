package net.primal.android.events.ui

import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import kotlin.math.min
import net.primal.domain.links.CdnResourceVariant

fun List<CdnResourceVariant>?.findNearestOrNull(maxWidthPx: Int): CdnResourceVariant? {
    if (this.isNullOrEmpty()) return null
    return this.sortedBy { it.width }.find { it.width >= maxWidthPx }
        ?: this.maxByOrNull { it.width }
}

fun CdnResourceVariant?.calculateImageSize(maxWidth: Int, maxHeight: Int): DpSize {
    if (this == null) {
        val sideLength = min(maxWidth, maxHeight)
        return DpSize(width = sideLength.dp, height = sideLength.dp)
    }

    return calculateDimensions(
        width = this.width,
        height = this.height,
        maxWidth = maxWidth,
        maxHeight = maxHeight,
    )
}

fun calculateDimensions(
    width: Int,
    height: Int,
    maxWidth: Int,
    maxHeight: Int,
    allowUpscaling: Boolean = true,
): DpSize {
    if (width == 0 || height == 0) {
        val sideLength = min(maxWidth, maxHeight)
        return DpSize(width = sideLength.dp, height = sideLength.dp)
    }

    val targetWidth = if (!allowUpscaling && width < maxWidth) width else maxWidth
    val heightForTargetWidth = (targetWidth.toFloat() * height.toFloat()) / width.toFloat()
    val finalHeight = min(heightForTargetWidth, maxHeight.toFloat())

    return DpSize(
        width = targetWidth.dp,
        height = finalHeight.dp,
    )
}
