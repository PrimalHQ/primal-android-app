@file:Suppress("MagicNumber")

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

public val PrimalIcons.Read: ImageVector
    get() {
        if (_read != null) {
            return _read!!
        }
        _read = Builder(name = "Read", defaultWidth = 25.0.dp, defaultHeight = 24.0.dp,
                viewportWidth = 25.0f, viewportHeight = 24.0f).apply {
            path(fill = SolidColor(Color(0xFFAAAAAA)), stroke = null, strokeLineWidth = 0.0f,
                    strokeLineCap = Butt, strokeLineJoin = Miter, strokeLineMiter = 4.0f,
                    pathFillType = NonZero) {
                moveTo(1.0029f, 2.0f)
                curveTo(0.4506f, 2.0f, 0.0029f, 2.4477f, 0.0029f, 3.0f)
                curveTo(0.0029f, 3.5523f, 0.4506f, 4.0f, 1.0029f, 4.0f)
                horizontalLineTo(23.0029f)
                curveTo(23.5552f, 4.0f, 24.0029f, 3.5523f, 24.0029f, 3.0f)
                curveTo(24.0029f, 2.4477f, 23.5552f, 2.0f, 23.0029f, 2.0f)
                horizontalLineTo(1.0029f)
                close()
            }
            path(fill = SolidColor(Color(0xFFAAAAAA)), stroke = null, strokeLineWidth = 0.0f,
                    strokeLineCap = Butt, strokeLineJoin = Miter, strokeLineMiter = 4.0f,
                    pathFillType = NonZero) {
                moveTo(1.0029f, 8.0f)
                curveTo(0.4506f, 8.0f, 0.0029f, 8.4477f, 0.0029f, 9.0f)
                curveTo(0.0029f, 9.5523f, 0.4506f, 10.0f, 1.0029f, 10.0f)
                horizontalLineTo(23.0029f)
                curveTo(23.5552f, 10.0f, 24.0029f, 9.5523f, 24.0029f, 9.0f)
                curveTo(24.0029f, 8.4477f, 23.5552f, 8.0f, 23.0029f, 8.0f)
                horizontalLineTo(1.0029f)
                close()
            }
            path(fill = SolidColor(Color(0xFFAAAAAA)), stroke = null, strokeLineWidth = 0.0f,
                    strokeLineCap = Butt, strokeLineJoin = Miter, strokeLineMiter = 4.0f,
                    pathFillType = NonZero) {
                moveTo(0.0029f, 15.0f)
                curveTo(0.0029f, 14.4477f, 0.4506f, 14.0f, 1.0029f, 14.0f)
                horizontalLineTo(23.0029f)
                curveTo(23.5552f, 14.0f, 24.0029f, 14.4477f, 24.0029f, 15.0f)
                curveTo(24.0029f, 15.5523f, 23.5552f, 16.0f, 23.0029f, 16.0f)
                horizontalLineTo(1.0029f)
                curveTo(0.4506f, 16.0f, 0.0029f, 15.5523f, 0.0029f, 15.0f)
                close()
            }
            path(fill = SolidColor(Color(0xFFAAAAAA)), stroke = null, strokeLineWidth = 0.0f,
                    strokeLineCap = Butt, strokeLineJoin = Miter, strokeLineMiter = 4.0f,
                    pathFillType = NonZero) {
                moveTo(1.0029f, 20.0f)
                curveTo(0.4506f, 20.0f, 0.0029f, 20.4477f, 0.0029f, 21.0f)
                curveTo(0.0029f, 21.5523f, 0.4506f, 22.0f, 1.0029f, 22.0f)
                horizontalLineTo(15.0029f)
                curveTo(15.5552f, 22.0f, 16.0029f, 21.5523f, 16.0029f, 21.0f)
                curveTo(16.0029f, 20.4477f, 15.5552f, 20.0f, 15.0029f, 20.0f)
                horizontalLineTo(1.0029f)
                close()
            }
        }
        .build()
        return _read!!
    }

private var _read: ImageVector? = null
