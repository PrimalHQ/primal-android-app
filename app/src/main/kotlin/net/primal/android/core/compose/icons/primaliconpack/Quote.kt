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

public val PrimalIcons.Quote: ImageVector
    get() {
        if (_quote != null) {
            return _quote!!
        }
        _quote = Builder(name = "Quote", defaultWidth = 24.0.dp, defaultHeight = 24.0.dp,
                viewportWidth = 24.0f, viewportHeight = 24.0f).apply {
            path(fill = SolidColor(Color(0xFFffffff)), stroke = null, strokeLineWidth = 0.0f,
                    strokeLineCap = Butt, strokeLineJoin = Miter, strokeLineMiter = 4.0f,
                    pathFillType = NonZero) {
                moveTo(8.3357f, 3.0f)
                curveTo(7.8312f, 3.0f, 7.3434f, 3.1711f, 6.9685f, 3.5131f)
                curveTo(5.5395f, 4.8166f, 2.0005f, 8.4083f, 2.0005f, 12.0f)
                lineTo(2.0f, 16.5f)
                curveTo(2.0f, 18.9854f, 3.99f, 21.0f, 6.4444f, 21.0f)
                horizontalLineTo(8.6667f)
                curveTo(9.8939f, 21.0f, 10.8889f, 19.9926f, 10.8889f, 18.75f)
                verticalLineTo(14.25f)
                curveTo(10.8889f, 13.0074f, 9.8939f, 12.0f, 8.6667f, 12.0f)
                horizontalLineTo(6.445f)
                curveTo(6.4444f, 8.3586f, 8.6276f, 4.7169f, 9.4604f, 3.4601f)
                curveTo(9.5884f, 3.2675f, 9.4517f, 3.0f, 9.2228f, 3.0f)
                horizontalLineTo(8.3357f)
                close()
            }
            path(fill = SolidColor(Color(0xFFffffff)), stroke = null, strokeLineWidth = 0.0f,
                    strokeLineCap = Butt, strokeLineJoin = Miter, strokeLineMiter = 4.0f,
                    pathFillType = NonZero) {
                moveTo(18.0796f, 3.5131f)
                curveTo(18.2511f, 3.3565f, 18.4464f, 3.2357f, 18.6564f, 3.1508f)
                curveTo(18.8251f, 3.0824f, 19.003f, 3.0373f, 19.1853f, 3.0157f)
                curveTo(19.2716f, 3.0052f, 19.3589f, 3.0f, 19.4468f, 3.0f)
                horizontalLineTo(20.3339f)
                curveTo(20.463f, 3.0f, 20.5628f, 3.0849f, 20.6024f, 3.1936f)
                curveTo(20.6328f, 3.2777f, 20.6274f, 3.376f, 20.5715f, 3.4601f)
                curveTo(19.7387f, 4.7169f, 17.5556f, 8.3586f, 17.5561f, 12.0f)
                horizontalLineTo(19.7778f)
                curveTo(21.005f, 12.0f, 22.0f, 13.0074f, 22.0f, 14.25f)
                verticalLineTo(18.75f)
                curveTo(22.0f, 19.9926f, 21.005f, 21.0f, 19.7778f, 21.0f)
                horizontalLineTo(17.5556f)
                curveTo(15.1011f, 21.0f, 13.1111f, 18.9854f, 13.1111f, 16.5f)
                lineTo(13.1117f, 12.0f)
                curveTo(13.1117f, 8.4083f, 16.6506f, 4.8166f, 18.0796f, 3.5131f)
                close()
            }
        }
        .build()
        return _quote!!
    }

private var _quote: ImageVector? = null
