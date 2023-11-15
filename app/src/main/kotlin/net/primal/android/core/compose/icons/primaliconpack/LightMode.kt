@file:Suppress("MagicNumber")

package net.primal.android.core.compose.icons.primaliconpack

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType.Companion.EvenOdd
import androidx.compose.ui.graphics.PathFillType.Companion.NonZero
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap.Companion.Butt
import androidx.compose.ui.graphics.StrokeJoin.Companion.Miter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.ImageVector.Builder
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp
import net.primal.android.core.compose.icons.PrimalIcons

public val PrimalIcons.LightMode: ImageVector
    get() {
        if (_lightMode != null) {
            return _lightMode!!
        }
        _lightMode = Builder(name = "LightMode", defaultWidth = 28.0.dp, defaultHeight = 28.0.dp,
                viewportWidth = 28.0f, viewportHeight = 28.0f).apply {
            path(fill = SolidColor(Color(0xFF666666)), stroke = null, strokeLineWidth = 0.0f,
                    strokeLineCap = Butt, strokeLineJoin = Miter, strokeLineMiter = 4.0f,
                    pathFillType = NonZero) {
                moveTo(13.0f, 1.0f)
                curveTo(13.0f, 0.4477f, 13.4477f, 0.0f, 14.0f, 0.0f)
                curveTo(14.5523f, 0.0f, 15.0f, 0.4477f, 15.0f, 1.0f)
                verticalLineTo(3.0f)
                curveTo(15.0f, 3.5523f, 14.5523f, 4.0f, 14.0f, 4.0f)
                curveTo(13.4477f, 4.0f, 13.0f, 3.5523f, 13.0f, 3.0f)
                verticalLineTo(1.0f)
                close()
            }
            path(fill = SolidColor(Color(0xFF666666)), stroke = null, strokeLineWidth = 0.0f,
                    strokeLineCap = Butt, strokeLineJoin = Miter, strokeLineMiter = 4.0f,
                    pathFillType = EvenOdd) {
                moveTo(14.0f, 22.0f)
                curveTo(18.4183f, 22.0f, 22.0f, 18.4183f, 22.0f, 14.0f)
                curveTo(22.0f, 9.5817f, 18.4183f, 6.0f, 14.0f, 6.0f)
                curveTo(9.5817f, 6.0f, 6.0f, 9.5817f, 6.0f, 14.0f)
                curveTo(6.0f, 18.4183f, 9.5817f, 22.0f, 14.0f, 22.0f)
                close()
                moveTo(14.0f, 20.0f)
                curveTo(17.3137f, 20.0f, 20.0f, 17.3137f, 20.0f, 14.0f)
                curveTo(20.0f, 10.6863f, 17.3137f, 8.0f, 14.0f, 8.0f)
                curveTo(10.6863f, 8.0f, 8.0f, 10.6863f, 8.0f, 14.0f)
                curveTo(8.0f, 17.3137f, 10.6863f, 20.0f, 14.0f, 20.0f)
                close()
            }
            path(fill = SolidColor(Color(0xFF666666)), stroke = null, strokeLineWidth = 0.0f,
                    strokeLineCap = Butt, strokeLineJoin = Miter, strokeLineMiter = 4.0f,
                    pathFillType = NonZero) {
                moveTo(14.0f, 24.0f)
                curveTo(13.4477f, 24.0f, 13.0f, 24.4477f, 13.0f, 25.0f)
                verticalLineTo(27.0f)
                curveTo(13.0f, 27.5523f, 13.4477f, 28.0f, 14.0f, 28.0f)
                curveTo(14.5523f, 28.0f, 15.0f, 27.5523f, 15.0f, 27.0f)
                verticalLineTo(25.0f)
                curveTo(15.0f, 24.4477f, 14.5523f, 24.0f, 14.0f, 24.0f)
                close()
            }
            path(fill = SolidColor(Color(0xFF666666)), stroke = null, strokeLineWidth = 0.0f,
                    strokeLineCap = Butt, strokeLineJoin = Miter, strokeLineMiter = 4.0f,
                    pathFillType = NonZero) {
                moveTo(22.4853f, 4.1005f)
                curveTo(22.8758f, 3.71f, 23.509f, 3.71f, 23.8995f, 4.1005f)
                curveTo(24.29f, 4.491f, 24.29f, 5.1242f, 23.8995f, 5.5147f)
                lineTo(22.4853f, 6.9289f)
                curveTo(22.0948f, 7.3195f, 21.4616f, 7.3195f, 21.0711f, 6.9289f)
                curveTo(20.6805f, 6.5384f, 20.6805f, 5.9053f, 21.0711f, 5.5147f)
                lineTo(22.4853f, 4.1005f)
                close()
            }
            path(fill = SolidColor(Color(0xFF666666)), stroke = null, strokeLineWidth = 0.0f,
                    strokeLineCap = Butt, strokeLineJoin = Miter, strokeLineMiter = 4.0f,
                    pathFillType = NonZero) {
                moveTo(6.9289f, 21.0711f)
                curveTo(6.5384f, 20.6805f, 5.9052f, 20.6805f, 5.5147f, 21.0711f)
                lineTo(4.1005f, 22.4853f)
                curveTo(3.71f, 22.8758f, 3.71f, 23.509f, 4.1005f, 23.8995f)
                curveTo(4.491f, 24.29f, 5.1242f, 24.29f, 5.5147f, 23.8995f)
                lineTo(6.9289f, 22.4853f)
                curveTo(7.3194f, 22.0948f, 7.3194f, 21.4616f, 6.9289f, 21.0711f)
                close()
            }
            path(fill = SolidColor(Color(0xFF666666)), stroke = null, strokeLineWidth = 0.0f,
                    strokeLineCap = Butt, strokeLineJoin = Miter, strokeLineMiter = 4.0f,
                    pathFillType = NonZero) {
                moveTo(1.0f, 15.0f)
                curveTo(0.4477f, 15.0f, 0.0f, 14.5523f, 0.0f, 14.0f)
                curveTo(0.0f, 13.4477f, 0.4477f, 13.0f, 1.0f, 13.0f)
                horizontalLineTo(3.0f)
                curveTo(3.5523f, 13.0f, 4.0f, 13.4477f, 4.0f, 14.0f)
                curveTo(4.0f, 14.5523f, 3.5523f, 15.0f, 3.0f, 15.0f)
                horizontalLineTo(1.0f)
                close()
            }
            path(fill = SolidColor(Color(0xFF666666)), stroke = null, strokeLineWidth = 0.0f,
                    strokeLineCap = Butt, strokeLineJoin = Miter, strokeLineMiter = 4.0f,
                    pathFillType = NonZero) {
                moveTo(24.0f, 14.0f)
                curveTo(24.0f, 14.5523f, 24.4477f, 15.0f, 25.0f, 15.0f)
                horizontalLineTo(27.0f)
                curveTo(27.5523f, 15.0f, 28.0f, 14.5523f, 28.0f, 14.0f)
                curveTo(28.0f, 13.4477f, 27.5523f, 13.0f, 27.0f, 13.0f)
                horizontalLineTo(25.0f)
                curveTo(24.4477f, 13.0f, 24.0f, 13.4477f, 24.0f, 14.0f)
                close()
            }
            path(fill = SolidColor(Color(0xFF666666)), stroke = null, strokeLineWidth = 0.0f,
                    strokeLineCap = Butt, strokeLineJoin = Miter, strokeLineMiter = 4.0f,
                    pathFillType = NonZero) {
                moveTo(4.1005f, 5.5147f)
                curveTo(3.71f, 5.1242f, 3.71f, 4.491f, 4.1005f, 4.1005f)
                curveTo(4.491f, 3.71f, 5.1242f, 3.71f, 5.5147f, 4.1005f)
                lineTo(6.9289f, 5.5147f)
                curveTo(7.3195f, 5.9053f, 7.3195f, 6.5384f, 6.9289f, 6.9289f)
                curveTo(6.5384f, 7.3195f, 5.9053f, 7.3195f, 5.5147f, 6.9289f)
                lineTo(4.1005f, 5.5147f)
                close()
            }
            path(fill = SolidColor(Color(0xFF666666)), stroke = null, strokeLineWidth = 0.0f,
                    strokeLineCap = Butt, strokeLineJoin = Miter, strokeLineMiter = 4.0f,
                    pathFillType = NonZero) {
                moveTo(21.0711f, 21.0711f)
                curveTo(20.6805f, 21.4616f, 20.6805f, 22.0948f, 21.0711f, 22.4853f)
                lineTo(22.4853f, 23.8995f)
                curveTo(22.8758f, 24.29f, 23.509f, 24.29f, 23.8995f, 23.8995f)
                curveTo(24.29f, 23.509f, 24.29f, 22.8758f, 23.8995f, 22.4853f)
                lineTo(22.4853f, 21.0711f)
                curveTo(22.0948f, 20.6805f, 21.4616f, 20.6805f, 21.0711f, 21.0711f)
                close()
            }
        }
        .build()
        return _lightMode!!
    }

private var _lightMode: ImageVector? = null
