package net.primal.android.core.compose.bubble

import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp

/**
 * @param pointerOffset Horizontal offset of the pointer from the left edge of the bubble body.
 */
class BubbleWithPointerShape(
    private val cornerRadius: Dp = 12.dp,
    private val pointerWidth: Dp = 12.dp,
    private val pointerHeight: Dp = 8.dp,
    private val pointerOffset: Dp = 24.dp,
) : Shape {
    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density,
    ): Outline =
        with(density) {
            val r = cornerRadius.toPx()
            val pw = pointerWidth.toPx()
            val ph = pointerHeight.toPx()
            val po = pointerOffset.toPx().coerceIn(r, size.width - r - pw)

            val body = RoundRect(
                left = 0f,
                top = ph,
                right = size.width,
                bottom = size.height,
                cornerRadius = CornerRadius(r, r),
            )

            val path = Path().apply {
                addRoundRect(body)

                moveTo(po, ph)
                lineTo(po + pw, ph)
                lineTo(po + pw / 2f, 0f)
                close()
            }

            Outline.Generic(path)
        }
}
