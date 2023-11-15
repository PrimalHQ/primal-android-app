@file:Suppress("MagicNumber")

package net.primal.android.core.compose.icons.primaliconpack

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush.Companion.linearGradient
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

public val PrimalIcons.ArrowBack: ImageVector
    get() {
        if (_union != null) {
            return _union!!
        }
        _union = Builder(name = "Union", defaultWidth = 24.0.dp, defaultHeight = 24.0.dp,
                viewportWidth = 24.0f, viewportHeight = 24.0f).apply {
            path(fill = SolidColor(Color(0xFFCA079F)), stroke = null, strokeLineWidth = 0.0f,
                    strokeLineCap = Butt, strokeLineJoin = Miter, strokeLineMiter = 4.0f,
                    pathFillType = NonZero) {
                moveTo(12.1904f, 2.8211f)
                curveTo(12.6633f, 2.3158f, 12.6633f, 1.5304f, 12.1904f, 1.0251f)
                curveTo(11.6711f, 0.4703f, 10.7908f, 0.4703f, 10.2715f, 1.0251f)
                lineTo(0.6395f, 11.3167f)
                curveTo(0.2797f, 11.7012f, 0.2797f, 12.2988f, 0.6395f, 12.6833f)
                lineTo(10.2715f, 22.9748f)
                curveTo(10.7908f, 23.5297f, 11.6711f, 23.5297f, 12.1904f, 22.9748f)
                curveTo(12.6633f, 22.4696f, 12.6633f, 21.6842f, 12.1904f, 21.1789f)
                lineTo(4.8764f, 13.3641f)
                horizontalLineTo(22.6359f)
                curveTo(23.3893f, 13.3641f, 24.0f, 12.7534f, 24.0f, 12.0f)
                curveTo(24.0f, 11.2466f, 23.3893f, 10.6359f, 22.6359f, 10.6359f)
                horizontalLineTo(4.8764f)
                lineTo(12.1904f, 2.8211f)
                close()
            }
            path(fill = linearGradient(0.0f to Color(0xFFFA4343), 1.0f to Color(0xFF5B12A4), start =
                    Offset(0.0f,0.0f), end = Offset(24.9258f,29.21f)), stroke = null,
                    strokeLineWidth = 0.0f, strokeLineCap = Butt, strokeLineJoin = Miter,
                    strokeLineMiter = 4.0f, pathFillType = NonZero) {
                moveTo(12.1904f, 2.8211f)
                curveTo(12.6633f, 2.3158f, 12.6633f, 1.5304f, 12.1904f, 1.0251f)
                curveTo(11.6711f, 0.4703f, 10.7908f, 0.4703f, 10.2715f, 1.0251f)
                lineTo(0.6395f, 11.3167f)
                curveTo(0.2797f, 11.7012f, 0.2797f, 12.2988f, 0.6395f, 12.6833f)
                lineTo(10.2715f, 22.9748f)
                curveTo(10.7908f, 23.5297f, 11.6711f, 23.5297f, 12.1904f, 22.9748f)
                curveTo(12.6633f, 22.4696f, 12.6633f, 21.6842f, 12.1904f, 21.1789f)
                lineTo(4.8764f, 13.3641f)
                horizontalLineTo(22.6359f)
                curveTo(23.3893f, 13.3641f, 24.0f, 12.7534f, 24.0f, 12.0f)
                curveTo(24.0f, 11.2466f, 23.3893f, 10.6359f, 22.6359f, 10.6359f)
                horizontalLineTo(4.8764f)
                lineTo(12.1904f, 2.8211f)
                close()
            }
        }
        .build()
        return _union!!
    }

private var _union: ImageVector? = null
