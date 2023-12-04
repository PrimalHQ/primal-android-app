package net.primal.android.core.compose.icons.primaliconpack

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType.Companion.NonZero
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap.Companion.Butt
import androidx.compose.ui.graphics.StrokeJoin.Companion.Miter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.ImageVector.Builder
import androidx.compose.ui.graphics.vector.group
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp
import net.primal.android.core.compose.icons.PrimalIcons

public val PrimalIcons.WalletPay: ImageVector
    get() {
        if (_walletPay != null) {
            return _walletPay!!
        }
        _walletPay = Builder(name = "WalletPay", defaultWidth = 24.0.dp, defaultHeight = 24.0.dp,
                viewportWidth = 24.0f, viewportHeight = 24.0f).apply {
            group {
                path(fill = SolidColor(Color(0xFFAAAAAA)), stroke = null, strokeLineWidth = 0.0f,
                        strokeLineCap = Butt, strokeLineJoin = Miter, strokeLineMiter = 4.0f,
                        pathFillType = NonZero) {
                    moveTo(21.6145f, 11.2328f)
                    curveTo(22.1289f, 10.7205f, 22.1284f, 9.8724f, 21.6134f, 9.3607f)
                    lineTo(12.3274f, 0.136f)
                    curveTo(12.1448f, -0.0453f, 11.8552f, -0.0453f, 11.6726f, 0.136f)
                    lineTo(2.3866f, 9.3607f)
                    curveTo(1.8716f, 9.8724f, 1.8711f, 10.7205f, 2.3855f, 11.2328f)
                    curveTo(2.8767f, 11.722f, 3.6572f, 11.7226f, 4.1492f, 11.2343f)
                    lineTo(10.7079f, 4.7256f)
                    verticalLineTo(22.6725f)
                    curveTo(10.7079f, 23.4057f, 11.2864f, 24.0f, 12.0f, 24.0f)
                    curveTo(12.7136f, 24.0f, 13.2921f, 23.4057f, 13.2921f, 22.6725f)
                    verticalLineTo(4.7256f)
                    lineTo(19.8508f, 11.2343f)
                    curveTo(20.3428f, 11.7226f, 21.1232f, 11.722f, 21.6145f, 11.2328f)
                    close()
                }
            }
        }
        .build()
        return _walletPay!!
    }

private var _walletPay: ImageVector? = null
