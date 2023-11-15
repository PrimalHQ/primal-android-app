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

public val PrimalIcons.NavNotifications: ImageVector
    get() {
        if (_navnotifications != null) {
            return _navnotifications!!
        }
        _navnotifications = Builder(name = "Navnotifications", defaultWidth = 24.0.dp, defaultHeight
                = 24.0.dp, viewportWidth = 24.0f, viewportHeight = 24.0f).apply {
            path(fill = SolidColor(Color(0xFFAAAAAA)), stroke = null, strokeLineWidth = 0.0f,
                    strokeLineCap = Butt, strokeLineJoin = Miter, strokeLineMiter = 4.0f,
                    pathFillType = EvenOdd) {
                moveTo(7.003f, 19.0f)
                horizontalLineTo(1.48f)
                curveTo(0.7725f, 19.0f, 0.2888f, 18.2855f, 0.5515f, 17.6286f)
                lineTo(1.5738f, 15.0729f)
                curveTo(1.8573f, 14.3641f, 2.003f, 13.6078f, 2.003f, 12.8445f)
                verticalLineTo(10.0f)
                curveTo(2.003f, 4.4771f, 6.4801f, 0.0f, 12.003f, 0.0f)
                curveTo(17.5258f, 0.0f, 22.003f, 4.4771f, 22.003f, 10.0f)
                verticalLineTo(12.8445f)
                curveTo(22.003f, 13.6078f, 22.1486f, 14.3641f, 22.4321f, 15.0728f)
                lineTo(23.4544f, 17.6286f)
                curveTo(23.7171f, 18.2855f, 23.2334f, 19.0f, 22.5259f, 19.0f)
                horizontalLineTo(17.003f)
                curveTo(17.003f, 21.7614f, 14.7644f, 24.0f, 12.003f, 24.0f)
                curveTo(9.2415f, 24.0f, 7.003f, 21.7614f, 7.003f, 19.0f)
                close()
                moveTo(20.5751f, 15.8156f)
                lineTo(21.0489f, 17.0f)
                horizontalLineTo(2.957f)
                lineTo(3.4308f, 15.8156f)
                curveTo(3.8088f, 14.8707f, 4.003f, 13.8623f, 4.003f, 12.8445f)
                verticalLineTo(10.0f)
                curveTo(4.003f, 5.5817f, 7.5847f, 2.0f, 12.003f, 2.0f)
                curveTo(16.4212f, 2.0f, 20.003f, 5.5817f, 20.003f, 10.0f)
                verticalLineTo(12.8445f)
                curveTo(20.003f, 13.8623f, 20.1972f, 14.8707f, 20.5751f, 15.8156f)
                close()
                moveTo(15.003f, 19.0f)
                horizontalLineTo(9.003f)
                curveTo(9.003f, 20.6569f, 10.3461f, 22.0f, 12.003f, 22.0f)
                curveTo(13.6598f, 22.0f, 15.003f, 20.6569f, 15.003f, 19.0f)
                close()
            }
        }
        .build()
        return _navnotifications!!
    }

private var _navnotifications: ImageVector? = null
