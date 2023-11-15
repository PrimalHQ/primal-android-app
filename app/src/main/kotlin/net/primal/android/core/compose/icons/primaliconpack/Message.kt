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

public val PrimalIcons.Message: ImageVector
    get() {
        if (_message != null) {
            return _message!!
        }
        _message = Builder(name = "Message", defaultWidth = 24.0.dp, defaultHeight = 24.0.dp,
                viewportWidth = 24.0f, viewportHeight = 24.0f).apply {
            path(fill = SolidColor(Color(0xFFffffff)), stroke = null, strokeLineWidth = 0.0f,
                    strokeLineCap = Butt, strokeLineJoin = Miter, strokeLineMiter = 4.0f,
                    pathFillType = EvenOdd) {
                moveTo(2.0f, 5.6f)
                curveTo(2.0f, 4.7163f, 2.7462f, 4.0f, 3.6667f, 4.0f)
                horizontalLineTo(20.3333f)
                curveTo(21.2538f, 4.0f, 22.0f, 4.7163f, 22.0f, 5.6f)
                verticalLineTo(18.4f)
                curveTo(22.0f, 19.2837f, 21.2538f, 20.0f, 20.3333f, 20.0f)
                horizontalLineTo(3.6667f)
                curveTo(2.7462f, 20.0f, 2.0f, 19.2837f, 2.0f, 18.4f)
                verticalLineTo(5.6f)
                close()
                moveTo(4.1167f, 5.9546f)
                curveTo(3.9655f, 5.8354f, 4.0532f, 5.6f, 4.2489f, 5.6f)
                horizontalLineTo(19.7511f)
                curveTo(19.9468f, 5.6f, 20.0345f, 5.8354f, 19.8833f, 5.9546f)
                lineTo(13.0574f, 11.3332f)
                curveTo(12.4427f, 11.8175f, 11.5573f, 11.8175f, 10.9426f, 11.3332f)
                lineTo(4.1167f, 5.9546f)
                close()
                moveTo(4.3472f, 8.2227f)
                curveTo(4.0751f, 8.009f, 3.6667f, 8.1948f, 3.6667f, 8.5323f)
                verticalLineTo(17.6f)
                curveTo(3.6667f, 18.0418f, 4.0398f, 18.4f, 4.5f, 18.4f)
                horizontalLineTo(19.5f)
                curveTo(19.9602f, 18.4f, 20.3333f, 18.0418f, 20.3333f, 17.6f)
                verticalLineTo(8.5323f)
                curveTo(20.3333f, 8.1948f, 19.9249f, 8.009f, 19.6528f, 8.2227f)
                lineTo(13.0554f, 13.4047f)
                curveTo(12.4415f, 13.8869f, 11.5585f, 13.8869f, 10.9446f, 13.4047f)
                lineTo(4.3472f, 8.2227f)
                close()
            }
        }
        .build()
        return _message!!
    }

private var _message: ImageVector? = null
