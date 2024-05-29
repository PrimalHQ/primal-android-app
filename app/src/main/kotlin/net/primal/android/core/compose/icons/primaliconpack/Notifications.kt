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

public val PrimalIcons.Notifications: ImageVector
    get() {
        if (_notifications != null) {
            return _notifications!!
        }
        _notifications = Builder(name = "Notifications", defaultWidth = 24.0.dp, defaultHeight =
                24.0.dp, viewportWidth = 24.0f, viewportHeight = 24.0f).apply {
            path(fill = SolidColor(Color(0xFFAAAAAA)), stroke = null, strokeLineWidth = 0.0f,
                    strokeLineCap = Butt, strokeLineJoin = Miter, strokeLineMiter = 4.0f,
                    pathFillType = EvenOdd) {
                moveTo(2.2529f, 10.0f)
                curveTo(2.2529f, 4.6152f, 6.6182f, 0.25f, 12.003f, 0.25f)
                curveTo(17.3877f, 0.25f, 21.753f, 4.6152f, 21.753f, 10.0f)
                verticalLineTo(12.8445f)
                curveTo(21.753f, 13.6396f, 21.9047f, 14.4274f, 22.2f, 15.1657f)
                lineTo(23.2223f, 17.7215f)
                curveTo(23.4193f, 18.2141f, 23.0565f, 18.75f, 22.5259f, 18.75f)
                horizontalLineTo(16.753f)
                verticalLineTo(19.0f)
                curveTo(16.753f, 21.6234f, 14.6263f, 23.75f, 12.003f, 23.75f)
                curveTo(9.3796f, 23.75f, 7.253f, 21.6234f, 7.253f, 19.0f)
                verticalLineTo(18.75f)
                horizontalLineTo(1.48f)
                curveTo(0.9494f, 18.75f, 0.5866f, 18.2141f, 0.7836f, 17.7215f)
                lineTo(1.8059f, 15.1657f)
                curveTo(2.1012f, 14.4274f, 2.2529f, 13.6396f, 2.2529f, 12.8445f)
                verticalLineTo(10.0f)
                close()
                moveTo(12.003f, 1.75f)
                curveTo(7.4466f, 1.75f, 3.7529f, 5.4436f, 3.7529f, 10.0f)
                verticalLineTo(12.8445f)
                curveTo(3.7529f, 13.8305f, 3.5648f, 14.8074f, 3.1986f, 15.7228f)
                lineTo(2.5878f, 17.25f)
                horizontalLineTo(21.4181f)
                lineTo(20.8073f, 15.7228f)
                curveTo(20.4411f, 14.8074f, 20.253f, 13.8305f, 20.253f, 12.8445f)
                verticalLineTo(10.0f)
                curveTo(20.253f, 5.4436f, 16.5593f, 1.75f, 12.003f, 1.75f)
                close()
                moveTo(15.253f, 19.0f)
                verticalLineTo(18.75f)
                horizontalLineTo(8.753f)
                verticalLineTo(19.0f)
                curveTo(8.753f, 20.7949f, 10.208f, 22.25f, 12.003f, 22.25f)
                curveTo(13.7979f, 22.25f, 15.253f, 20.7949f, 15.253f, 19.0f)
                close()
            }
        }
        .build()
        return _notifications!!
    }

private var _notifications: ImageVector? = null
