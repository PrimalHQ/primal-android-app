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

public val PrimalIcons.NavWallet: ImageVector
    get() {
        if (_navwallet != null) {
            return _navwallet!!
        }
        _navwallet = Builder(name = "Navwallet", defaultWidth = 24.0.dp, defaultHeight = 24.0.dp,
                viewportWidth = 24.0f, viewportHeight = 24.0f).apply {
            path(fill = SolidColor(Color(0xFFAAAAAA)), stroke = null, strokeLineWidth = 0.0f,
                    strokeLineCap = Butt, strokeLineJoin = Miter, strokeLineMiter = 4.0f,
                    pathFillType = NonZero) {
                moveTo(19.2f, 14.7405f)
                curveTo(19.2f, 15.7632f, 18.3941f, 16.5924f, 17.4f, 16.5924f)
                curveTo(16.4059f, 16.5924f, 15.6f, 15.7632f, 15.6f, 14.7405f)
                curveTo(15.6f, 13.7177f, 16.4059f, 12.8886f, 17.4f, 12.8886f)
                curveTo(18.3941f, 12.8886f, 19.2f, 13.7177f, 19.2f, 14.7405f)
                close()
            }
            path(fill = SolidColor(Color(0xFFAAAAAA)), stroke = null, strokeLineWidth = 0.0f,
                    strokeLineCap = Butt, strokeLineJoin = Miter, strokeLineMiter = 4.0f,
                    pathFillType = EvenOdd) {
                moveTo(19.2f, 5.4809f)
                verticalLineTo(2.4704f)
                curveTo(19.2f, 0.864f, 17.7327f, -0.3147f, 16.2179f, 0.0749f)
                lineTo(1.8179f, 3.7787f)
                curveTo(0.7495f, 4.0536f, 0.0f, 5.0412f, 0.0f, 6.1742f)
                verticalLineTo(21.5308f)
                curveTo(0.0f, 22.8945f, 1.0745f, 24.0f, 2.4f, 24.0f)
                horizontalLineTo(21.6f)
                curveTo(22.9255f, 24.0f, 24.0f, 22.8945f, 24.0f, 21.5308f)
                verticalLineTo(7.9501f)
                curveTo(24.0f, 6.5864f, 22.9255f, 5.4809f, 21.6f, 5.4809f)
                horizontalLineTo(19.2f)
                close()
                moveTo(3.8586f, 5.4809f)
                horizontalLineTo(17.1f)
                verticalLineTo(2.4704f)
                curveTo(17.1f, 2.2696f, 16.9166f, 2.1223f, 16.7272f, 2.171f)
                lineTo(3.8586f, 5.4809f)
                close()
                moveTo(2.7f, 7.6415f)
                curveTo(2.3686f, 7.6415f, 2.1f, 7.9179f, 2.1f, 8.2588f)
                verticalLineTo(21.2221f)
                curveTo(2.1f, 21.5631f, 2.3686f, 21.8394f, 2.7f, 21.8394f)
                horizontalLineTo(21.3f)
                curveTo(21.6314f, 21.8394f, 21.9f, 21.5631f, 21.9f, 21.2221f)
                verticalLineTo(8.2588f)
                curveTo(21.9f, 7.9179f, 21.6314f, 7.6415f, 21.3f, 7.6415f)
                horizontalLineTo(2.7f)
                close()
            }
        }
        .build()
        return _navwallet!!
    }

private var _navwallet: ImageVector? = null
