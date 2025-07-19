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
    if (this == null || this.width == 0 || this.height == 0) {
        val sideLength = min(maxWidth, maxHeight)
        return DpSize(width = sideLength.dp, height = sideLength.dp)
    }

    val heightForMaxWidth = (maxWidth.toFloat() * this.height.toFloat()) / this.width.toFloat()
    val finalHeight = min(heightForMaxWidth, maxHeight.toFloat())

    return DpSize(
        width = maxWidth.dp,
        height = finalHeight.dp,
    )
}
