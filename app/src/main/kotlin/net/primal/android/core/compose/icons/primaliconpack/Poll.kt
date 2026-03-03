package net.primal.android.core.compose.icons.primaliconpack

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp
import net.primal.android.core.compose.icons.PrimalIcons

val PrimalIcons.Poll: ImageVector
    get() {
        if (_Poll != null) {
            return _Poll!!
        }
        _Poll = ImageVector.Builder(
            name = "Poll",
            defaultWidth = 22.dp,
            defaultHeight = 22.dp,
            viewportWidth = 22f,
            viewportHeight = 22f
        ).apply {
            path(
                fill = SolidColor(Color(0xFFAAAAAA)),
                pathFillType = PathFillType.EvenOdd
            ) {
                moveTo(3.5f, 12.5f)
                curveTo(5.433f, 12.5f, 7f, 14.067f, 7f, 16f)
                curveTo(7f, 17.933f, 5.433f, 19.5f, 3.5f, 19.5f)
                curveTo(1.567f, 19.5f, 0f, 17.933f, 0f, 16f)
                curveTo(0f, 14.067f, 1.567f, 12.5f, 3.5f, 12.5f)
                close()
                moveTo(3.5f, 14f)
                curveTo(2.395f, 14f, 1.5f, 14.895f, 1.5f, 16f)
                curveTo(1.5f, 17.105f, 2.395f, 18f, 3.5f, 18f)
                curveTo(4.605f, 18f, 5.5f, 17.105f, 5.5f, 16f)
                curveTo(5.5f, 14.895f, 4.605f, 14f, 3.5f, 14f)
                close()
            }
            path(fill = SolidColor(Color(0xFFAAAAAA))) {
                moveTo(21.25f, 15.25f)
                curveTo(21.664f, 15.25f, 22f, 15.586f, 22f, 16f)
                curveTo(22f, 16.414f, 21.664f, 16.75f, 21.25f, 16.75f)
                horizontalLineTo(10f)
                curveTo(9.586f, 16.75f, 9.25f, 16.414f, 9.25f, 16f)
                curveTo(9.25f, 15.586f, 9.586f, 15.25f, 10f, 15.25f)
                horizontalLineTo(21.25f)
                close()
            }
            path(
                fill = SolidColor(Color(0xFFAAAAAA)),
                pathFillType = PathFillType.EvenOdd
            ) {
                moveTo(3.5f, 2.5f)
                curveTo(5.433f, 2.5f, 7f, 4.067f, 7f, 6f)
                curveTo(7f, 7.933f, 5.433f, 9.5f, 3.5f, 9.5f)
                curveTo(1.567f, 9.5f, 0f, 7.933f, 0f, 6f)
                curveTo(0f, 4.067f, 1.567f, 2.5f, 3.5f, 2.5f)
                close()
                moveTo(3.5f, 4f)
                curveTo(2.395f, 4f, 1.5f, 4.895f, 1.5f, 6f)
                curveTo(1.5f, 7.105f, 2.395f, 8f, 3.5f, 8f)
                curveTo(4.605f, 8f, 5.5f, 7.105f, 5.5f, 6f)
                curveTo(5.5f, 4.895f, 4.605f, 4f, 3.5f, 4f)
                close()
            }
            path(fill = SolidColor(Color(0xFFAAAAAA))) {
                moveTo(21.25f, 5.25f)
                curveTo(21.664f, 5.25f, 22f, 5.586f, 22f, 6f)
                curveTo(22f, 6.414f, 21.664f, 6.75f, 21.25f, 6.75f)
                horizontalLineTo(10f)
                curveTo(9.586f, 6.75f, 9.25f, 6.414f, 9.25f, 6f)
                curveTo(9.25f, 5.586f, 9.586f, 5.25f, 10f, 5.25f)
                horizontalLineTo(21.25f)
                close()
            }
        }.build()

        return _Poll!!
    }

@Suppress("ObjectPropertyName")
private var _Poll: ImageVector? = null
