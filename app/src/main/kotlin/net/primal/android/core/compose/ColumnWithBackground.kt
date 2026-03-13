package net.primal.android.core.compose

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.DpSize

@Composable
fun ColumnWithBackground(
    modifier: Modifier = Modifier,
    backgroundPainter: Painter,
    verticalArrangement: Arrangement.Vertical = Arrangement.Top,
    horizontalAlignment: Alignment.Horizontal = Alignment.Start,
    content: @Composable (DpSize) -> Unit,
) {
    BoxWithConstraints {
        val maxSize = DpSize(width = this.maxWidth, height = this.maxHeight)

        Image(
            modifier = Modifier.fillMaxSize(),
            painter = backgroundPainter,
            contentScale = ContentScale.FillBounds,
            alignment = Alignment.Center,
            contentDescription = null,
        )

        Column(
            modifier = modifier
                .fillMaxSize()
                .systemBarsPadding(),
            verticalArrangement = verticalArrangement,
            horizontalAlignment = horizontalAlignment,
        ) {
            content(maxSize)
        }
    }
}

@Composable
fun ColumnWithBackground(
    modifier: Modifier = Modifier,
    backgroundBrushProvider: (Size) -> Brush,
    brushAlpha: Float = 1f,
    backgroundColor: Color = Color.Unspecified,
    verticalArrangement: Arrangement.Vertical = Arrangement.Top,
    horizontalAlignment: Alignment.Horizontal = Alignment.Start,
    content: @Composable (DpSize) -> Unit,
) {
    BoxWithConstraints {
        val maxSize = DpSize(width = this.maxWidth, height = this.maxHeight)

        if (backgroundColor != Color.Unspecified) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(backgroundColor),
            )
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .alpha(brushAlpha)
                .drawBehind { drawRect(brush = backgroundBrushProvider(size)) },
        )

        Column(
            modifier = modifier
                .fillMaxSize()
                .systemBarsPadding(),
            verticalArrangement = verticalArrangement,
            horizontalAlignment = horizontalAlignment,
        ) {
            content(maxSize)
        }
    }
}
