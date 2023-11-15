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

public val PrimalIcons.ContextCopyRawData: ImageVector
    get() {
        if (_contextCopyRawData != null) {
            return _contextCopyRawData!!
        }
        _contextCopyRawData = Builder(name = "ContextCopyRawData", defaultWidth = 20.0.dp,
                defaultHeight = 20.0.dp, viewportWidth = 20.0f, viewportHeight = 20.0f).apply {
            path(fill = SolidColor(Color(0xFFffffff)), stroke = null, strokeLineWidth = 0.0f,
                    strokeLineCap = Butt, strokeLineJoin = Miter, strokeLineMiter = 4.0f,
                    pathFillType = NonZero) {
                moveTo(2.0f, 1.5f)
                horizontalLineTo(13.0f)
                curveTo(13.2761f, 1.5f, 13.5f, 1.7239f, 13.5f, 2.0f)
                verticalLineTo(2.25f)
                curveTo(13.5f, 2.6642f, 13.8358f, 3.0f, 14.25f, 3.0f)
                curveTo(14.6642f, 3.0f, 15.0f, 2.6642f, 15.0f, 2.25f)
                verticalLineTo(2.0f)
                curveTo(15.0f, 0.8954f, 14.1046f, 0.0f, 13.0f, 0.0f)
                horizontalLineTo(2.0f)
                curveTo(0.8954f, 0.0f, 0.0f, 0.8954f, 0.0f, 2.0f)
                verticalLineTo(13.0f)
                curveTo(0.0f, 14.1046f, 0.8954f, 15.0f, 2.0f, 15.0f)
                horizontalLineTo(3.25f)
                curveTo(3.6642f, 15.0f, 4.0f, 14.6642f, 4.0f, 14.25f)
                curveTo(4.0f, 13.8358f, 3.6642f, 13.5f, 3.25f, 13.5f)
                horizontalLineTo(2.0f)
                curveTo(1.7239f, 13.5f, 1.5f, 13.2761f, 1.5f, 13.0f)
                verticalLineTo(2.0f)
                curveTo(1.5f, 1.7239f, 1.7239f, 1.5f, 2.0f, 1.5f)
                close()
            }
            path(fill = SolidColor(Color(0xFFffffff)), stroke = null, strokeLineWidth = 0.0f,
                    strokeLineCap = Butt, strokeLineJoin = Miter, strokeLineMiter = 4.0f,
                    pathFillType = NonZero) {
                moveTo(10.0f, 5.0f)
                curveTo(8.3432f, 5.0f, 7.0f, 6.3432f, 7.0f, 8.0f)
                verticalLineTo(11.0f)
                horizontalLineTo(5.75f)
                curveTo(5.3358f, 11.0f, 5.0f, 11.3358f, 5.0f, 11.75f)
                curveTo(5.0f, 12.1642f, 5.3358f, 12.5f, 5.75f, 12.5f)
                horizontalLineTo(7.0f)
                verticalLineTo(16.0f)
                curveTo(7.0f, 17.6569f, 8.3432f, 19.0f, 10.0f, 19.0f)
                horizontalLineTo(10.25f)
                curveTo(10.6642f, 19.0f, 11.0f, 18.6642f, 11.0f, 18.25f)
                curveTo(11.0f, 17.8358f, 10.6642f, 17.5f, 10.25f, 17.5f)
                horizontalLineTo(10.0f)
                curveTo(9.1716f, 17.5f, 8.5f, 16.8284f, 8.5f, 16.0f)
                verticalLineTo(8.0f)
                curveTo(8.5f, 7.1716f, 9.1716f, 6.5f, 10.0f, 6.5f)
                horizontalLineTo(10.25f)
                curveTo(10.6642f, 6.5f, 11.0f, 6.1642f, 11.0f, 5.75f)
                curveTo(11.0f, 5.3358f, 10.6642f, 5.0f, 10.25f, 5.0f)
                horizontalLineTo(10.0f)
                close()
            }
            path(fill = SolidColor(Color(0xFFffffff)), stroke = null, strokeLineWidth = 0.0f,
                    strokeLineCap = Butt, strokeLineJoin = Miter, strokeLineMiter = 4.0f,
                    pathFillType = NonZero) {
                moveTo(15.0f, 5.0f)
                curveTo(16.6569f, 5.0f, 18.0f, 6.3432f, 18.0f, 8.0f)
                verticalLineTo(11.0f)
                horizontalLineTo(19.25f)
                curveTo(19.6642f, 11.0f, 20.0f, 11.3358f, 20.0f, 11.75f)
                curveTo(20.0f, 12.1642f, 19.6642f, 12.5f, 19.25f, 12.5f)
                horizontalLineTo(18.0f)
                verticalLineTo(16.0f)
                curveTo(18.0f, 17.6569f, 16.6569f, 19.0f, 15.0f, 19.0f)
                horizontalLineTo(14.75f)
                curveTo(14.3358f, 19.0f, 14.0f, 18.6642f, 14.0f, 18.25f)
                curveTo(14.0f, 17.8358f, 14.3358f, 17.5f, 14.75f, 17.5f)
                horizontalLineTo(15.0f)
                curveTo(15.8284f, 17.5f, 16.5f, 16.8284f, 16.5f, 16.0f)
                verticalLineTo(8.0f)
                curveTo(16.5f, 7.1716f, 15.8284f, 6.5f, 15.0f, 6.5f)
                horizontalLineTo(14.75f)
                curveTo(14.3358f, 6.5f, 14.0f, 6.1642f, 14.0f, 5.75f)
                curveTo(14.0f, 5.3358f, 14.3358f, 5.0f, 14.75f, 5.0f)
                horizontalLineTo(15.0f)
                close()
            }
        }
        .build()
        return _contextCopyRawData!!
    }

private var _contextCopyRawData: ImageVector? = null
