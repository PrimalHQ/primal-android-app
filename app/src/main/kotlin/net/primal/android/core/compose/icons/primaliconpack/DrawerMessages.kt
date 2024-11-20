package net.primal.android.core.compose.icons.primaliconpack

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp
import net.primal.android.core.compose.icons.PrimalIcons

val PrimalIcons.DrawerMessages: ImageVector
    get() {
        if (_DrawerMessages != null) {
            return _DrawerMessages!!
        }
        _DrawerMessages = ImageVector.Builder(
            name = "DrawerMessages",
            defaultWidth = 20.dp,
            defaultHeight = 20.dp,
            viewportWidth = 20f,
            viewportHeight = 20f
        ).apply {
            path(
                fill = SolidColor(Color(0xFFAAAAAA)),
                pathFillType = PathFillType.EvenOdd
            ) {
                moveTo(1f, 5.385f)
                curveTo(1f, 3.791f, 2.286f, 2.5f, 3.872f, 2.5f)
                horizontalLineTo(16.128f)
                curveTo(17.714f, 2.5f, 19f, 3.791f, 19f, 5.385f)
                verticalLineTo(14.615f)
                curveTo(19f, 16.208f, 17.714f, 17.5f, 16.128f, 17.5f)
                horizontalLineTo(3.872f)
                curveTo(2.286f, 17.5f, 1f, 16.208f, 1f, 14.615f)
                verticalLineTo(5.385f)
                close()
                moveTo(3.872f, 3.654f)
                curveTo(3.469f, 3.654f, 3.098f, 3.793f, 2.805f, 4.026f)
                curveTo(2.664f, 4.137f, 2.61f, 4.3f, 2.626f, 4.454f)
                curveTo(2.642f, 4.605f, 2.724f, 4.747f, 2.852f, 4.842f)
                lineTo(9.435f, 9.68f)
                curveTo(9.771f, 9.928f, 10.229f, 9.928f, 10.566f, 9.68f)
                lineTo(17.147f, 4.842f)
                curveTo(17.276f, 4.747f, 17.358f, 4.605f, 17.374f, 4.454f)
                curveTo(17.39f, 4.3f, 17.336f, 4.137f, 17.195f, 4.026f)
                curveTo(16.902f, 3.793f, 16.531f, 3.654f, 16.128f, 3.654f)
                horizontalLineTo(3.872f)
                close()
                moveTo(2.756f, 6.184f)
                curveTo(2.503f, 6f, 2.149f, 6.182f, 2.149f, 6.496f)
                verticalLineTo(14.615f)
                curveTo(2.149f, 15.571f, 2.921f, 16.346f, 3.872f, 16.346f)
                horizontalLineTo(16.128f)
                curveTo(17.08f, 16.346f, 17.851f, 15.571f, 17.851f, 14.615f)
                verticalLineTo(6.496f)
                curveTo(17.851f, 6.182f, 17.497f, 6f, 17.244f, 6.184f)
                lineTo(10.785f, 10.873f)
                curveTo(10.317f, 11.214f, 9.683f, 11.214f, 9.215f, 10.873f)
                lineTo(2.756f, 6.184f)
                close()
            }
        }.build()

        return _DrawerMessages!!
    }

@Suppress("ObjectPropertyName")
private var _DrawerMessages: ImageVector? = null
