package net.primal.android.core.ext

import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import net.primal.android.core.compose.media.model.MediaResourceUi
import net.primal.android.nostr.model.primal.PrimalResourceVariant


fun List<MediaResourceUi>?.findByUrl(url: String?): MediaResourceUi? =
    if (url == null) null else this?.find { it.url.startsWith(url) }

fun List<PrimalResourceVariant>?.findNearestOrNull(maxWidthPx: Int): PrimalResourceVariant? {
    return this?.sortedBy { it.width }?.find { it.width >= maxWidthPx }
        ?: this?.maxByOrNull { it.width }
}

fun PrimalResourceVariant?.calculateImageSize(
    maxWidth: Int,
    maxHeight: Int,
    density: Float
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
        }
    )
}
