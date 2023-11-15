@file:Suppress("MagicNumber")

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

public val PrimalIcons.NavWalletFilled: ImageVector
    get() {
        if (_navwalletfilled != null) {
            return _navwalletfilled!!
        }
        _navwalletfilled = Builder(name = "Navwalletfilled", defaultWidth = 25.0.dp, defaultHeight =
                24.0.dp, viewportWidth = 25.0f, viewportHeight = 24.0f).apply {
            path(fill = SolidColor(Color(0xFFffffff)), stroke = null, strokeLineWidth = 0.0f,
                    strokeLineCap = Butt, strokeLineJoin = Miter, strokeLineMiter = 4.0f,
                    pathFillType = NonZero) {
                moveTo(19.2016f, 4.2446f)
                horizontalLineTo(0.884f)
                curveTo(1.1468f, 4.0304f, 1.4576f, 3.8697f, 1.8022f, 3.781f)
                lineTo(16.2035f, 0.0769f)
                curveTo(17.7223f, -0.3137f, 19.2016f, 0.8332f, 19.2016f, 2.4014f)
                verticalLineTo(4.2446f)
                close()
            }
            path(fill = SolidColor(Color(0xFFffffff)), stroke = null, strokeLineWidth = 0.0f,
                    strokeLineCap = Butt, strokeLineJoin = Miter, strokeLineMiter = 4.0f,
                    pathFillType = EvenOdd) {
                moveTo(0.0f, 5.7794f)
                curveTo(0.0f, 5.6137f, 0.1343f, 5.4793f, 0.3f, 5.4793f)
                horizontalLineTo(21.6019f)
                curveTo(22.9275f, 5.4793f, 24.0021f, 6.5539f, 24.0021f, 7.8795f)
                verticalLineTo(21.5998f)
                curveTo(24.0021f, 22.9254f, 22.9275f, 24.0f, 21.6019f, 24.0f)
                horizontalLineTo(2.4002f)
                curveTo(1.0746f, 24.0f, 0.0f, 22.9254f, 0.0f, 21.5998f)
                verticalLineTo(5.7794f)
                close()
                moveTo(19.2017f, 14.7396f)
                curveTo(19.2017f, 15.7625f, 18.3957f, 16.5917f, 17.4015f, 16.5917f)
                curveTo(16.4073f, 16.5917f, 15.6014f, 15.7625f, 15.6014f, 14.7396f)
                curveTo(15.6014f, 13.7168f, 16.4073f, 12.8876f, 17.4015f, 12.8876f)
                curveTo(18.3957f, 12.8876f, 19.2017f, 13.7168f, 19.2017f, 14.7396f)
                close()
            }
        }
        .build()
        return _navwalletfilled!!
    }

private var _navwalletfilled: ImageVector? = null
