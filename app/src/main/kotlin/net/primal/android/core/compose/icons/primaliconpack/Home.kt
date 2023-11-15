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

public val PrimalIcons.Home: ImageVector
    get() {
        if (_home != null) {
            return _home!!
        }
        _home = Builder(name = "Home", defaultWidth = 24.0.dp, defaultHeight = 24.0.dp,
                viewportWidth = 24.0f, viewportHeight = 24.0f).apply {
            path(fill = SolidColor(Color(0xFFAAAAAA)), stroke = null, strokeLineWidth = 0.0f,
                    strokeLineCap = Butt, strokeLineJoin = Miter, strokeLineMiter = 4.0f,
                    pathFillType = NonZero) {
                moveTo(12.8823f, 0.4053f)
                curveTo(12.3564f, 0.0228f, 11.6438f, 0.0228f, 11.1178f, 0.4053f)
                lineTo(0.4119f, 8.1914f)
                curveTo(-0.0348f, 8.5163f, -0.1335f, 9.1417f, 0.1913f, 9.5884f)
                curveTo(0.5162f, 10.035f, 1.1416f, 10.1337f, 1.5882f, 9.8089f)
                lineTo(11.1178f, 2.8783f)
                curveTo(11.6438f, 2.4958f, 12.3564f, 2.4958f, 12.8823f, 2.8783f)
                lineTo(22.4119f, 9.8089f)
                curveTo(22.8586f, 10.1337f, 23.484f, 10.035f, 23.8088f, 9.5884f)
                curveTo(24.1336f, 9.1417f, 24.0349f, 8.5163f, 23.5882f, 8.1914f)
                lineTo(12.8823f, 0.4053f)
                close()
            }
            path(fill = SolidColor(Color(0xFFAAAAAA)), stroke = null, strokeLineWidth = 0.0f,
                    strokeLineCap = Butt, strokeLineJoin = Miter, strokeLineMiter = 4.0f,
                    pathFillType = NonZero) {
                moveTo(4.4f, 10.7f)
                curveTo(4.1482f, 10.8889f, 4.0f, 11.1852f, 4.0f, 11.5f)
                verticalLineTo(21.0f)
                curveTo(4.0f, 22.1046f, 4.8954f, 23.0f, 6.0f, 23.0f)
                horizontalLineTo(18.0f)
                curveTo(19.1046f, 23.0f, 20.0f, 22.1046f, 20.0f, 21.0f)
                verticalLineTo(11.5f)
                curveTo(20.0f, 11.1852f, 19.8518f, 10.8889f, 19.6f, 10.7f)
                lineTo(18.4f, 9.8f)
                curveTo(18.2352f, 9.6764f, 18.0f, 9.794f, 18.0f, 10.0f)
                verticalLineTo(20.0f)
                curveTo(18.0f, 20.5523f, 17.5523f, 21.0f, 17.0f, 21.0f)
                horizontalLineTo(7.0f)
                curveTo(6.4477f, 21.0f, 6.0f, 20.5523f, 6.0f, 20.0f)
                verticalLineTo(10.0f)
                curveTo(6.0f, 9.794f, 5.7648f, 9.6764f, 5.6f, 9.8f)
                lineTo(4.4f, 10.7f)
                close()
            }
        }
        .build()
        return _home!!
    }

private var _home: ImageVector? = null
