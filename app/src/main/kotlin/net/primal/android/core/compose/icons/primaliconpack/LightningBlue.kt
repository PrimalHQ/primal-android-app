package net.primal.android.core.compose.icons.primaliconpack

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp
import net.primal.android.core.compose.icons.PrimalIcons

val PrimalIcons.LightningBlue: ImageVector
    get() {
        if (_LightningBlue != null) {
            return _LightningBlue!!
        }
        _LightningBlue = ImageVector.Builder(
            name = "LightningBlue",
            defaultWidth = 42.dp,
            defaultHeight = 60.dp,
            viewportWidth = 42f,
            viewportHeight = 60f
        ).apply {
            path(
                fill = Brush.radialGradient(
                    colorStops = arrayOf(
                        0.156f to Color(0xFF35CCFF),
                        1f to Color(0xFF198AE5)
                    ),
                    center = Offset(21f, 30f),
                    radius = 32.097f
                )
            ) {
                moveTo(25.198f, 0.915f)
                curveTo(26.811f, -1.105f, 28.864f, 0.501f, 28.351f, 2.991f)
                lineTo(26.061f, 20.627f)
                curveTo(26.022f, 20.926f, 26.255f, 21.191f, 26.557f, 21.191f)
                horizontalLineTo(40.739f)
                curveTo(41.762f, 21.191f, 42.358f, 22.292f, 41.764f, 23.084f)
                lineTo(16.799f, 58.981f)
                curveTo(15.24f, 61.059f, 12.379f, 59.706f, 12.851f, 57.192f)
                lineTo(15.837f, 37.252f)
                curveTo(15.882f, 36.95f, 15.648f, 36.678f, 15.342f, 36.678f)
                horizontalLineTo(1.26f)
                curveTo(0.22f, 36.678f, -0.371f, 35.544f, 0.259f, 34.755f)
                lineTo(25.198f, 0.915f)
                close()
            }
        }.build()

        return _LightningBlue!!
    }

@Suppress("ObjectPropertyName")
private var _LightningBlue: ImageVector? = null
