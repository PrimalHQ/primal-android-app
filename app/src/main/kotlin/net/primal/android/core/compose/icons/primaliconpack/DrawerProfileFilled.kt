package net.primal.android.core.compose.icons.primaliconpack

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp
import net.primal.android.core.compose.icons.PrimalIcons

val PrimalIcons.DrawerProfileFilled: ImageVector
    get() {
        if (_DrawerProfileFilled != null) {
            return _DrawerProfileFilled!!
        }
        _DrawerProfileFilled = ImageVector.Builder(
            name = "DrawerProfileFilled",
            defaultWidth = 20.dp,
            defaultHeight = 20.dp,
            viewportWidth = 20f,
            viewportHeight = 20f,
        ).apply {
            path(
                fill = SolidColor(Color(0xFFAAAAAA)),
                pathFillType = PathFillType.NonZero,
            ) {
                moveTo(15f, 5f)
                curveTo(15f, 7.761f, 12.761f, 10f, 10f, 10f)
                curveTo(7.239f, 10f, 5f, 7.761f, 5f, 5f)
                curveTo(5f, 2.239f, 7.239f, 0f, 10f, 0f)
                curveTo(12.761f, 0f, 15f, 2.239f, 15f, 5f)
                close()
            }
            path(
                fill = SolidColor(Color(0xFFAAAAAA)),
                pathFillType = PathFillType.NonZero,
            ) {
                moveTo(1f, 16f)
                curveTo(1f, 13.791f, 2.791f, 12f, 5f, 12f)
                horizontalLineTo(15f)
                curveTo(17.209f, 12f, 19f, 13.791f, 19f, 16f)
                curveTo(19f, 18.209f, 17.209f, 20f, 15f, 20f)
                horizontalLineTo(5f)
                curveTo(2.791f, 20f, 1f, 18.209f, 1f, 16f)
                close()
            }
        }.build()

        return _DrawerProfileFilled!!
    }

@Suppress("ObjectPropertyName")
private var _DrawerProfileFilled: ImageVector? = null
