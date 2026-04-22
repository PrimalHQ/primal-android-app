package net.primal.android.notes.feed.note.ui.attachment

import androidx.compose.foundation.layout.BoxWithConstraintsScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import kotlin.math.min
import net.primal.android.core.compose.attachment.model.EventUriUi
import net.primal.android.events.ui.calculateDimensions
import net.primal.android.events.ui.calculateImageSize
import net.primal.android.events.ui.findNearestOrNull
import net.primal.domain.links.CdnResourceVariant
import net.primal.domain.links.EventUriType

private const val MAX_SCREEN_HEIGHT_VISIBLE_AREA = 0.77
private const val FEED_NOTE_MEDIA_MAX_HEIGHT_DP = 580

enum class Fit { FitWidth, FitBoth }

@Composable
fun BoxWithConstraintsScope.findFeedNoteMediaSize(eventUri: EventUriUi): DpSize =
    findImageSize(
        eventUri = eventUri,
        maxHeightDp = FEED_NOTE_MEDIA_MAX_HEIGHT_DP,
        fit = Fit.FitBoth,
        allowUpscaling = false,
    )

@Composable
fun BoxWithConstraintsScope.findMediaFeedCardMediaSize(eventUri: EventUriUi): DpSize {
    val maxHeightDp = (LocalConfiguration.current.screenHeightDp * MAX_SCREEN_HEIGHT_VISIBLE_AREA).toInt()
    return findImageSize(
        eventUri = eventUri,
        maxHeightDp = maxHeightDp,
        fit = Fit.FitWidth,
        allowUpscaling = true,
    )
}

@Composable
private fun BoxWithConstraintsScope.findImageSize(
    eventUri: EventUriUi,
    maxHeightDp: Int,
    fit: Fit,
    allowUpscaling: Boolean,
): DpSize {
    val maxWidthPx = with(LocalDensity.current) { maxWidth.roundToPx() }
    val maxWidthDp = maxWidth.value.toInt()
    val variant = eventUri.variants.findNearestOrNull(maxWidthPx = maxWidthPx)

    return when {
        variant != null && variant.width > 0 && variant.height > 0 -> {
            calculateMediaSize(
                width = variant.width,
                height = variant.height,
                maxWidth = maxWidthDp,
                maxHeight = maxHeightDp,
                fit = fit,
                allowUpscaling = allowUpscaling,
            )
        }
        eventUri.originalWidth != null && eventUri.originalHeight != null -> {
            calculateMediaSize(
                width = eventUri.originalWidth,
                height = eventUri.originalHeight,
                maxWidth = maxWidthDp,
                maxHeight = maxHeightDp,
                fit = fit,
                allowUpscaling = allowUpscaling,
            )
        }
        else -> calculateFallbackDimensions(
            maxWidth = maxWidthDp,
            maxHeight = maxHeightDp,
            type = eventUri.type,
        )
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

private fun calculateMediaSize(
    width: Int,
    height: Int,
    maxWidth: Int,
    maxHeight: Int,
    fit: Fit,
    allowUpscaling: Boolean,
): DpSize {
    if (width == 0 || height == 0) {
        val sideLength = min(maxWidth, maxHeight)
        return DpSize(width = sideLength.dp, height = sideLength.dp)
    }

    return when (fit) {
        Fit.FitWidth -> calculateDimensions(
            width = width,
            height = height,
            maxWidth = maxWidth,
            maxHeight = maxHeight,
        )

        Fit.FitBoth -> {
            val widthScale = maxWidth.toFloat() / width.toFloat()
            val heightScale = maxHeight.toFloat() / height.toFloat()
            val upperBound = if (allowUpscaling) Float.POSITIVE_INFINITY else 1f
            val scale = minOf(widthScale, heightScale, upperBound)
            DpSize(
                width = (width.toFloat() * scale).dp,
                height = (height.toFloat() * scale).dp,
            )
        }
    }
}

private fun calculateFallbackDimensions(
    maxWidth: Int,
    maxHeight: Int,
    type: EventUriType,
): DpSize {
    return if (type == EventUriType.Video) {
        DpSize(width = maxWidth.dp, height = (maxWidth * 9 / 16).dp)
    } else {
        val side = min(maxWidth, maxHeight)
        DpSize(width = side.dp, height = side.dp)
    }
}
