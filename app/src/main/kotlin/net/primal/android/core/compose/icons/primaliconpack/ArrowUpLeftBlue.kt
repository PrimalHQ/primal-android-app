package net.primal.android.core.compose.icons.primaliconpack

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp
import net.primal.android.core.compose.icons.PrimalIcons

val PrimalIcons.ArrowUpLeftBlue: ImageVector
    get() {
        if (_ArrowUpLeft != null) {
            return _ArrowUpLeft!!
        }
        _ArrowUpLeft = ImageVector.Builder(
            name = "ArrowUpLeft",
            defaultWidth = 12.dp,
            defaultHeight = 12.dp,
            viewportWidth = 12f,
            viewportHeight = 12f
        ).apply {
            path(fill = SolidColor(Color(0xFF2394EF))) {
                moveTo(9.504f, 0.68f)
                curveTo(9.493f, 1.025f, 9.217f, 1.303f, 8.872f, 1.316f)
                lineTo(2.456f, 1.49f)
                lineTo(11.8f, 10.834f)
                curveTo(12.067f, 11.101f, 12.067f, 11.533f, 11.8f, 11.8f)
                curveTo(11.533f, 12.067f, 11.101f, 12.067f, 10.834f, 11.8f)
                lineTo(1.49f, 2.456f)
                lineTo(1.316f, 8.872f)
                curveTo(1.303f, 9.217f, 1.025f, 9.493f, 0.68f, 9.504f)
                curveTo(0.299f, 9.517f, -0.014f, 9.203f, 0f, 8.822f)
                lineTo(0.223f, 0.865f)
                curveTo(0.236f, 0.516f, 0.516f, 0.236f, 0.865f, 0.223f)
                lineTo(8.822f, 0f)
                curveTo(9.203f, -0.014f, 9.517f, 0.298f, 9.504f, 0.68f)
                close()
            }
        }.build()

        return _ArrowUpLeft!!
    }

@Suppress("ObjectPropertyName")
private var _ArrowUpLeft: ImageVector? = null
