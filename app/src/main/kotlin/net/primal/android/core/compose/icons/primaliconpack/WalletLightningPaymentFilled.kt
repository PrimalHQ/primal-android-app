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

public val PrimalIcons.WalletLightningPaymentFilled: ImageVector
    get() {
        if (_navboltwalletfilled != null) {
            return _navboltwalletfilled!!
        }
        _navboltwalletfilled = Builder(name = "Navboltwalletfilled", defaultWidth = 20.0.dp,
                defaultHeight = 29.0.dp, viewportWidth = 20.0f, viewportHeight = 29.0f).apply {
            path(fill = SolidColor(Color(0xFFAAAAAA)), stroke = null, strokeLineWidth = 0.0f,
                    strokeLineCap = Butt, strokeLineJoin = Miter, strokeLineMiter = 4.0f,
                    pathFillType = EvenOdd) {
                moveTo(19.8873f, 11.1901f)
                curveTo(20.1706f, 10.8079f, 19.8872f, 10.277f, 19.3998f, 10.277f)
                horizontalLineTo(12.3748f)
                lineTo(13.7774f, 1.3819f)
                curveTo(14.0218f, 0.1813f, 12.4153f, -0.5131f, 11.6475f, 0.4612f)
                lineTo(0.1231f, 16.8179f)
                curveTo(-0.1765f, 17.1981f, 0.1049f, 17.7447f, 0.6002f, 17.7447f)
                horizontalLineTo(7.5821f)
                lineTo(6.1198f, 27.6375f)
                curveTo(5.8952f, 28.8497f, 7.5319f, 29.5138f, 8.2744f, 28.5118f)
                lineTo(19.8873f, 11.1901f)
                close()
            }
        }
        .build()
        return _navboltwalletfilled!!
    }

private var _navboltwalletfilled: ImageVector? = null
