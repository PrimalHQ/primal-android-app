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

public val PrimalIcons.NavMessagesFilled: ImageVector
    get() {
        if (_navmessagesfilled != null) {
            return _navmessagesfilled!!
        }
        _navmessagesfilled = Builder(name = "Navmessagesfilled", defaultWidth = 24.0.dp,
                defaultHeight = 24.0.dp, viewportWidth = 24.0f, viewportHeight = 24.0f).apply {
            path(fill = SolidColor(Color(0xFFffffff)), stroke = null, strokeLineWidth = 0.0f,
                    strokeLineCap = Butt, strokeLineJoin = Miter, strokeLineMiter = 4.0f,
                    pathFillType = NonZero) {
                moveTo(0.0f, 6.8907f)
                curveTo(0.0f, 6.6384f, 0.2926f, 6.4988f, 0.4888f, 6.6575f)
                lineTo(10.2403f, 14.5517f)
                curveTo(11.2648f, 15.381f, 12.73f, 15.3802f, 13.7536f, 14.5498f)
                lineTo(23.511f, 6.6334f)
                curveTo(23.7071f, 6.4743f, 24.0f, 6.6139f, 24.0f, 6.8664f)
                verticalLineTo(20.0f)
                curveTo(24.0f, 21.1046f, 23.1046f, 22.0f, 22.0f, 22.0f)
                horizontalLineTo(2.0f)
                curveTo(0.8954f, 22.0f, 0.0f, 21.1046f, 0.0f, 20.0f)
                verticalLineTo(6.8907f)
                close()
            }
            path(fill = SolidColor(Color(0xFFffffff)), stroke = null, strokeLineWidth = 0.0f,
                    strokeLineCap = Butt, strokeLineJoin = Miter, strokeLineMiter = 4.0f,
                    pathFillType = NonZero) {
                moveTo(0.1494f, 3.8353f)
                curveTo(0.0679f, 3.7694f, 0.0251f, 3.6653f, 0.0479f, 3.563f)
                curveTo(0.2472f, 2.6686f, 1.0455f, 2.0f, 2.0f, 2.0f)
                horizontalLineTo(22.0f)
                curveTo(22.9451f, 2.0f, 23.737f, 2.6555f, 23.946f, 3.5366f)
                curveTo(23.9705f, 3.6397f, 23.9278f, 3.7455f, 23.8455f, 3.8123f)
                lineTo(12.5061f, 13.0122f)
                curveTo(12.209f, 13.2533f, 11.7836f, 13.2535f, 11.4861f, 13.0127f)
                lineTo(0.1494f, 3.8353f)
                close()
            }
        }
        .build()
        return _navmessagesfilled!!
    }

private var _navmessagesfilled: ImageVector? = null
