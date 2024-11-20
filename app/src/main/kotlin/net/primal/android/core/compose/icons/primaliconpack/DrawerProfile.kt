package net.primal.android.core.compose.icons.primaliconpack

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp
import net.primal.android.core.compose.icons.PrimalIcons

val PrimalIcons.DrawerProfile: ImageVector
    get() {
        if (_DrawerProfile != null) {
            return _DrawerProfile!!
        }
        _DrawerProfile = ImageVector.Builder(
            name = "DrawerProfile",
            defaultWidth = 20.dp,
            defaultHeight = 20.dp,
            viewportWidth = 20f,
            viewportHeight = 20f
        ).apply {
            path(
                fill = SolidColor(Color(0xFFAAAAAA)),
                pathFillType = PathFillType.EvenOdd
            ) {
                moveTo(15f, 5f)
                curveTo(15f, 7.761f, 12.761f, 10f, 10f, 10f)
                curveTo(7.239f, 10f, 5f, 7.761f, 5f, 5f)
                curveTo(5f, 2.239f, 7.239f, 0f, 10f, 0f)
                curveTo(12.761f, 0f, 15f, 2.239f, 15f, 5f)
                close()
                moveTo(13.75f, 5f)
                curveTo(13.75f, 7.071f, 12.071f, 8.75f, 10f, 8.75f)
                curveTo(7.929f, 8.75f, 6.25f, 7.071f, 6.25f, 5f)
                curveTo(6.25f, 2.929f, 7.929f, 1.25f, 10f, 1.25f)
                curveTo(12.071f, 1.25f, 13.75f, 2.929f, 13.75f, 5f)
                close()
            }
            path(
                fill = SolidColor(Color(0xFFAAAAAA)),
                pathFillType = PathFillType.EvenOdd
            ) {
                moveTo(1f, 16f)
                curveTo(1f, 13.791f, 2.791f, 12f, 5f, 12f)
                horizontalLineTo(15f)
                curveTo(17.209f, 12f, 19f, 13.791f, 19f, 16f)
                curveTo(19f, 18.209f, 17.209f, 20f, 15f, 20f)
                horizontalLineTo(5f)
                curveTo(2.791f, 20f, 1f, 18.209f, 1f, 16f)
                close()
                moveTo(2.25f, 16f)
                curveTo(2.25f, 14.481f, 3.481f, 13.25f, 5f, 13.25f)
                horizontalLineTo(15f)
                curveTo(16.519f, 13.25f, 17.75f, 14.481f, 17.75f, 16f)
                curveTo(17.75f, 17.519f, 16.519f, 18.75f, 15f, 18.75f)
                horizontalLineTo(5f)
                curveTo(3.481f, 18.75f, 2.25f, 17.519f, 2.25f, 16f)
                close()
            }
        }.build()

        return _DrawerProfile!!
    }

@Suppress("ObjectPropertyName")
private var _DrawerProfile: ImageVector? = null
