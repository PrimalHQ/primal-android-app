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

public val PrimalIcons.NewDM: ImageVector
    get() {
        if (_newdm != null) {
            return _newdm!!
        }
        _newdm = Builder(name = "Newdm", defaultWidth = 28.0.dp, defaultHeight = 24.0.dp,
                viewportWidth = 28.0f, viewportHeight = 24.0f).apply {
            path(fill = SolidColor(Color(0xFFffffff)), stroke = null, strokeLineWidth = 0.0f,
                    strokeLineCap = Butt, strokeLineJoin = Miter, strokeLineMiter = 4.0f,
                    pathFillType = EvenOdd) {
                moveTo(2.0f, 0.0f)
                curveTo(0.8954f, 0.0f, 0.0f, 0.8954f, 0.0f, 2.0f)
                verticalLineTo(18.0f)
                curveTo(0.0f, 19.1046f, 0.8954f, 20.0f, 2.0f, 20.0f)
                horizontalLineTo(16.1678f)
                curveTo(16.0572f, 19.6872f, 15.9971f, 19.3506f, 15.9971f, 19.0f)
                curveTo(15.9971f, 18.6494f, 16.0572f, 18.3128f, 16.1678f, 18.0f)
                horizontalLineTo(3.0f)
                curveTo(2.4477f, 18.0f, 2.0f, 17.5523f, 2.0f, 17.0f)
                verticalLineTo(5.6654f)
                curveTo(2.0f, 5.2435f, 2.4901f, 5.0113f, 2.8166f, 5.2784f)
                lineTo(10.7335f, 11.7559f)
                curveTo(11.4703f, 12.3586f, 12.5297f, 12.3586f, 13.2665f, 11.7559f)
                lineTo(21.1834f, 5.2784f)
                curveTo(21.5098f, 5.0113f, 22.0f, 5.2435f, 22.0f, 5.6654f)
                verticalLineTo(12.1697f)
                curveTo(22.312f, 12.0598f, 22.6475f, 12.0f, 22.9971f, 12.0f)
                curveTo(23.3488f, 12.0f, 23.6864f, 12.0605f, 24.0f, 12.1717f)
                verticalLineTo(2.0f)
                curveTo(24.0f, 0.8954f, 23.1046f, 0.0f, 22.0f, 0.0f)
                horizontalLineTo(2.0f)
                close()
                moveTo(2.6986f, 2.0f)
                curveTo(2.4639f, 2.0f, 2.3586f, 2.2943f, 2.54f, 2.4432f)
                lineTo(10.7311f, 9.1664f)
                curveTo(11.4687f, 9.7719f, 12.5313f, 9.7719f, 13.2689f, 9.1664f)
                lineTo(21.46f, 2.4432f)
                curveTo(21.6414f, 2.2943f, 21.5361f, 2.0f, 21.3014f, 2.0f)
                horizontalLineTo(2.6986f)
                close()
            }
            path(fill = SolidColor(Color(0xFFffffff)), stroke = null, strokeLineWidth = 0.0f,
                    strokeLineCap = Butt, strokeLineJoin = Miter, strokeLineMiter = 4.0f,
                    pathFillType = NonZero) {
                moveTo(17.9971f, 19.0f)
                curveTo(17.9971f, 18.4477f, 18.4448f, 18.0f, 18.9971f, 18.0f)
                horizontalLineTo(21.9971f)
                verticalLineTo(15.0f)
                curveTo(21.9971f, 14.4477f, 22.4448f, 14.0f, 22.9971f, 14.0f)
                curveTo(23.5494f, 14.0f, 23.9971f, 14.4477f, 23.9971f, 15.0f)
                verticalLineTo(18.0f)
                horizontalLineTo(26.9971f)
                curveTo(27.5494f, 18.0f, 27.9971f, 18.4477f, 27.9971f, 19.0f)
                curveTo(27.9971f, 19.5523f, 27.5494f, 20.0f, 26.9971f, 20.0f)
                horizontalLineTo(23.9971f)
                verticalLineTo(23.0f)
                curveTo(23.9971f, 23.5523f, 23.5494f, 24.0f, 22.9971f, 24.0f)
                curveTo(22.4448f, 24.0f, 21.9971f, 23.5523f, 21.9971f, 23.0f)
                verticalLineTo(20.0f)
                horizontalLineTo(18.9971f)
                curveTo(18.4448f, 20.0f, 17.9971f, 19.5523f, 17.9971f, 19.0f)
                close()
            }
        }
        .build()
        return _newdm!!
    }

private var _newdm: ImageVector? = null
