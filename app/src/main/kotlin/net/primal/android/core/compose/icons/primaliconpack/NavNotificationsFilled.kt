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

public val PrimalIcons.NavNotificationsFilled: ImageVector
    get() {
        if (_navnotificationsfilled != null) {
            return _navnotificationsfilled!!
        }
        _navnotificationsfilled = Builder(name = "Navnotificationsfilled", defaultWidth = 24.0.dp,
                defaultHeight = 24.0.dp, viewportWidth = 24.0f, viewportHeight = 24.0f).apply {
            path(fill = SolidColor(Color(0xFFffffff)), stroke = null, strokeLineWidth = 0.0f,
                    strokeLineCap = Butt, strokeLineJoin = Miter, strokeLineMiter = 4.0f,
                    pathFillType = NonZero) {
                moveTo(1.3893f, 18.9959f)
                curveTo(0.7326f, 18.9375f, 0.3013f, 18.2567f, 0.5525f, 17.6286f)
                lineTo(1.5748f, 15.0729f)
                curveTo(1.8583f, 14.3641f, 2.004f, 13.6078f, 2.004f, 12.8445f)
                verticalLineTo(10.0f)
                curveTo(2.004f, 4.4771f, 6.4811f, 0.0f, 12.004f, 0.0f)
                curveTo(17.5268f, 0.0f, 22.004f, 4.4771f, 22.004f, 10.0f)
                verticalLineTo(12.8445f)
                curveTo(22.004f, 13.6078f, 22.1496f, 14.3641f, 22.4331f, 15.0728f)
                lineTo(23.4554f, 17.6286f)
                curveTo(23.7066f, 18.2567f, 23.2754f, 18.9375f, 22.6186f, 18.9959f)
                horizontalLineTo(1.3893f)
                close()
            }
            path(fill = SolidColor(Color(0xFFffffff)), stroke = null, strokeLineWidth = 0.0f,
                    strokeLineCap = Butt, strokeLineJoin = Miter, strokeLineMiter = 4.0f,
                    pathFillType = NonZero) {
                moveTo(16.7763f, 20.4959f)
                curveTo(16.1405f, 22.5266f, 14.2443f, 24.0f, 12.004f, 24.0f)
                curveTo(9.7636f, 24.0f, 7.8675f, 22.5266f, 7.2316f, 20.4959f)
                horizontalLineTo(16.7763f)
                close()
            }
        }
        .build()
        return _navnotificationsfilled!!
    }

private var _navnotificationsfilled: ImageVector? = null
