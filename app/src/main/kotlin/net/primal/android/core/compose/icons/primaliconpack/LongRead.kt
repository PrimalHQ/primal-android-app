package net.primal.android.core.compose.icons.primaliconpack

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp
import net.primal.android.core.compose.icons.PrimalIcons

val PrimalIcons.LongRead: ImageVector
    get() {
        if (_LongRead != null) {
            return _LongRead!!
        }
        _LongRead = ImageVector.Builder(
            name = "LongRead",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(fill = SolidColor(Color(0xFFAAAAAA))) {
                moveTo(2.625f, 4.5f)
                curveTo(2.625f, 4.155f, 2.905f, 3.875f, 3.25f, 3.875f)
                horizontalLineTo(20.75f)
                curveTo(21.095f, 3.875f, 21.375f, 4.155f, 21.375f, 4.5f)
                curveTo(21.375f, 4.845f, 21.095f, 5.125f, 20.75f, 5.125f)
                horizontalLineTo(3.25f)
                curveTo(2.905f, 5.125f, 2.625f, 4.845f, 2.625f, 4.5f)
                close()
            }
            path(fill = SolidColor(Color(0xFFAAAAAA))) {
                moveTo(2.625f, 9.5f)
                curveTo(2.625f, 9.155f, 2.905f, 8.875f, 3.25f, 8.875f)
                horizontalLineTo(20.75f)
                curveTo(21.095f, 8.875f, 21.375f, 9.155f, 21.375f, 9.5f)
                curveTo(21.375f, 9.845f, 21.095f, 10.125f, 20.75f, 10.125f)
                horizontalLineTo(3.25f)
                curveTo(2.905f, 10.125f, 2.625f, 9.845f, 2.625f, 9.5f)
                close()
            }
            path(fill = SolidColor(Color(0xFFAAAAAA))) {
                moveTo(2.625f, 14.5f)
                curveTo(2.625f, 14.155f, 2.905f, 13.875f, 3.25f, 13.875f)
                horizontalLineTo(20.75f)
                curveTo(21.095f, 13.875f, 21.375f, 14.155f, 21.375f, 14.5f)
                curveTo(21.375f, 14.845f, 21.095f, 15.125f, 20.75f, 15.125f)
                horizontalLineTo(3.25f)
                curveTo(2.905f, 15.125f, 2.625f, 14.845f, 2.625f, 14.5f)
                close()
            }
            path(fill = SolidColor(Color(0xFFAAAAAA))) {
                moveTo(2.625f, 19.5f)
                curveTo(2.625f, 19.155f, 2.905f, 18.875f, 3.25f, 18.875f)
                horizontalLineTo(14.083f)
                curveTo(14.429f, 18.875f, 14.708f, 19.155f, 14.708f, 19.5f)
                curveTo(14.708f, 19.845f, 14.429f, 20.125f, 14.083f, 20.125f)
                horizontalLineTo(3.25f)
                curveTo(2.905f, 20.125f, 2.625f, 19.845f, 2.625f, 19.5f)
                close()
            }
        }.build()

        return _LongRead!!
    }

@Suppress("ObjectPropertyName")
private var _LongRead: ImageVector? = null
