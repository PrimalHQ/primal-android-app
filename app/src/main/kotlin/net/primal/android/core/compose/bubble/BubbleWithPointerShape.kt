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
    private val placement: BubblePlacement = BubblePlacement.Below,
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

            val body = when (placement) {
                BubblePlacement.Below -> RoundRect(0f, ph, size.width, size.height, CornerRadius(r, r))
                BubblePlacement.Above -> RoundRect(0f, 0f, size.width, size.height - ph, CornerRadius(r, r))
            }

            val path = Path().apply {
                addRoundRect(body)

                when (placement) {
                    BubblePlacement.Below -> {
                        moveTo(po, ph)
                        lineTo(po + pw, ph)
                        lineTo(po + pw / 2f, 0f)
                    }

                    BubblePlacement.Above -> {
                        moveTo(po, size.height - ph)
                        lineTo(po + pw, size.height - ph)
                        lineTo(po + pw / 2f, size.height)
                    }
                }
                close()
            }

            Outline.Generic(path)
        }
}
