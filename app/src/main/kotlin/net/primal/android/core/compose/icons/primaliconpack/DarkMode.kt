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

public val PrimalIcons.DarkMode: ImageVector
    get() {
        if (_darkMode != null) {
            return _darkMode!!
        }
        _darkMode = Builder(name = "DarkMode", defaultWidth = 28.0.dp, defaultHeight = 28.0.dp,
                viewportWidth = 28.0f, viewportHeight = 28.0f).apply {
            path(fill = SolidColor(Color(0xFFAAAAAA)), stroke = null, strokeLineWidth = 0.0f,
                    strokeLineCap = Butt, strokeLineJoin = Miter, strokeLineMiter = 4.0f,
                    pathFillType = EvenOdd) {
                moveTo(22.4141f, 22.0818f)
                curveTo(21.9506f, 22.1378f, 21.4787f, 22.1667f, 21.0f, 22.1667f)
                curveTo(14.5567f, 22.1667f, 9.3333f, 16.9433f, 9.3333f, 10.5f)
                curveTo(9.3333f, 7.3628f, 10.5716f, 4.5148f, 12.586f, 2.4181f)
                curveTo(12.7144f, 2.2845f, 12.846f, 2.1539f, 12.9806f, 2.0264f)
                curveTo(13.3035f, 1.7207f, 13.6439f, 1.4333f, 14.0f, 1.1658f)
                curveTo(14.0807f, 1.1052f, 14.1623f, 1.0456f, 14.2446f, 0.987f)
                curveTo(14.6637f, 0.6888f, 14.5144f, 0.0f, 14.0f, 0.0f)
                curveTo(13.8486f, 0.0f, 13.6978f, 0.0024f, 13.5475f, 0.0072f)
                curveTo(13.0932f, 0.0216f, 12.6443f, 0.0577f, 12.2019f, 0.1144f)
                curveTo(11.9633f, 0.145f, 11.7267f, 0.1816f, 11.492f, 0.224f)
                curveTo(4.9568f, 1.406f, 0.0f, 7.1241f, 0.0f, 14.0f)
                curveTo(0.0f, 21.732f, 6.268f, 28.0f, 14.0f, 28.0f)
                curveTo(17.671f, 28.0f, 21.012f, 26.5871f, 23.5088f, 24.2754f)
                curveTo(23.6538f, 24.1412f, 23.7959f, 24.004f, 23.9351f, 23.8638f)
                curveTo(24.2191f, 23.5777f, 24.4909f, 23.2794f, 24.7495f, 22.9698f)
                curveTo(24.8605f, 22.8369f, 24.9691f, 22.702f, 25.0752f, 22.565f)
                curveTo(25.4279f, 22.1095f, 24.9126f, 21.5096f, 24.3608f, 21.6753f)
                curveTo(24.2555f, 21.7069f, 24.1497f, 21.7371f, 24.0432f, 21.7658f)
                curveTo(23.66f, 21.869f, 23.269f, 21.9533f, 22.8713f, 22.0174f)
                curveTo(22.7198f, 22.0418f, 22.5674f, 22.0633f, 22.4141f, 22.0818f)
                close()
                moveTo(8.8597f, 3.5238f)
                curveTo(4.9939f, 5.4243f, 2.3333f, 9.4014f, 2.3333f, 14.0f)
                curveTo(2.3333f, 20.4433f, 7.5567f, 25.6667f, 14.0f, 25.6667f)
                curveTo(15.9075f, 25.6667f, 17.708f, 25.2095f, 19.2982f, 24.3976f)
                curveTo(12.3688f, 23.5579f, 7.0f, 17.6559f, 7.0f, 10.5f)
                curveTo(7.0f, 7.9594f, 7.6774f, 5.5771f, 8.8597f, 3.5238f)
                close()
            }
            path(fill = SolidColor(Color(0xFFAAAAAA)), stroke = null, strokeLineWidth = 0.0f,
                    strokeLineCap = Butt, strokeLineJoin = Miter, strokeLineMiter = 4.0f,
                    pathFillType = NonZero) {
                moveTo(18.319f, 6.1619f)
                curveTo(18.2494f, 6.1387f, 18.1947f, 6.084f, 18.1715f, 6.0143f)
                lineTo(17.5547f, 4.1641f)
                curveTo(17.4838f, 3.9514f, 17.1829f, 3.9514f, 17.112f, 4.1641f)
                lineTo(16.4953f, 6.0143f)
                curveTo(16.472f, 6.084f, 16.4174f, 6.1387f, 16.3477f, 6.1619f)
                lineTo(14.4974f, 6.7787f)
                curveTo(14.2847f, 6.8496f, 14.2847f, 7.1505f, 14.4974f, 7.2214f)
                lineTo(16.3477f, 7.8381f)
                curveTo(16.4174f, 7.8614f, 16.472f, 7.916f, 16.4953f, 7.9857f)
                lineTo(17.112f, 9.8359f)
                curveTo(17.1829f, 10.0487f, 17.4838f, 10.0487f, 17.5547f, 9.8359f)
                lineTo(18.1715f, 7.9857f)
                curveTo(18.1947f, 7.916f, 18.2494f, 7.8614f, 18.319f, 7.8381f)
                lineTo(20.1693f, 7.2214f)
                curveTo(20.382f, 7.1505f, 20.382f, 6.8496f, 20.1693f, 6.7787f)
                lineTo(18.319f, 6.1619f)
                close()
            }
            path(fill = SolidColor(Color(0xFFAAAAAA)), stroke = null, strokeLineWidth = 0.0f,
                    strokeLineCap = Butt, strokeLineJoin = Miter, strokeLineMiter = 4.0f,
                    pathFillType = NonZero) {
                moveTo(24.997f, 10.9612f)
                curveTo(25.0293f, 11.058f, 25.1053f, 11.134f, 25.2022f, 11.1663f)
                lineTo(27.7748f, 12.0239f)
                curveTo(28.0706f, 12.1225f, 28.0706f, 12.5409f, 27.7748f, 12.6395f)
                lineTo(25.2022f, 13.497f)
                curveTo(25.1053f, 13.5293f, 25.0293f, 13.6053f, 24.997f, 13.7022f)
                lineTo(24.1394f, 16.2748f)
                curveTo(24.0408f, 16.5706f, 23.6225f, 16.5706f, 23.5239f, 16.2748f)
                lineTo(22.6663f, 13.7022f)
                curveTo(22.634f, 13.6053f, 22.558f, 13.5293f, 22.4611f, 13.497f)
                lineTo(19.8885f, 12.6395f)
                curveTo(19.5927f, 12.5409f, 19.5927f, 12.1225f, 19.8885f, 12.0239f)
                lineTo(22.4611f, 11.1663f)
                curveTo(22.558f, 11.134f, 22.634f, 11.058f, 22.6663f, 10.9611f)
                lineTo(23.5239f, 8.3885f)
                curveTo(23.6225f, 8.0927f, 24.0408f, 8.0927f, 24.1394f, 8.3885f)
                lineTo(24.997f, 10.9612f)
                close()
            }
        }
        .build()
        return _darkMode!!
    }

private var _darkMode: ImageVector? = null
