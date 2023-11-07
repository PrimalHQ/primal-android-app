package net.primal.android.core.compose.feed

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import net.primal.android.theme.AppTheme
import net.primal.android.theme.PrimalTheme

@Composable
fun CardWithHighlight(
    modifier: Modifier = Modifier,
    shape: Shape = CardDefaults.shape,
    highlighted: Boolean = false,
    highlightWidth: Dp = 8.dp,
    connectedToPreviousNote: Boolean = false,
    connectedToNextNote: Boolean = false,
    connectionWidth: Dp = 2.dp,
    content: @Composable ColumnScope.() -> Unit,
) {

    val outlineColor = AppTheme.colorScheme.outline
    val gradientColors = listOf(
        AppTheme.colorScheme.primary,
        AppTheme.colorScheme.primary,
    )

    Card(
        shape = shape,
        modifier = modifier.clip(shape),
    ) {
        if (highlighted || connectedToPreviousNote || connectedToNextNote) {
            Column(
                modifier = Modifier.drawWithCache {
                    onDrawBehind {
                        if (highlighted) {
                            drawLine(
                                brush = Brush.verticalGradient(gradientColors),
                                start = Offset(x = 0f, y = 0f),
                                end = Offset(x = 0f, y = size.height),
                                strokeWidth = highlightWidth.toPx(),
                                cap = StrokeCap.Square
                            )
                        }

                        val connectionX = 40.dp.toPx()

                        if (connectedToNextNote) {
                            drawLine(
                                color = outlineColor,
                                start = Offset(x = connectionX, y = 16.dp.toPx()),
                                end = Offset(x = connectionX, y = size.height),
                                strokeWidth = connectionWidth.toPx(),
                                cap = StrokeCap.Square
                            )
                        }

                        if (connectedToPreviousNote) {
                            drawLine(
                                color = outlineColor,
                                start = Offset(x = connectionX, y = 0f),
                                end = Offset(x = connectionX, y = 16.dp.toPx()),
                                strokeWidth = connectionWidth.toPx(),
                                cap = StrokeCap.Square
                            )
                        }
                    }
                }
            ) {
                content()
            }
        } else {
            content()
        }
    }
}

@Preview
@Composable
fun PreviewCardWithHighlight() {
    PrimalTheme(primalTheme = PrimalTheme.Sunset) {
        CardWithHighlight(
            highlighted = true,
            highlightWidth = 16.dp,
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(text = "This is sample text on a Card.")
            }
        }
    }
}
