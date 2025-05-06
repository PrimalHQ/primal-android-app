package net.primal.android.auth.compose

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.DpSize

@Composable
fun ColumnWithBackground(
    modifier: Modifier = Modifier,
    backgroundPainter: Painter,
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
        ) {
            content(maxSize)
        }
    }
}
