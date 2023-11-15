@file:Suppress("MagicNumber")

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

public val PrimalIcons.NavMessages: ImageVector
    get() {
        if (_navmessages != null) {
            return _navmessages!!
        }
        _navmessages = Builder(name = "Navmessages", defaultWidth = 25.0.dp, defaultHeight =
                24.0.dp, viewportWidth = 25.0f, viewportHeight = 24.0f).apply {
            path(fill = SolidColor(Color(0xFFAAAAAA)), stroke = null, strokeLineWidth = 0.0f,
                    strokeLineCap = Butt, strokeLineJoin = Miter, strokeLineMiter = 4.0f,
                    pathFillType = EvenOdd) {
                moveTo(0.0029f, 4.0f)
                curveTo(0.0029f, 2.8954f, 0.8984f, 2.0f, 2.0029f, 2.0f)
                horizontalLineTo(22.0029f)
                curveTo(23.1075f, 2.0f, 24.0029f, 2.8954f, 24.0029f, 4.0f)
                verticalLineTo(20.0f)
                curveTo(24.0029f, 21.1046f, 23.1075f, 22.0f, 22.0029f, 22.0f)
                horizontalLineTo(2.0029f)
                curveTo(0.8984f, 22.0f, 0.0029f, 21.1046f, 0.0029f, 20.0f)
                verticalLineTo(4.0f)
                close()
                moveTo(2.5429f, 4.4432f)
                curveTo(2.3615f, 4.2943f, 2.4668f, 4.0f, 2.7016f, 4.0f)
                horizontalLineTo(21.3043f)
                curveTo(21.5391f, 4.0f, 21.6444f, 4.2943f, 21.4629f, 4.4432f)
                lineTo(13.2718f, 11.1664f)
                curveTo(12.5342f, 11.7719f, 11.4716f, 11.7719f, 10.734f, 11.1664f)
                lineTo(2.5429f, 4.4432f)
                close()
                moveTo(2.8196f, 7.2784f)
                curveTo(2.4931f, 7.0113f, 2.0029f, 7.2435f, 2.0029f, 7.6654f)
                verticalLineTo(19.0f)
                curveTo(2.0029f, 19.5523f, 2.4506f, 20.0f, 3.0029f, 20.0f)
                horizontalLineTo(21.0029f)
                curveTo(21.5552f, 20.0f, 22.0029f, 19.5523f, 22.0029f, 19.0f)
                verticalLineTo(7.6654f)
                curveTo(22.0029f, 7.2435f, 21.5128f, 7.0113f, 21.1863f, 7.2784f)
                lineTo(13.2694f, 13.7559f)
                curveTo(12.5327f, 14.3586f, 11.4732f, 14.3586f, 10.7365f, 13.7559f)
                lineTo(2.8196f, 7.2784f)
                close()
            }
        }
        .build()
        return _navmessages!!
    }

private var _navmessages: ImageVector? = null
