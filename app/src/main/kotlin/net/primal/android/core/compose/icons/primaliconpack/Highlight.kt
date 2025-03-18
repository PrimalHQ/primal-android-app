package net.primal.android.core.compose.icons.primaliconpack

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp
import net.primal.android.core.compose.icons.PrimalIcons

val PrimalIcons.Highlight: ImageVector
    get() {
        if (_Highlight != null) {
            return _Highlight!!
        }
        _Highlight = ImageVector.Builder(
            name = "Highlight",
            defaultWidth = 20.dp,
            defaultHeight = 20.dp,
            viewportWidth = 20f,
            viewportHeight = 20f
        ).apply {
            path(
                fill = SolidColor(Color(0xFFFFFFFF)),
                pathFillType = PathFillType.EvenOdd
            ) {
                moveTo(13.5f, 1.732f)
                curveTo(15.142f, 0.605f, 17.373f, 0.797f, 18.79f, 2.186f)
                curveTo(20.207f, 3.575f, 20.403f, 5.761f, 19.253f, 7.371f)
                lineTo(12.174f, 17.284f)
                curveTo(10.938f, 19.015f, 8.408f, 19.232f, 6.884f, 17.738f)
                lineTo(2.923f, 13.856f)
                curveTo(1.4f, 12.363f, 1.621f, 9.883f, 3.387f, 8.671f)
                lineTo(13.5f, 1.732f)
                close()
                moveTo(11.438f, 13.395f)
                curveTo(10.311f, 14.501f, 8.482f, 14.501f, 7.354f, 13.395f)
                curveTo(6.226f, 12.29f, 6.226f, 10.497f, 7.354f, 9.392f)
                curveTo(8.482f, 8.286f, 10.311f, 8.286f, 11.438f, 9.392f)
                curveTo(12.566f, 10.497f, 12.566f, 12.29f, 11.438f, 13.395f)
                close()
            }
            path(fill = SolidColor(Color(0xFFFFFFFF))) {
                moveTo(5.72f, 18.199f)
                lineTo(2.453f, 14.997f)
                lineTo(0.341f, 17.067f)
                curveTo(-0.387f, 17.78f, 0.128f, 19f, 1.158f, 19f)
                lineTo(4.425f, 19f)
                curveTo(4.731f, 19f, 5.025f, 18.881f, 5.242f, 18.668f)
                lineTo(5.72f, 18.199f)
                close()
            }
        }.build()

        return _Highlight!!
    }

@Suppress("ObjectPropertyName")
private var _Highlight: ImageVector? = null
