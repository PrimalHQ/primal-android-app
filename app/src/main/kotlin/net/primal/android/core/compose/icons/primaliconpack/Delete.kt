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

public val PrimalIcons.Delete: ImageVector
    get() {
        if (_delete != null) {
            return _delete!!
        }
        _delete = Builder(name = "Delete", defaultWidth = 24.0.dp, defaultHeight = 24.0.dp,
                viewportWidth = 24.0f, viewportHeight = 24.0f).apply {
            path(fill = SolidColor(Color(0xFFAAAAAA)), stroke = null, strokeLineWidth = 0.0f,
                    strokeLineCap = Butt, strokeLineJoin = Miter, strokeLineMiter = 4.0f,
                    pathFillType = NonZero) {
                moveTo(9.0029f, 2.0f)
                curveTo(9.0029f, 0.8954f, 9.8984f, 0.0f, 11.0029f, 0.0f)
                horizontalLineTo(13.0029f)
                curveTo(14.1075f, 0.0f, 15.0029f, 0.8954f, 15.0029f, 2.0f)
                horizontalLineTo(21.4029f)
                curveTo(22.2866f, 2.0f, 23.0029f, 2.7163f, 23.0029f, 3.6f)
                curveTo(23.0029f, 3.8209f, 22.8238f, 4.0f, 22.6029f, 4.0f)
                horizontalLineTo(1.4029f)
                curveTo(1.182f, 4.0f, 1.0029f, 3.8209f, 1.0029f, 3.6f)
                curveTo(1.0029f, 2.7163f, 1.7193f, 2.0f, 2.6029f, 2.0f)
                horizontalLineTo(9.0029f)
                close()
            }
            path(fill = SolidColor(Color(0xFFAAAAAA)), stroke = null, strokeLineWidth = 0.0f,
                    strokeLineCap = Butt, strokeLineJoin = Miter, strokeLineMiter = 4.0f,
                    pathFillType = NonZero) {
                moveTo(8.5029f, 8.0f)
                curveTo(8.5029f, 7.4477f, 8.9506f, 7.0f, 9.5029f, 7.0f)
                curveTo(10.0552f, 7.0f, 10.5029f, 7.4477f, 10.5029f, 8.0f)
                verticalLineTo(18.0f)
                curveTo(10.5029f, 18.5523f, 10.0552f, 19.0f, 9.5029f, 19.0f)
                curveTo(8.9506f, 19.0f, 8.5029f, 18.5523f, 8.5029f, 18.0f)
                verticalLineTo(8.0f)
                close()
            }
            path(fill = SolidColor(Color(0xFFAAAAAA)), stroke = null, strokeLineWidth = 0.0f,
                    strokeLineCap = Butt, strokeLineJoin = Miter, strokeLineMiter = 4.0f,
                    pathFillType = NonZero) {
                moveTo(14.5029f, 7.0f)
                curveTo(13.9506f, 7.0f, 13.5029f, 7.4477f, 13.5029f, 8.0f)
                verticalLineTo(18.0f)
                curveTo(13.5029f, 18.5523f, 13.9506f, 19.0f, 14.5029f, 19.0f)
                curveTo(15.0552f, 19.0f, 15.5029f, 18.5523f, 15.5029f, 18.0f)
                verticalLineTo(8.0f)
                curveTo(15.5029f, 7.4477f, 15.0552f, 7.0f, 14.5029f, 7.0f)
                close()
            }
            path(fill = SolidColor(Color(0xFFAAAAAA)), stroke = null, strokeLineWidth = 0.0f,
                    strokeLineCap = Butt, strokeLineJoin = Miter, strokeLineMiter = 4.0f,
                    pathFillType = NonZero) {
                moveTo(4.0029f, 6.0f)
                curveTo(3.4506f, 6.0f, 3.0029f, 6.4477f, 3.0029f, 7.0f)
                verticalLineTo(22.0f)
                curveTo(3.0029f, 23.1046f, 3.8984f, 24.0f, 5.0029f, 24.0f)
                horizontalLineTo(19.0029f)
                curveTo(20.1075f, 24.0f, 21.0029f, 23.1046f, 21.0029f, 22.0f)
                verticalLineTo(7.0f)
                curveTo(21.0029f, 6.4477f, 20.5552f, 6.0f, 20.0029f, 6.0f)
                curveTo(19.4506f, 6.0f, 19.0029f, 6.4477f, 19.0029f, 7.0f)
                verticalLineTo(21.0f)
                curveTo(19.0029f, 21.5523f, 18.5552f, 22.0f, 18.0029f, 22.0f)
                horizontalLineTo(6.0029f)
                curveTo(5.4506f, 22.0f, 5.0029f, 21.5523f, 5.0029f, 21.0f)
                verticalLineTo(7.0f)
                curveTo(5.0029f, 6.4477f, 4.5552f, 6.0f, 4.0029f, 6.0f)
                close()
            }
        }
        .build()
        return _delete!!
    }

private var _delete: ImageVector? = null
