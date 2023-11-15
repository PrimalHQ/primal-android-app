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

public val PrimalIcons.AvatarDefault: ImageVector
    get() {
        if (_avatarDefault != null) {
            return _avatarDefault!!
        }
        _avatarDefault = Builder(name = "Avatardefault", defaultWidth = 52.0.dp, defaultHeight =
                52.0.dp, viewportWidth = 52.0f, viewportHeight = 52.0f).apply {
            path(fill = SolidColor(Color(0xFF444444)), stroke = null, strokeLineWidth = 0.0f,
                    strokeLineCap = Butt, strokeLineJoin = Miter, strokeLineMiter = 4.0f,
                    pathFillType = EvenOdd) {
                moveTo(7.2818f, 41.0223f)
                curveTo(10.2003f, 39.8377f, 15.6033f, 37.7566f, 19.6444f, 36.85f)
                verticalLineTo(34.7365f)
                curveTo(18.6589f, 33.5244f, 17.8956f, 31.3877f, 17.5446f, 28.7916f)
                curveTo(16.8769f, 28.1679f, 16.2208f, 27.065f, 15.7892f, 25.7263f)
                curveTo(15.117f, 23.6414f, 15.221f, 21.7021f, 15.9858f, 21.1263f)
                curveTo(15.9223f, 20.4163f, 15.8889f, 19.6789f, 15.8889f, 18.9222f)
                curveTo(15.8889f, 18.5928f, 15.8952f, 18.267f, 15.9076f, 17.9455f)
                curveTo(15.9167f, 17.7091f, 15.9291f, 17.475f, 15.9447f, 17.2435f)
                curveTo(15.7198f, 16.5345f, 15.6f, 15.79f, 15.6f, 15.0222f)
                curveTo(15.6f, 10.2358f, 20.2562f, 6.3556f, 26.0f, 6.3556f)
                curveTo(31.7438f, 6.3556f, 36.4f, 10.2358f, 36.4f, 15.0222f)
                curveTo(36.4f, 15.79f, 36.2802f, 16.5345f, 36.0552f, 17.2435f)
                curveTo(36.072f, 17.4932f, 36.0852f, 17.7459f, 36.0945f, 18.0015f)
                curveTo(36.1055f, 18.3048f, 36.1111f, 18.6119f, 36.1111f, 18.9222f)
                curveTo(36.1111f, 19.6789f, 36.0777f, 20.4163f, 36.0142f, 21.1263f)
                curveTo(36.779f, 21.7021f, 36.883f, 23.6414f, 36.2108f, 25.7263f)
                curveTo(35.7792f, 27.065f, 35.1231f, 28.1679f, 34.4554f, 28.7916f)
                curveTo(34.1044f, 31.3877f, 33.3411f, 33.5244f, 32.3556f, 34.7365f)
                verticalLineTo(36.85f)
                curveTo(36.3967f, 37.7566f, 41.7997f, 39.8377f, 44.7182f, 41.0223f)
                curveTo(48.0227f, 36.9101f, 50.0f, 31.686f, 50.0f, 26.0f)
                curveTo(50.0f, 12.7452f, 39.2548f, 2.0f, 26.0f, 2.0f)
                curveTo(12.7452f, 2.0f, 2.0f, 12.7452f, 2.0f, 26.0f)
                curveTo(2.0f, 31.686f, 3.9773f, 36.9101f, 7.2818f, 41.0223f)
                close()
                moveTo(5.364f, 41.8185f)
                curveTo(1.9998f, 37.4363f, 0.0f, 31.9517f, 0.0f, 26.0f)
                curveTo(0.0f, 11.6406f, 11.6406f, 0.0f, 26.0f, 0.0f)
                curveTo(40.3594f, 0.0f, 52.0f, 11.6406f, 52.0f, 26.0f)
                curveTo(52.0f, 32.2822f, 49.7719f, 38.0441f, 46.0629f, 42.5384f)
                curveTo(41.2941f, 48.3168f, 34.0772f, 52.0f, 26.0f, 52.0f)
                curveTo(25.9439f, 52.0f, 25.8879f, 51.9998f, 25.8319f, 51.9995f)
                curveTo(17.4926f, 51.9467f, 10.0849f, 47.9679f, 5.364f, 41.8185f)
                close()
            }
        }
        .build()
        return _avatarDefault!!
    }

private var _avatarDefault: ImageVector? = null
