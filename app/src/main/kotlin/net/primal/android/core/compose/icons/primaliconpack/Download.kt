@file:Suppress("MagicNumber")

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

public val PrimalIcons.Download: ImageVector
    get() {
        if (_download != null) {
            return _download!!
        }
        _download = Builder(name = "Download", defaultWidth = 24.0.dp, defaultHeight = 24.0.dp,
                viewportWidth = 24.0f, viewportHeight = 24.0f).apply {
            path(fill = SolidColor(Color(0xFFAAAAAA)), stroke = null, strokeLineWidth = 0.0f,
                    strokeLineCap = Butt, strokeLineJoin = Miter, strokeLineMiter = 4.0f,
                    pathFillType = NonZero) {
                moveTo(13.0f, 1.0394f)
                curveTo(13.0f, 0.4654f, 12.5523f, 0.0f, 12.0f, 0.0f)
                curveTo(11.4477f, 0.0f, 11.0f, 0.4654f, 11.0f, 1.0394f)
                verticalLineTo(14.371f)
                lineTo(5.6508f, 9.6052f)
                curveTo(5.2315f, 9.2316f, 4.6002f, 9.282f, 4.2408f, 9.7179f)
                curveTo(3.8813f, 10.1538f, 3.9299f, 10.81f, 4.3492f, 11.1836f)
                lineTo(11.3348f, 17.4073f)
                curveTo(11.714f, 17.7451f, 12.2861f, 17.7451f, 12.6652f, 17.4073f)
                lineTo(19.6508f, 11.1836f)
                curveTo(20.0701f, 10.81f, 20.1187f, 10.1538f, 19.7593f, 9.7179f)
                curveTo(19.3999f, 9.282f, 18.7686f, 9.2316f, 18.3492f, 9.6052f)
                lineTo(13.0f, 14.371f)
                verticalLineTo(1.0394f)
                close()
            }
            path(fill = SolidColor(Color(0xFFAAAAAA)), stroke = null, strokeLineWidth = 0.0f,
                    strokeLineCap = Butt, strokeLineJoin = Miter, strokeLineMiter = 4.0f,
                    pathFillType = NonZero) {
                moveTo(1.0f, 18.0f)
                curveTo(0.4477f, 18.0f, 0.0f, 18.4477f, 0.0f, 19.0f)
                verticalLineTo(22.0f)
                curveTo(0.0f, 23.1046f, 0.8954f, 24.0f, 2.0f, 24.0f)
                horizontalLineTo(22.0f)
                curveTo(23.1046f, 24.0f, 24.0f, 23.1046f, 24.0f, 22.0f)
                verticalLineTo(19.0f)
                curveTo(24.0f, 18.4477f, 23.5523f, 18.0f, 23.0f, 18.0f)
                curveTo(22.4477f, 18.0f, 22.0f, 18.4477f, 22.0f, 19.0f)
                verticalLineTo(21.0f)
                curveTo(22.0f, 21.5523f, 21.5523f, 22.0f, 21.0f, 22.0f)
                horizontalLineTo(3.0f)
                curveTo(2.4477f, 22.0f, 2.0f, 21.5523f, 2.0f, 21.0f)
                verticalLineTo(19.0f)
                curveTo(2.0f, 18.4477f, 1.5523f, 18.0f, 1.0f, 18.0f)
                close()
            }
        }
        .build()
        return _download!!
    }

private var _download: ImageVector? = null
