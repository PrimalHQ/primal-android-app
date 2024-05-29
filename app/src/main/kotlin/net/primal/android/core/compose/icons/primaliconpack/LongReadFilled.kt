package net.primal.android.core.compose.icons.primaliconpack

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType.Companion.NonZero
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap.Companion.Butt
import androidx.compose.ui.graphics.StrokeJoin.Companion.Miter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.ImageVector.Builder
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp
import net.primal.android.core.compose.icons.PrimalIcons

public val PrimalIcons.LongReadFilled: ImageVector
    get() {
        if (_longreadfilled != null) {
            return _longreadfilled!!
        }
        _longreadfilled = Builder(name = "Longreadfilled", defaultWidth = 24.0.dp, defaultHeight =
                24.0.dp, viewportWidth = 24.0f, viewportHeight = 24.0f).apply {
            path(fill = SolidColor(Color(0xFFffffff)), stroke = null, strokeLineWidth = 0.0f,
                    strokeLineCap = Butt, strokeLineJoin = Miter, strokeLineMiter = 4.0f,
                    pathFillType = NonZero) {
                moveTo(1.5f, 2.0f)
                curveTo(0.9477f, 2.0f, 0.5f, 2.4477f, 0.5f, 3.0f)
                curveTo(0.5f, 3.5523f, 0.9477f, 4.0f, 1.5f, 4.0f)
                horizontalLineTo(22.5f)
                curveTo(23.0523f, 4.0f, 23.5f, 3.5523f, 23.5f, 3.0f)
                curveTo(23.5f, 2.4477f, 23.0523f, 2.0f, 22.5f, 2.0f)
                horizontalLineTo(1.5f)
                close()
            }
            path(fill = SolidColor(Color(0xFFffffff)), stroke = null, strokeLineWidth = 0.0f,
                    strokeLineCap = Butt, strokeLineJoin = Miter, strokeLineMiter = 4.0f,
                    pathFillType = NonZero) {
                moveTo(1.5f, 8.0f)
                curveTo(0.9477f, 8.0f, 0.5f, 8.4477f, 0.5f, 9.0f)
                curveTo(0.5f, 9.5523f, 0.9477f, 10.0f, 1.5f, 10.0f)
                horizontalLineTo(22.5f)
                curveTo(23.0523f, 10.0f, 23.5f, 9.5523f, 23.5f, 9.0f)
                curveTo(23.5f, 8.4477f, 23.0523f, 8.0f, 22.5f, 8.0f)
                horizontalLineTo(1.5f)
                close()
            }
            path(fill = SolidColor(Color(0xFFffffff)), stroke = null, strokeLineWidth = 0.0f,
                    strokeLineCap = Butt, strokeLineJoin = Miter, strokeLineMiter = 4.0f,
                    pathFillType = NonZero) {
                moveTo(0.5f, 15.0f)
                curveTo(0.5f, 14.4477f, 0.9477f, 14.0f, 1.5f, 14.0f)
                horizontalLineTo(22.5f)
                curveTo(23.0523f, 14.0f, 23.5f, 14.4477f, 23.5f, 15.0f)
                curveTo(23.5f, 15.5523f, 23.0523f, 16.0f, 22.5f, 16.0f)
                horizontalLineTo(1.5f)
                curveTo(0.9477f, 16.0f, 0.5f, 15.5523f, 0.5f, 15.0f)
                close()
            }
            path(fill = SolidColor(Color(0xFFffffff)), stroke = null, strokeLineWidth = 0.0f,
                    strokeLineCap = Butt, strokeLineJoin = Miter, strokeLineMiter = 4.0f,
                    pathFillType = NonZero) {
                moveTo(1.5f, 20.0f)
                curveTo(0.9477f, 20.0f, 0.5f, 20.4477f, 0.5f, 21.0f)
                curveTo(0.5f, 21.5523f, 0.9477f, 22.0f, 1.5f, 22.0f)
                horizontalLineTo(14.5f)
                curveTo(15.0523f, 22.0f, 15.5f, 21.5523f, 15.5f, 21.0f)
                curveTo(15.5f, 20.4477f, 15.0523f, 20.0f, 14.5f, 20.0f)
                horizontalLineTo(1.5f)
                close()
            }
        }
        .build()
        return _longreadfilled!!
    }

private var _longreadfilled: ImageVector? = null
