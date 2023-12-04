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

public val PrimalIcons.WalletReceive: ImageVector
    get() {
        if (_walletReceive != null) {
            return _walletReceive!!
        }
        _walletReceive = Builder(name = "WalletReceive", defaultWidth = 24.0.dp, defaultHeight =
                24.0.dp, viewportWidth = 24.0f, viewportHeight = 24.0f).apply {
            path(fill = SolidColor(Color(0xFFAAAAAA)), stroke = null, strokeLineWidth = 0.0f,
                    strokeLineCap = Butt, strokeLineJoin = Miter, strokeLineMiter = 4.0f,
                    pathFillType = NonZero) {
                moveTo(2.3855f, 12.7672f)
                curveTo(1.8711f, 13.2795f, 1.8716f, 14.1276f, 2.3866f, 14.6393f)
                lineTo(11.6726f, 23.864f)
                curveTo(11.8552f, 24.0453f, 12.1448f, 24.0453f, 12.3274f, 23.864f)
                lineTo(21.6134f, 14.6393f)
                curveTo(22.1284f, 14.1276f, 22.1289f, 13.2795f, 21.6145f, 12.7672f)
                curveTo(21.1232f, 12.278f, 20.3428f, 12.2774f, 19.8508f, 12.7657f)
                lineTo(13.2921f, 19.2743f)
                verticalLineTo(1.3275f)
                curveTo(13.2921f, 0.5943f, 12.7136f, 0.0f, 12.0f, 0.0f)
                curveTo(11.2864f, 0.0f, 10.7079f, 0.5943f, 10.7079f, 1.3275f)
                verticalLineTo(19.2743f)
                lineTo(4.1492f, 12.7657f)
                curveTo(3.6572f, 12.2774f, 2.8767f, 12.278f, 2.3855f, 12.7672f)
                close()
            }
        }
        .build()
        return _walletReceive!!
    }

private var _walletReceive: ImageVector? = null
