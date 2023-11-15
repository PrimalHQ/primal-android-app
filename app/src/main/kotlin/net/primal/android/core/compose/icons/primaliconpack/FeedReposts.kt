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

public val PrimalIcons.FeedReposts: ImageVector
    get() {
        if (_feedReposts != null) {
            return _feedReposts!!
        }
        _feedReposts = Builder(name = "Feedreposts", defaultWidth = 18.0.dp, defaultHeight =
                18.0.dp, viewportWidth = 18.0f, viewportHeight = 18.0f).apply {
            path(fill = SolidColor(Color(0xFF666666)), stroke = null, strokeLineWidth = 0.0f,
                    strokeLineCap = Butt, strokeLineJoin = Miter, strokeLineMiter = 4.0f,
                    pathFillType = NonZero) {
                moveTo(6.2595f, 5.812f)
                curveTo(6.5378f, 6.0355f, 6.95f, 5.8366f, 6.95f, 5.4788f)
                verticalLineTo(3.8065f)
                horizontalLineTo(13.75f)
                curveTo(15.1583f, 3.8065f, 16.3f, 4.9527f, 16.3f, 6.3666f)
                verticalLineTo(9.5709f)
                curveTo(16.3f, 9.8614f, 16.2518f, 10.1405f, 16.163f, 10.4008f)
                curveTo(16.098f, 10.5914f, 16.1418f, 10.8092f, 16.2986f, 10.9352f)
                lineTo(16.999f, 11.4977f)
                curveTo(17.1986f, 11.658f, 17.4947f, 11.6067f, 17.6029f, 11.3742f)
                curveTo(17.8577f, 10.8264f, 18.0f, 10.2154f, 18.0f, 9.5709f)
                verticalLineTo(6.3666f)
                curveTo(18.0f, 4.0101f, 16.0972f, 2.0998f, 13.75f, 2.0998f)
                horizontalLineTo(6.95f)
                verticalLineTo(0.4275f)
                curveTo(6.95f, 0.0697f, 6.5378f, -0.1292f, 6.2595f, 0.0943f)
                lineTo(3.1148f, 2.62f)
                curveTo(2.9022f, 2.7908f, 2.9022f, 3.1155f, 3.1148f, 3.2863f)
                lineTo(6.2595f, 5.812f)
                close()
            }
            path(fill = SolidColor(Color(0xFF666666)), stroke = null, strokeLineWidth = 0.0f,
                    strokeLineCap = Butt, strokeLineJoin = Miter, strokeLineMiter = 4.0f,
                    pathFillType = NonZero) {
                moveTo(2.001f, 5.5023f)
                curveTo(1.8014f, 5.342f, 1.5053f, 5.3933f, 1.3971f, 5.6258f)
                curveTo(1.1423f, 6.1736f, 1.0f, 6.7846f, 1.0f, 7.4291f)
                verticalLineTo(10.6334f)
                curveTo(1.0f, 12.9899f, 2.9028f, 14.9002f, 5.25f, 14.9002f)
                horizontalLineTo(12.05f)
                verticalLineTo(16.5725f)
                curveTo(12.05f, 16.9303f, 12.4622f, 17.1292f, 12.7405f, 16.9057f)
                lineTo(15.8852f, 14.38f)
                curveTo(16.0978f, 14.2092f, 16.0978f, 13.8845f, 15.8852f, 13.7137f)
                lineTo(12.7405f, 11.188f)
                curveTo(12.4622f, 10.9645f, 12.05f, 11.1634f, 12.05f, 11.5212f)
                verticalLineTo(13.1935f)
                horizontalLineTo(5.25f)
                curveTo(3.8417f, 13.1935f, 2.7f, 12.0473f, 2.7f, 10.6334f)
                verticalLineTo(7.4291f)
                curveTo(2.7f, 7.1386f, 2.7482f, 6.8595f, 2.837f, 6.5992f)
                curveTo(2.902f, 6.4086f, 2.8582f, 6.1908f, 2.7014f, 6.0648f)
                lineTo(2.001f, 5.5023f)
                close()
            }
        }
        .build()
        return _feedReposts!!
    }

private var _feedReposts: ImageVector? = null
