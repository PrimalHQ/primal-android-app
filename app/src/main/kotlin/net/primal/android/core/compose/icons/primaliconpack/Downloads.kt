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

public val PrimalIcons.Downloads: ImageVector
    get() {
        if (_downloads != null) {
            return _downloads!!
        }
        _downloads = Builder(name = "Downloads", defaultWidth = 24.0.dp, defaultHeight = 24.0.dp,
                viewportWidth = 24.0f, viewportHeight = 24.0f).apply {
            path(fill = SolidColor(Color(0xFFAAAAAA)), stroke = null, strokeLineWidth = 0.0f,
                    strokeLineCap = Butt, strokeLineJoin = Miter, strokeLineMiter = 4.0f,
                    pathFillType = NonZero) {
                moveTo(11.25f, 1.0394f)
                curveTo(11.25f, 0.5942f, 11.5948f, 0.25f, 12.0f, 0.25f)
                curveTo(12.4052f, 0.25f, 12.75f, 0.5942f, 12.75f, 1.0394f)
                verticalLineTo(14.9286f)
                lineTo(18.5155f, 9.7918f)
                curveTo(18.8279f, 9.5135f, 19.2963f, 9.5494f, 19.5664f, 9.877f)
                curveTo(19.8412f, 10.2102f, 19.8025f, 10.7136f, 19.4845f, 10.9969f)
                lineTo(12.4989f, 17.2207f)
                curveTo(12.2146f, 17.474f, 11.7855f, 17.474f, 11.5011f, 17.2207f)
                lineTo(4.5155f, 10.9969f)
                curveTo(4.1975f, 10.7136f, 4.1589f, 10.2102f, 4.4336f, 9.877f)
                curveTo(4.7038f, 9.5494f, 5.1722f, 9.5135f, 5.4845f, 9.7918f)
                lineTo(11.25f, 14.9286f)
                verticalLineTo(1.0394f)
                close()
            }
            path(fill = SolidColor(Color(0xFFAAAAAA)), stroke = null, strokeLineWidth = 0.0f,
                    strokeLineCap = Butt, strokeLineJoin = Miter, strokeLineMiter = 4.0f,
                    pathFillType = NonZero) {
                moveTo(0.25f, 19.0f)
                curveTo(0.25f, 18.5858f, 0.5858f, 18.25f, 1.0f, 18.25f)
                curveTo(1.4142f, 18.25f, 1.75f, 18.5858f, 1.75f, 19.0f)
                verticalLineTo(21.0f)
                curveTo(1.75f, 21.6904f, 2.3096f, 22.25f, 3.0f, 22.25f)
                horizontalLineTo(21.0f)
                curveTo(21.6904f, 22.25f, 22.25f, 21.6904f, 22.25f, 21.0f)
                verticalLineTo(19.0f)
                curveTo(22.25f, 18.5858f, 22.5858f, 18.25f, 23.0f, 18.25f)
                curveTo(23.4142f, 18.25f, 23.75f, 18.5858f, 23.75f, 19.0f)
                verticalLineTo(22.0f)
                curveTo(23.75f, 22.9665f, 22.9665f, 23.75f, 22.0f, 23.75f)
                horizontalLineTo(2.0f)
                curveTo(1.0335f, 23.75f, 0.25f, 22.9665f, 0.25f, 22.0f)
                verticalLineTo(19.0f)
                close()
            }
        }
        .build()
        return _downloads!!
    }

private var _downloads: ImageVector? = null
