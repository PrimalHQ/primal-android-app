@file:Suppress("MagicNumber")

package net.primal.android.core.compose.icons.primaliconpack

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp
import net.primal.android.core.compose.icons.PrimalIcons

val PrimalIcons.Explore: ImageVector
    get() {
        if (_Explore != null) {
            return _Explore!!
        }
        _Explore = ImageVector.Builder(
            name = "Explore",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(
                fill = SolidColor(Color(0xFFAAAAAA)),
                pathFillType = PathFillType.EvenOdd
            ) {
                moveTo(16.977f, 6.235f)
                curveTo(17.414f, 5.999f, 17.889f, 6.475f, 17.654f, 6.912f)
                lineTo(14.192f, 13.342f)
                curveTo(14.006f, 13.687f, 13.724f, 13.97f, 13.379f, 14.155f)
                lineTo(6.948f, 17.618f)
                curveTo(6.512f, 17.853f, 6.036f, 17.377f, 6.271f, 16.941f)
                lineTo(9.734f, 10.51f)
                curveTo(9.919f, 10.165f, 10.202f, 9.883f, 10.547f, 9.697f)
                lineTo(16.977f, 6.235f)
                close()
                moveTo(13.25f, 12f)
                curveTo(13.25f, 12.69f, 12.69f, 13.25f, 12f, 13.25f)
                curveTo(11.31f, 13.25f, 10.75f, 12.69f, 10.75f, 12f)
                curveTo(10.75f, 11.31f, 11.31f, 10.75f, 12f, 10.75f)
                curveTo(12.69f, 10.75f, 13.25f, 11.31f, 13.25f, 12f)
                close()
            }
            path(
                fill = SolidColor(Color(0xFFAAAAAA)),
                pathFillType = PathFillType.EvenOdd
            ) {
                moveTo(23f, 12f)
                curveTo(23f, 18.075f, 18.075f, 23f, 12f, 23f)
                curveTo(5.925f, 23f, 1f, 18.075f, 1f, 12f)
                curveTo(1f, 5.925f, 5.925f, 1f, 12f, 1f)
                curveTo(18.075f, 1f, 23f, 5.925f, 23f, 12f)
                close()
                moveTo(21.75f, 12f)
                curveTo(21.75f, 17.385f, 17.385f, 21.75f, 12f, 21.75f)
                curveTo(6.615f, 21.75f, 2.25f, 17.385f, 2.25f, 12f)
                curveTo(2.25f, 6.615f, 6.615f, 2.25f, 12f, 2.25f)
                curveTo(17.385f, 2.25f, 21.75f, 6.615f, 21.75f, 12f)
                close()
            }
        }.build()

        return _Explore!!
    }

@Suppress("ObjectPropertyName")
private var _Explore: ImageVector? = null
