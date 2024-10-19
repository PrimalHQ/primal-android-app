package net.primal.android.core.compose.icons.primaliconpack

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp
import net.primal.android.core.compose.icons.PrimalIcons

val PrimalIcons.HomeFilled: ImageVector
    get() {
        if (_HomeFilled != null) {
            return _HomeFilled!!
        }
        _HomeFilled = ImageVector.Builder(
            name = "HomeFilled",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(
                fill = SolidColor(Color(0xFFFFFFFF)),
                pathFillType = PathFillType.EvenOdd
            ) {
                moveTo(21.657f, 9.899f)
                lineTo(12.502f, 3.001f)
                curveTo(12.205f, 2.777f, 11.795f, 2.777f, 11.498f, 3.001f)
                lineTo(2.343f, 9.899f)
                curveTo(1.971f, 10.179f, 1.889f, 10.719f, 2.159f, 11.105f)
                curveTo(2.43f, 11.491f, 2.951f, 11.576f, 3.324f, 11.295f)
                lineTo(4.5f, 10.409f)
                lineTo(5.333f, 19.29f)
                curveTo(5.333f, 20.21f, 6.08f, 20.957f, 7f, 20.957f)
                horizontalLineTo(17f)
                curveTo(17.92f, 20.957f, 18.667f, 20.21f, 18.667f, 19.29f)
                lineTo(19.5f, 10.409f)
                lineTo(20.677f, 11.295f)
                curveTo(21.049f, 11.576f, 21.57f, 11.491f, 21.841f, 11.105f)
                curveTo(22.111f, 10.719f, 22.029f, 10.179f, 21.657f, 9.899f)
                close()
            }
        }.build()

        return _HomeFilled!!
    }

@Suppress("ObjectPropertyName")
private var _HomeFilled: ImageVector? = null
