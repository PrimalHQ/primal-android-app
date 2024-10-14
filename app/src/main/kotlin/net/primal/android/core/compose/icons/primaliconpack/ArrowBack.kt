@file:Suppress("MagicNumber")

package net.primal.android.core.compose.icons.primaliconpack

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp
import net.primal.android.core.compose.icons.PrimalIcons

val PrimalIcons.ArrowBack: ImageVector
    get() {
        if (_ArrowBack != null) {
            return _ArrowBack!!
        }
        _ArrowBack = ImageVector.Builder(
            name = "ArrowBack",
            defaultWidth = 20.dp,
            defaultHeight = 20.dp,
            viewportWidth = 20f,
            viewportHeight = 20f
        ).apply {
            path(fill = SolidColor(Color(0xFFFFFFFF))) {
                moveTo(8.331f, 3.757f)
                curveTo(8.706f, 3.354f, 8.704f, 2.732f, 8.326f, 2.331f)
                curveTo(7.908f, 1.888f, 7.199f, 1.89f, 6.782f, 2.335f)
                lineTo(0.286f, 9.278f)
                curveTo(-0.095f, 9.685f, -0.095f, 10.315f, 0.286f, 10.722f)
                lineTo(6.782f, 17.665f)
                curveTo(7.199f, 18.11f, 7.908f, 18.112f, 8.326f, 17.669f)
                curveTo(8.704f, 17.268f, 8.706f, 16.646f, 8.331f, 16.243f)
                lineTo(3.523f, 11.085f)
                horizontalLineTo(18.905f)
                curveTo(19.51f, 11.085f, 20f, 10.599f, 20f, 10f)
                curveTo(20f, 9.4f, 19.51f, 8.914f, 18.905f, 8.914f)
                horizontalLineTo(3.523f)
                lineTo(8.331f, 3.757f)
                close()
            }
        }.build()

        return _ArrowBack!!
    }

@Suppress("ObjectPropertyName")
private var _ArrowBack: ImageVector? = null
