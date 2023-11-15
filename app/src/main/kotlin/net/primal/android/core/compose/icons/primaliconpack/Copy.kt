@file:Suppress("MagicNumber")

package net.primal.android.core.compose.icons.primaliconpack

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType.Companion.EvenOdd
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap.Companion.Butt
import androidx.compose.ui.graphics.StrokeJoin.Companion.Miter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.ImageVector.Builder
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp
import net.primal.android.core.compose.icons.PrimalIcons

public val PrimalIcons.Copy: ImageVector
    get() {
        if (_copy != null) {
            return _copy!!
        }
        _copy = Builder(name = "Copy", defaultWidth = 16.0.dp, defaultHeight = 16.0.dp,
                viewportWidth = 16.0f, viewportHeight = 16.0f).apply {
            path(fill = SolidColor(Color(0xFFffffff)), stroke = null, strokeLineWidth = 0.0f,
                    strokeLineCap = Butt, strokeLineJoin = Miter, strokeLineMiter = 4.0f,
                    pathFillType = EvenOdd) {
                moveTo(11.0f, 4.0f)
                verticalLineTo(3.0f)
                curveTo(11.0f, 1.8954f, 10.1046f, 1.0f, 9.0f, 1.0f)
                horizontalLineTo(4.0f)
                curveTo(2.8954f, 1.0f, 2.0f, 1.8954f, 2.0f, 3.0f)
                verticalLineTo(10.0f)
                curveTo(2.0f, 11.1046f, 2.8954f, 12.0f, 4.0f, 12.0f)
                horizontalLineTo(5.0f)
                verticalLineTo(13.0f)
                curveTo(5.0f, 14.1046f, 5.8954f, 15.0f, 7.0f, 15.0f)
                horizontalLineTo(12.0f)
                curveTo(13.1046f, 15.0f, 14.0f, 14.1046f, 14.0f, 13.0f)
                verticalLineTo(6.0f)
                curveTo(14.0f, 4.8954f, 13.1046f, 4.0f, 12.0f, 4.0f)
                horizontalLineTo(11.0f)
                close()
                moveTo(9.0f, 2.5f)
                horizontalLineTo(4.0f)
                curveTo(3.7239f, 2.5f, 3.5f, 2.7239f, 3.5f, 3.0f)
                verticalLineTo(10.0f)
                curveTo(3.5f, 10.2761f, 3.7239f, 10.5f, 4.0f, 10.5f)
                horizontalLineTo(5.0f)
                verticalLineTo(6.0f)
                curveTo(5.0f, 4.8954f, 5.8954f, 4.0f, 7.0f, 4.0f)
                horizontalLineTo(9.5f)
                verticalLineTo(3.0f)
                curveTo(9.5f, 2.7239f, 9.2761f, 2.5f, 9.0f, 2.5f)
                close()
                moveTo(6.5f, 6.0f)
                curveTo(6.5f, 5.7239f, 6.7239f, 5.5f, 7.0f, 5.5f)
                horizontalLineTo(12.0f)
                curveTo(12.2761f, 5.5f, 12.5f, 5.7239f, 12.5f, 6.0f)
                verticalLineTo(13.0f)
                curveTo(12.5f, 13.2761f, 12.2761f, 13.5f, 12.0f, 13.5f)
                horizontalLineTo(7.0f)
                curveTo(6.7239f, 13.5f, 6.5f, 13.2761f, 6.5f, 13.0f)
                verticalLineTo(6.0f)
                close()
            }
        }
        .build()
        return _copy!!
    }

private var _copy: ImageVector? = null
