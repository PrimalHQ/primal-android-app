package net.primal.android.core.compose.icons.primaliconpack

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp
import net.primal.android.core.compose.icons.PrimalIcons

val PrimalIcons.MediaVideoFilled: ImageVector
    get() {
        if (_MediaVideoFilled != null) {
            return _MediaVideoFilled!!
        }
        _MediaVideoFilled = ImageVector.Builder(
            name = "MediaVideoFilled",
            defaultWidth = 18.dp,
            defaultHeight = 18.dp,
            viewportWidth = 18f,
            viewportHeight = 18f
        ).apply {
            path(
                fill = SolidColor(Color(0xFFFFFFFF)),
                pathFillType = PathFillType.EvenOdd
            ) {
                moveTo(3f, 0.75f)
                curveTo(1.343f, 0.75f, 0f, 2.093f, 0f, 3.75f)
                verticalLineTo(14.25f)
                curveTo(0f, 15.907f, 1.343f, 17.25f, 3f, 17.25f)
                horizontalLineTo(15f)
                curveTo(16.657f, 17.25f, 18f, 15.907f, 18f, 14.25f)
                verticalLineTo(3.75f)
                curveTo(18f, 2.093f, 16.657f, 0.75f, 15f, 0.75f)
                horizontalLineTo(3f)
                close()
            }
            path(
                fill = SolidColor(Color(0xFF000000)),
                pathFillType = PathFillType.EvenOdd
            ) {
                moveTo(12.305f, 8.28f)
                lineTo(7.165f, 5.711f)
                curveTo(6.63f, 5.443f, 6f, 5.833f, 6f, 6.431f)
                verticalLineTo(11.569f)
                curveTo(6f, 12.167f, 6.63f, 12.557f, 7.165f, 12.289f)
                lineTo(12.305f, 9.72f)
                curveTo(12.898f, 9.423f, 12.898f, 8.577f, 12.305f, 8.28f)
                close()
            }
        }.build()

        return _MediaVideoFilled!!
    }

@Suppress("ObjectPropertyName")
private var _MediaVideoFilled: ImageVector? = null
