package net.primal.android.attachments.domain

import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp

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
    return DpSize(
        width = when {
            else -> maxWidth.dp
        },
        height = when {
            variantHeight == 0 -> maxWidth.dp
            variantHeight > maxHeight -> maxHeight.dp
            else -> ((maxWidth * variantHeight) / variantWidth).dp
        },
    )
}
