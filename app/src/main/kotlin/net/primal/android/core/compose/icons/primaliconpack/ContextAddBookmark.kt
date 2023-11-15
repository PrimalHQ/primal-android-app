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

public val PrimalIcons.ContextAddBookmark: ImageVector
    get() {
        if (_contextAddBookmark != null) {
            return _contextAddBookmark!!
        }
        _contextAddBookmark = Builder(name = "ContextAddBookmark", defaultWidth = 20.0.dp,
                defaultHeight = 20.0.dp, viewportWidth = 20.0f, viewportHeight = 20.0f).apply {
            path(fill = SolidColor(Color(0xFFffffff)), stroke = null, strokeLineWidth = 0.0f,
                    strokeLineCap = Butt, strokeLineJoin = Miter, strokeLineMiter = 4.0f,
                    pathFillType = EvenOdd) {
                moveTo(9.3492f, 12.5822f)
                curveTo(9.7237f, 12.2612f, 10.2763f, 12.2612f, 10.6508f, 12.5822f)
                lineTo(15.5f, 16.7387f)
                verticalLineTo(3.0f)
                curveTo(15.5f, 2.7239f, 15.2761f, 2.5f, 15.0f, 2.5f)
                horizontalLineTo(5.0f)
                curveTo(4.7239f, 2.5f, 4.5f, 2.7239f, 4.5f, 3.0f)
                verticalLineTo(16.7387f)
                lineTo(9.3492f, 12.5822f)
                close()
                moveTo(10.0f, 14.0f)
                lineTo(4.6508f, 18.585f)
                curveTo(4.0021f, 19.141f, 3.0f, 18.6801f, 3.0f, 17.8258f)
                verticalLineTo(3.0f)
                curveTo(3.0f, 1.8954f, 3.8954f, 1.0f, 5.0f, 1.0f)
                horizontalLineTo(15.0f)
                curveTo(16.1046f, 1.0f, 17.0f, 1.8954f, 17.0f, 3.0f)
                verticalLineTo(17.8258f)
                curveTo(17.0f, 18.6801f, 15.9979f, 19.141f, 15.3492f, 18.585f)
                lineTo(10.0f, 14.0f)
                close()
            }
        }
        .build()
        return _contextAddBookmark!!
    }

private var _contextAddBookmark: ImageVector? = null
