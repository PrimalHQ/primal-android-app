package net.primal.android.core.compose.foundation

import androidx.compose.foundation.background
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import net.primal.android.theme.AppTheme

fun Modifier.brandBackground(
    shape: Shape = RectangleShape,
) = this.composed {
    background(
        brush = Brush.linearGradient(
            colors = listOf(
                AppTheme.extraColorScheme.brand1,
                AppTheme.extraColorScheme.brand2
            ),
            start = Offset(0f, 0f),
            end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY),
        ),
        shape = shape,
    )
}
