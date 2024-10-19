@file:Suppress("MagicNumber")

package net.primal.android.core.compose.icons.primaliconpack

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp
import net.primal.android.core.compose.icons.PrimalIcons

val PrimalIcons.ExploreFilled: ImageVector
    get() {
        if (_ExploreFilled != null) {
            return _ExploreFilled!!
        }
        _ExploreFilled = ImageVector.Builder(
            name = "ExploreFilled",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(fill = SolidColor(Color(0xFFFFFFFF))) {
                moveTo(13.25f, 12f)
                curveTo(13.25f, 12.69f, 12.69f, 13.25f, 12f, 13.25f)
                curveTo(11.31f, 13.25f, 10.75f, 12.69f, 10.75f, 12f)
                curveTo(10.75f, 11.31f, 11.31f, 10.75f, 12f, 10.75f)
                curveTo(12.69f, 10.75f, 13.25f, 11.31f, 13.25f, 12f)
                close()
            }
            path(
                fill = SolidColor(Color(0xFFFFFFFF)),
                pathFillType = PathFillType.EvenOdd
            ) {
                moveTo(12f, 23f)
                curveTo(18.075f, 23f, 23f, 18.075f, 23f, 12f)
                curveTo(23f, 5.925f, 18.075f, 1f, 12f, 1f)
                curveTo(5.925f, 1f, 1f, 5.925f, 1f, 12f)
                curveTo(1f, 18.075f, 5.925f, 23f, 12f, 23f)
                close()
                moveTo(16.74f, 5.794f)
                curveTo(17.614f, 5.324f, 18.565f, 6.276f, 18.095f, 7.149f)
                lineTo(14.632f, 13.58f)
                curveTo(14.4f, 14.01f, 14.047f, 14.364f, 13.616f, 14.596f)
                lineTo(7.185f, 18.058f)
                curveTo(6.312f, 18.529f, 5.361f, 17.577f, 5.831f, 16.704f)
                lineTo(9.293f, 10.273f)
                curveTo(9.525f, 9.842f, 9.879f, 9.489f, 10.309f, 9.257f)
                lineTo(16.74f, 5.794f)
                close()
            }
        }.build()

        return _ExploreFilled!!
    }

@Suppress("ObjectPropertyName")
private var _ExploreFilled: ImageVector? = null
