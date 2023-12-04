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

public val PrimalIcons.WalletPrimalActivation: ImageVector
    get() {
        if (_walletPrimalActivation != null) {
            return _walletPrimalActivation!!
        }
        _walletPrimalActivation = Builder(name = "WalletPrimalActivation", defaultWidth = 100.0.dp,
                defaultHeight = 100.0.dp, viewportWidth = 100.0f, viewportHeight = 100.0f).apply {
            path(fill = SolidColor(Color(0xFFffffff)), stroke = null, strokeLineWidth = 0.0f,
                    strokeLineCap = Butt, strokeLineJoin = Miter, strokeLineMiter = 4.0f,
                    pathFillType = NonZero) {
                moveTo(80.007f, 17.686f)
                horizontalLineTo(3.683f)
                curveTo(4.778f, 16.793f, 6.074f, 16.124f, 7.509f, 15.754f)
                lineTo(67.515f, 0.32f)
                curveTo(73.843f, -1.307f, 80.007f, 3.472f, 80.007f, 10.006f)
                verticalLineTo(17.686f)
                close()
            }
            path(fill = SolidColor(Color(0xFFffffff)), stroke = null, strokeLineWidth = 0.0f,
                    strokeLineCap = Butt, strokeLineJoin = Miter, strokeLineMiter = 4.0f,
                    pathFillType = EvenOdd) {
                moveTo(0.0f, 24.081f)
                curveTo(0.0f, 23.39f, 0.56f, 22.83f, 1.25f, 22.83f)
                horizontalLineTo(90.008f)
                curveTo(95.531f, 22.83f, 100.009f, 27.308f, 100.009f, 32.831f)
                verticalLineTo(89.999f)
                curveTo(100.009f, 95.522f, 95.531f, 100.0f, 90.008f, 100.0f)
                horizontalLineTo(10.001f)
                curveTo(4.478f, 100.0f, 0.0f, 95.522f, 0.0f, 89.999f)
                verticalLineTo(24.081f)
                close()
                moveTo(80.007f, 61.415f)
                curveTo(80.007f, 65.677f, 76.649f, 69.132f, 72.506f, 69.132f)
                curveTo(68.364f, 69.132f, 65.006f, 65.677f, 65.006f, 61.415f)
                curveTo(65.006f, 57.153f, 68.364f, 53.698f, 72.506f, 53.698f)
                curveTo(76.649f, 53.698f, 80.007f, 57.153f, 80.007f, 61.415f)
                close()
            }
        }
        .build()
        return _walletPrimalActivation!!
    }

private var _walletPrimalActivation: ImageVector? = null
