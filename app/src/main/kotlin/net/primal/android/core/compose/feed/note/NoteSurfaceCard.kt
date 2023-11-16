package net.primal.android.core.compose.feed.note

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Card
import androidx.compose.material3.CardColors
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import net.primal.android.theme.AppTheme
import net.primal.android.theme.PrimalTheme
import net.primal.android.theme.domain.PrimalTheme

@Composable
fun NoteSurfaceCard(
    modifier: Modifier = Modifier,
    shape: Shape = CardDefaults.shape,
    drawLineAboveAvatar: Boolean = false,
    drawLineBelowAvatar: Boolean = false,
    lineOffsetX: Dp = 40.dp,
    lineWidth: Dp = 2.dp,
    colors: CardColors = CardDefaults.cardColors(),
    content: @Composable ColumnScope.() -> Unit,
) {
    val outlineColor = AppTheme.colorScheme.outline
    Card(
        modifier = modifier.clip(shape),
        shape = shape,
        colors = colors,
    ) {
        if (drawLineAboveAvatar || drawLineBelowAvatar) {
            Column(
                modifier = Modifier.drawWithCache {
                    onDrawBehind {
                        val connectionX = lineOffsetX.toPx()

                        if (drawLineBelowAvatar) {
                            drawLine(
                                color = outlineColor,
                                start = Offset(x = connectionX, y = 16.dp.toPx()),
                                end = Offset(x = connectionX, y = size.height),
                                strokeWidth = lineWidth.toPx(),
                                cap = StrokeCap.Square,
                            )
                        }

                        if (drawLineAboveAvatar) {
                            drawLine(
                                color = outlineColor,
                                start = Offset(x = connectionX, y = 0f),
                                end = Offset(x = connectionX, y = 16.dp.toPx()),
                                strokeWidth = lineWidth.toPx(),
                                cap = StrokeCap.Square,
                            )
                        }
                    }
                },
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
        NoteSurfaceCard {
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
