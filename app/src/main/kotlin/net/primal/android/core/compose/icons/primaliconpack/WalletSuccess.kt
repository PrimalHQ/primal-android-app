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

public val PrimalIcons.WalletSuccess: ImageVector
    get() {
        if (_walletSuccess != null) {
            return _walletSuccess!!
        }
        _walletSuccess = Builder(name = "WalletSuccess", defaultWidth = 160.0.dp, defaultHeight =
                160.0.dp, viewportWidth = 160.0f, viewportHeight = 160.0f).apply {
            path(fill = SolidColor(Color(0x00000000)), stroke = SolidColor(Color(0xFFffffff)),
                    strokeLineWidth = 10.0f, strokeLineCap = Butt, strokeLineJoin = Miter,
                    strokeLineMiter = 4.0f, pathFillType = NonZero) {
                moveTo(80.0f, 80.0f)
                moveToRelative(-75.0f, 0.0f)
                arcToRelative(75.0f, 75.0f, 0.0f, true, true, 150.0f, 0.0f)
                arcToRelative(75.0f, 75.0f, 0.0f, true, true, -150.0f, 0.0f)
            }
            path(fill = SolidColor(Color(0xFFffffff)), stroke = null, strokeLineWidth = 0.0f,
                    strokeLineCap = Butt, strokeLineJoin = Miter, strokeLineMiter = 4.0f,
                    pathFillType = NonZero) {
                moveTo(66.943f, 109.242f)
                lineTo(46.763f, 88.102f)
                curveTo(44.655f, 85.894f, 44.648f, 82.421f, 46.748f, 80.205f)
                curveTo(48.999f, 77.829f, 52.78f, 77.818f, 55.044f, 80.182f)
                lineTo(66.944f, 92.603f)
                curveTo(67.338f, 93.014f, 67.995f, 93.013f, 68.389f, 92.602f)
                lineTo(104.958f, 54.349f)
                curveTo(107.22f, 51.983f, 111.003f, 51.991f, 113.254f, 54.368f)
                curveTo(115.353f, 56.583f, 115.346f, 60.054f, 113.239f, 62.261f)
                lineTo(68.39f, 109.242f)
                curveTo(67.996f, 109.655f, 67.337f, 109.655f, 66.943f, 109.242f)
                close()
            }
        }
        .build()
        return _walletSuccess!!
    }

private var _walletSuccess: ImageVector? = null
