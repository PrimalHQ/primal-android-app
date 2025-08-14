package net.primal.android.core.compose.icons.primaliconpack

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp
import net.primal.android.core.compose.icons.PrimalIcons

val PrimalIcons.VideoPlayMini: ImageVector
    get() {
        if (_VideoPlayMini != null) {
            return _VideoPlayMini!!
        }
        _VideoPlayMini = ImageVector.Builder(
            name = "VideoPlayMini",
            defaultWidth = 32.dp,
            defaultHeight = 32.dp,
            viewportWidth = 32f,
            viewportHeight = 32f
        ).apply {
            path(fill = SolidColor(Color(0xFFFFFFFF))) {
                moveTo(22.535f, 16.774f)
                curveTo(23.154f, 16.434f, 23.155f, 15.585f, 22.537f, 15.245f)
                lineTo(11.404f, 9.12f)
                curveTo(10.785f, 8.78f, 10.011f, 9.204f, 10.011f, 9.883f)
                lineTo(10f, 22.115f)
                curveTo(9.999f, 22.795f, 10.773f, 23.22f, 11.392f, 22.881f)
                lineTo(22.535f, 16.774f)
                close()
            }
        }.build()

        return _VideoPlayMini!!
    }

@Suppress("ObjectPropertyName")
private var _VideoPlayMini: ImageVector? = null
