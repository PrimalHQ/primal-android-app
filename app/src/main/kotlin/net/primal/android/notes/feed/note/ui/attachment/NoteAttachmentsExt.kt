package net.primal.android.notes.feed.note.ui.attachment

import androidx.compose.foundation.layout.BoxWithConstraintsScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import kotlin.math.min
import net.primal.android.core.compose.attachment.model.EventUriUi
import net.primal.android.events.ui.calculateImageSize
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

    return when {
        variant != null && variant.width > 0 && variant.height > 0 -> {
            variant.calculateImageSize(
                maxWidth = maxWidth,
                maxHeight = maxHeight,
            )
        }
        eventUri.originalWidth != null && eventUri.originalHeight != null -> {
            calculateDimensions(
                width = eventUri.originalWidth,
                height = eventUri.originalHeight,
                maxWidth = maxWidth,
                maxHeight = maxHeight,
            )
        }
        else -> {
            calculateFallbackDimensions(
                maxWidth = maxWidth,
                type = eventUri.type,
            )
        }
    }
}

@Composable
fun CdnResourceVariant?.findImageSize(maxWidth: Int): DpSize {
    val maxHeight = (LocalConfiguration.current.screenHeightDp * MAX_SCREEN_HEIGHT_VISIBLE_AREA).toInt()
    return calculateImageSize(
        maxWidth = maxWidth,
        maxHeight = maxHeight,
    )
}

private fun calculateDimensions(
    width: Int,
    height: Int,
    maxWidth: Int,
    maxHeight: Int,
): DpSize {
    val heightForMaxWidth = (maxWidth.toFloat() * height.toFloat()) / width.toFloat()
    val finalHeight = min(heightForMaxWidth, maxHeight.toFloat())
    return DpSize(
        width = maxWidth.dp,
        height = finalHeight.dp,
    )
}

private fun calculateFallbackDimensions(maxWidth: Int, type: EventUriType): DpSize {
    return if (type == EventUriType.Video) {
        DpSize(width = maxWidth.dp, height = (maxWidth * 9 / 16).dp)
    } else {
        DpSize(width = maxWidth.dp, height = maxWidth.dp)
    }
}
