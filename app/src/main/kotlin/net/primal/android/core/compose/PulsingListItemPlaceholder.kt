package net.primal.android.core.compose

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import net.primal.android.LocalPrimalTheme
import net.primal.android.R
import net.primal.android.theme.AppTheme

@Composable
fun PulsingListItemPlaceholder(
    modifier: Modifier = Modifier,
    height: Dp,
    width: Dp? = null,
    widthFraction: Float = 1f,
    shape: Shape = AppTheme.shapes.small,
    padding: PaddingValues = PaddingValues(0.dp),
) {
    val animationRawResId = when (LocalPrimalTheme.current.isDarkTheme) {
        true -> R.raw.primal_loader_generic_square_dark
        false -> R.raw.primal_loader_generic_square_light
    }

    Box(
        modifier = modifier
            .run {
                if (width != null) {
                    this.width(width)
                } else {
                    this.fillMaxWidth(widthFraction)
                }
            }
            .height(height)
            .clip(shape),
    ) {
        InfiniteLottieAnimation(
            modifier = Modifier
                .scale(scale = 25f)
                .padding(padding),
            resId = animationRawResId,
        )
    }
}
