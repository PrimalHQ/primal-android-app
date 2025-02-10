package net.primal.android.core.compose.icons.primaliconpack

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp
import net.primal.android.core.compose.icons.PrimalIcons

val PrimalIcons.AddAccount: ImageVector
    get() {
        if (_AddAccount != null) {
            return _AddAccount!!
        }
        _AddAccount = ImageVector.Builder(
            name = "AddAccount",
            defaultWidth = 26.dp,
            defaultHeight = 26.dp,
            viewportWidth = 26f,
            viewportHeight = 26f
        ).apply {
            path(fill = SolidColor(Color(0xFFAAAAAA))) {
                moveTo(20.222f, 0.963f)
                curveTo(20.222f, 0.431f, 20.653f, 0f, 21.185f, 0f)
                curveTo(21.717f, 0f, 22.148f, 0.431f, 22.148f, 0.963f)
                verticalLineTo(3.852f)
                horizontalLineTo(25.037f)
                curveTo(25.569f, 3.852f, 26f, 4.283f, 26f, 4.815f)
                curveTo(26f, 5.347f, 25.569f, 5.778f, 25.037f, 5.778f)
                horizontalLineTo(22.148f)
                verticalLineTo(8.667f)
                curveTo(22.148f, 9.198f, 21.717f, 9.63f, 21.185f, 9.63f)
                curveTo(20.653f, 9.63f, 20.222f, 9.198f, 20.222f, 8.667f)
                verticalLineTo(5.778f)
                horizontalLineTo(17.333f)
                curveTo(16.802f, 5.778f, 16.37f, 5.347f, 16.37f, 4.815f)
                curveTo(16.37f, 4.283f, 16.802f, 3.852f, 17.333f, 3.852f)
                horizontalLineTo(20.222f)
                verticalLineTo(0.963f)
                close()
            }
            path(
                fill = SolidColor(Color(0xFFAAAAAA)),
                pathFillType = PathFillType.EvenOdd
            ) {
                moveTo(11.556f, 2.889f)
                curveTo(12.695f, 2.889f, 13.796f, 3.054f, 14.836f, 3.361f)
                curveTo(14.587f, 3.788f, 14.444f, 4.285f, 14.444f, 4.815f)
                curveTo(14.444f, 4.969f, 14.457f, 5.12f, 14.48f, 5.267f)
                curveTo(13.557f, 4.973f, 12.575f, 4.815f, 11.556f, 4.815f)
                curveTo(6.237f, 4.815f, 1.926f, 9.126f, 1.926f, 14.444f)
                curveTo(1.926f, 16.479f, 2.557f, 18.366f, 3.633f, 19.92f)
                curveTo(4.557f, 18.371f, 6.25f, 17.333f, 8.185f, 17.333f)
                horizontalLineTo(14.926f)
                curveTo(16.861f, 17.333f, 18.554f, 18.371f, 19.478f, 19.92f)
                curveTo(20.554f, 18.366f, 21.185f, 16.479f, 21.185f, 14.444f)
                curveTo(21.185f, 13.425f, 21.027f, 12.443f, 20.733f, 11.52f)
                curveTo(20.881f, 11.543f, 21.031f, 11.556f, 21.185f, 11.556f)
                curveTo(21.715f, 11.556f, 22.212f, 11.413f, 22.639f, 11.164f)
                curveTo(22.946f, 12.204f, 23.111f, 13.305f, 23.111f, 14.444f)
                curveTo(23.111f, 20.826f, 17.938f, 26f, 11.556f, 26f)
                curveTo(5.174f, 26f, 0f, 20.826f, 0f, 14.444f)
                curveTo(0f, 8.062f, 5.174f, 2.889f, 11.556f, 2.889f)
                close()
                moveTo(11.556f, 24.074f)
                curveTo(14.085f, 24.074f, 16.386f, 23.099f, 18.104f, 21.505f)
                curveTo(17.641f, 20.197f, 16.393f, 19.259f, 14.926f, 19.259f)
                horizontalLineTo(8.185f)
                curveTo(6.718f, 19.259f, 5.47f, 20.197f, 5.007f, 21.505f)
                curveTo(6.725f, 23.099f, 9.027f, 24.074f, 11.556f, 24.074f)
                close()
            }
            path(
                fill = SolidColor(Color(0xFFAAAAAA)),
                pathFillType = PathFillType.EvenOdd
            ) {
                moveTo(15.407f, 11.556f)
                curveTo(15.407f, 13.683f, 13.683f, 15.407f, 11.556f, 15.407f)
                curveTo(9.428f, 15.407f, 7.704f, 13.683f, 7.704f, 11.556f)
                curveTo(7.704f, 9.428f, 9.428f, 7.704f, 11.556f, 7.704f)
                curveTo(13.683f, 7.704f, 15.407f, 9.428f, 15.407f, 11.556f)
                close()
                moveTo(13.481f, 11.556f)
                curveTo(13.481f, 12.619f, 12.619f, 13.481f, 11.556f, 13.481f)
                curveTo(10.492f, 13.481f, 9.63f, 12.619f, 9.63f, 11.556f)
                curveTo(9.63f, 10.492f, 10.492f, 9.63f, 11.556f, 9.63f)
                curveTo(12.619f, 9.63f, 13.481f, 10.492f, 13.481f, 11.556f)
                close()
            }
        }.build()

        return _AddAccount!!
    }

@Suppress("ObjectPropertyName")
private var _AddAccount: ImageVector? = null
