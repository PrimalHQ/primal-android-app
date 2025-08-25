package net.primal.android.core.compose

import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

enum class FadingEdge {
    Top,
    Bottom,
    Start,
    End,
}

fun Modifier.fadingEdge(
    edge: FadingEdge,
    color: Color,
    length: Dp = 16.dp,
): Modifier =
    this
        .graphicsLayer(compositingStrategy = CompositingStrategy.Offscreen)
        .drawWithContent {
            drawContent()
            val edgePx = length.toPx()

            val brush = when (edge) {
                FadingEdge.Top -> Brush.verticalGradient(
                    colors = listOf(Color.Transparent, color),
                    startY = 0f,
                    endY = edgePx,
                )

                FadingEdge.Bottom -> Brush.verticalGradient(
                    colors = listOf(color, Color.Transparent),
                    startY = size.height - edgePx,
                    endY = size.height,
                )

                FadingEdge.Start -> Brush.horizontalGradient(
                    colors = listOf(Color.Transparent, color),
                    startX = 0f,
                    endX = edgePx,
                )

                FadingEdge.End -> Brush.horizontalGradient(
                    colors = listOf(color, Color.Transparent),
                    startX = size.width - edgePx,
                    endX = size.width,
                )
            }

            drawRect(
                brush = brush,
                size = size,
                blendMode = BlendMode.DstIn,
            )
        }
