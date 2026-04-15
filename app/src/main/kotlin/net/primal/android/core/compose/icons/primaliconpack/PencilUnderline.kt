package net.primal.android.core.compose.icons.primaliconpack

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp
import net.primal.android.core.compose.icons.PrimalIcons

val PrimalIcons.PencilUnderline: ImageVector
    get() {
        if (_PencilUnderline != null) {
            return _PencilUnderline!!
        }
        _PencilUnderline = ImageVector.Builder(
            name = "PencilUnderline",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(fill = SolidColor(Color(0xFFAAAAAA))) {
                moveTo(19.915f, 19.338f)
                curveTo(20.354f, 19.338f, 20.711f, 19.71f, 20.711f, 20.169f)
                curveTo(20.711f, 20.628f, 20.354f, 21f, 19.915f, 21f)
                horizontalLineTo(11.962f)
                curveTo(11.523f, 21f, 11.166f, 20.628f, 11.166f, 20.169f)
                curveTo(11.166f, 19.71f, 11.523f, 19.338f, 11.962f, 19.338f)
                horizontalLineTo(19.915f)
                close()
            }
            path(
                fill = SolidColor(Color(0xFFAAAAAA)),
                pathFillType = PathFillType.EvenOdd
            ) {
                moveTo(15.569f, 2.973f)
                curveTo(16.812f, 1.676f, 18.826f, 1.676f, 20.068f, 2.973f)
                curveTo(21.311f, 4.272f, 21.311f, 6.377f, 20.068f, 7.675f)
                lineTo(10.48f, 17.693f)
                curveTo(10.118f, 18.071f, 9.69f, 18.372f, 9.22f, 18.58f)
                lineTo(4.386f, 20.722f)
                curveTo(3.563f, 21.086f, 2.733f, 20.219f, 3.082f, 19.36f)
                lineTo(5.131f, 14.309f)
                curveTo(5.331f, 13.817f, 5.619f, 13.37f, 5.98f, 12.993f)
                lineTo(15.569f, 2.973f)
                close()
                moveTo(18.944f, 4.148f)
                curveTo(18.322f, 3.5f, 17.315f, 3.5f, 16.694f, 4.148f)
                lineTo(7.105f, 14.168f)
                curveTo(6.888f, 14.394f, 6.715f, 14.663f, 6.595f, 14.957f)
                lineTo(5.122f, 18.59f)
                lineTo(8.599f, 17.05f)
                curveTo(8.882f, 16.925f, 9.138f, 16.744f, 9.355f, 16.518f)
                lineTo(18.944f, 6.5f)
                curveTo(19.565f, 5.85f, 19.565f, 4.798f, 18.944f, 4.148f)
                close()
            }
        }.build()

        return _PencilUnderline!!
    }

@Suppress("ObjectPropertyName")
private var _PencilUnderline: ImageVector? = null
