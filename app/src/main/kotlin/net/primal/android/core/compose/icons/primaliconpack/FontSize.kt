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

public val PrimalIcons.FontSize: ImageVector
    get() {
        if (_fontsize != null) {
            return _fontsize!!
        }
        _fontsize = Builder(name = "Fontsize", defaultWidth = 25.0.dp, defaultHeight = 16.0.dp,
                viewportWidth = 25.0f, viewportHeight = 16.0f).apply {
            path(fill = SolidColor(Color(0xFFAAAAAA)), stroke = null, strokeLineWidth = 0.0f,
                    strokeLineCap = Butt, strokeLineJoin = Miter, strokeLineMiter = 4.0f,
                    pathFillType = NonZero) {
                moveTo(9.0047f, 0.4f)
                lineTo(14.3047f, 15.0f)
                horizontalLineTo(11.6847f)
                lineTo(10.3047f, 11.12f)
                horizontalLineTo(4.7047f)
                lineTo(3.3047f, 15.0f)
                horizontalLineTo(0.7047f)
                lineTo(6.1247f, 0.4f)
                horizontalLineTo(9.0047f)
                close()
                moveTo(5.3847f, 8.92f)
                horizontalLineTo(9.6447f)
                lineTo(7.5447f, 2.78f)
                lineTo(5.3847f, 8.92f)
                close()
                moveTo(20.2588f, 4.22f)
                curveTo(23.1988f, 4.22f, 24.3588f, 5.54f, 24.4988f, 7.38f)
                curveTo(24.5388f, 7.92f, 24.5588f, 8.38f, 24.5588f, 8.98f)
                verticalLineTo(15.0f)
                horizontalLineTo(22.3588f)
                lineTo(22.2388f, 13.66f)
                curveTo(21.6588f, 15.04f, 20.2988f, 15.28f, 18.7988f, 15.28f)
                curveTo(16.9588f, 15.28f, 15.4188f, 14.18f, 15.4188f, 12.16f)
                curveTo(15.4188f, 9.44f, 17.5188f, 8.52f, 21.6588f, 8.52f)
                horizontalLineTo(22.1388f)
                verticalLineTo(8.2f)
                curveTo(22.1388f, 7.06f, 21.7988f, 6.3f, 20.1588f, 6.3f)
                curveTo(18.7588f, 6.3f, 18.1188f, 6.76f, 17.9788f, 7.86f)
                horizontalLineTo(15.7788f)
                curveTo(15.8588f, 5.66f, 17.1188f, 4.22f, 20.2588f, 4.22f)
                close()
                moveTo(17.8588f, 11.86f)
                curveTo(17.8588f, 12.72f, 18.5388f, 13.2f, 19.5788f, 13.2f)
                curveTo(20.8588f, 13.2f, 22.1388f, 12.5f, 22.1388f, 11.36f)
                verticalLineTo(10.44f)
                curveTo(19.5188f, 10.34f, 17.8588f, 10.54f, 17.8588f, 11.86f)
                close()
            }
        }
        .build()
        return _fontsize!!
    }

private var _fontsize: ImageVector? = null
