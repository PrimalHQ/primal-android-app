package net.primal.android.core.compose

import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp

fun Modifier.fadingBottomEdge(): Modifier =
    this.then(
        Modifier
            .graphicsLayer { alpha = 0.99f }
            .drawWithContent {
                val colors = listOf(Color.Transparent, Color.Black)
                drawContent()
                drawRect(
                    brush = Brush.verticalGradient(
                        colors = colors,
                        startY = size.height,
                        endY = size.height - 32.dp.toPx(),
                    ),
                    blendMode = BlendMode.DstIn,
                )
            },
    )

fun Modifier.fadingTopEdge(): Modifier =
    this.then(
        Modifier
            .graphicsLayer { alpha = 0.99f }
            .drawWithContent {
                val colors = listOf(Color.Transparent, Color.Black)
                drawContent()
                drawRect(
                    brush = Brush.verticalGradient(
                        colors = colors,
                        startY = 0f,
                        endY = 32.dp.toPx(),
                    ),
                    blendMode = BlendMode.DstIn,
                )
            },
    )
