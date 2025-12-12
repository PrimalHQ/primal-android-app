package net.primal.android.notes.feed.note.ui.attachment

import androidx.compose.foundation.layout.BoxWithConstraintsScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import net.primal.android.core.compose.attachment.model.EventUriUi
import net.primal.android.events.ui.findNearestOrNull
import net.primal.domain.links.CdnResourceVariant
import net.primal.domain.links.EventUriType

private const val MAX_SCREEN_HEIGHT_VISIBLE_AREA = 0.77

@Composable
fun BoxWithConstraintsScope.findImageSize(eventUri: EventUriUi): DpSize {
    val maxWidthPx = with(LocalDensity.current) { maxWidth.roundToPx() }
    val maxWidth = maxWidth.value.toInt()
    val maxHeight = (LocalConfiguration.current.screenHeightDp * MAX_SCREEN_HEIGHT_VISIBLE_AREA).toInt()
    val variant = eventUri.variants.findNearestOrNull(maxWidthPx = maxWidthPx)
    return variant.calculateImageSize(
        maxWidth = maxWidth,
        maxHeight = maxHeight,
        type = eventUri.type,
    )
}

@Composable
fun CdnResourceVariant?.findImageSize(maxWidth: Int): DpSize {
    val maxHeight = (LocalConfiguration.current.screenHeightDp * MAX_SCREEN_HEIGHT_VISIBLE_AREA).toInt()
    return calculateImageSize(
        maxWidth = maxWidth,
        maxHeight = maxHeight,
        type = EventUriType.Image,
    )
}

fun CdnResourceVariant?.calculateImageSize(
    maxWidth: Int,
    maxHeight: Int,
    type: EventUriType,
): DpSize {
    val variantWidth = this?.width?.takeIf { it > 0 }
    val variantHeight = this?.height?.takeIf { it > 0 }

    if (variantWidth == null || variantHeight == null) {
        return if (type == EventUriType.Video) {
            DpSize(width = maxWidth.dp, height = (maxWidth * 9 / 16).dp)
        } else {
            DpSize(width = maxWidth.dp, height = maxWidth.dp)
        }
    }

    val imageAspectRatio = variantWidth.toFloat() / variantHeight.toFloat()
    val containerAspectRatio = maxWidth.toFloat() / maxHeight.toFloat()

    return if (imageAspectRatio > containerAspectRatio) {
        DpSize(
            width = maxWidth.dp,
            height = (maxWidth / imageAspectRatio).dp,
        )
    } else {
        DpSize(
            width = (maxHeight * imageAspectRatio).dp,
            height = maxHeight.dp,
        )
    }
}
