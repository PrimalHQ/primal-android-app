package net.primal.android.notes.feed.note.ui.attachment

import androidx.compose.foundation.layout.BoxWithConstraintsScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.DpSize
import net.primal.android.core.compose.attachment.model.EventUriUi
import net.primal.android.events.domain.CdnResourceVariant
import net.primal.android.events.domain.calculateImageSize
import net.primal.android.events.domain.findNearestOrNull

private const val MAX_SCREEN_HEIGHT_VISIBLE_AREA = 0.77

@Composable
fun BoxWithConstraintsScope.findImageSize(eventUri: EventUriUi): DpSize {
    val density = LocalDensity.current.density
    val maxWidthPx = with(LocalDensity.current) { maxWidth.roundToPx() }
    val maxWidth = maxWidth.value.toInt()
    val maxHeight = (LocalConfiguration.current.screenHeightDp * MAX_SCREEN_HEIGHT_VISIBLE_AREA).toInt()
    val variant = eventUri.variants.findNearestOrNull(maxWidthPx = maxWidthPx)
    return variant.calculateImageSize(
        maxWidth = maxWidth,
        maxHeight = maxHeight,
        density = density,
    )
}

@Composable
fun CdnResourceVariant?.findImageSize(maxWidth: Int): DpSize {
    val density = LocalDensity.current.density
    val maxHeight = (LocalConfiguration.current.screenHeightDp * MAX_SCREEN_HEIGHT_VISIBLE_AREA).toInt()
    return calculateImageSize(
        maxWidth = maxWidth,
        maxHeight = maxHeight,
        density = density,
    )
}
