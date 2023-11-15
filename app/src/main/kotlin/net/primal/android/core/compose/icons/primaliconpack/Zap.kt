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

public val PrimalIcons.Zap: ImageVector
    get() {
        if (_zap != null) {
            return _zap!!
        }
        _zap = Builder(name = "Zap", defaultWidth = 24.0.dp, defaultHeight = 24.0.dp, viewportWidth
                = 24.0f, viewportHeight = 24.0f).apply {
            path(fill = SolidColor(Color(0xFFffffff)), stroke = null, strokeLineWidth = 0.0f,
                    strokeLineCap = Butt, strokeLineJoin = Miter, strokeLineMiter = 4.0f,
                    pathFillType = EvenOdd) {
                moveTo(7.3971f, 5.2046f)
                curveTo(8.6377f, 3.2119f, 10.8218f, 2.0f, 13.1726f, 2.0f)
                horizontalLineTo(18.5252f)
                curveTo(19.3503f, 2.0f, 19.8255f, 2.9358f, 19.3375f, 3.5999f)
                lineTo(16.2165f, 7.8464f)
                horizontalLineTo(19.4906f)
                curveTo(20.4268f, 7.8464f, 20.8567f, 9.0096f, 20.1447f, 9.6163f)
                lineTo(5.8416f, 21.8029f)
                curveTo(5.2136f, 22.338f, 4.2814f, 21.6963f, 4.5596f, 20.9204f)
                lineTo(7.4872f, 12.7539f)
                horizontalLineTo(4.5089f)
                curveTo(3.7189f, 12.7539f, 3.2364f, 11.8877f, 3.6533f, 11.218f)
                lineTo(7.3971f, 5.2046f)
                close()
                moveTo(13.1726f, 3.508f)
                curveTo(11.3442f, 3.508f, 9.6455f, 4.4505f, 8.6805f, 6.0005f)
                lineTo(5.4149f, 11.2459f)
                horizontalLineTo(9.6326f)
                lineTo(6.8742f, 18.9404f)
                lineTo(18.125f, 9.3543f)
                horizontalLineTo(13.2344f)
                lineTo(17.5311f, 3.508f)
                horizontalLineTo(13.1726f)
                close()
            }
        }
        .build()
        return _zap!!
    }

private var _zap: ImageVector? = null
