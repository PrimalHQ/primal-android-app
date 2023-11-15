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

public val PrimalIcons.Settings: ImageVector
    get() {
        if (_settings != null) {
            return _settings!!
        }
        _settings = Builder(name = "Settings", defaultWidth = 24.0.dp, defaultHeight = 24.0.dp,
                viewportWidth = 24.0f, viewportHeight = 24.0f).apply {
            path(fill = SolidColor(Color(0xFFAAAAAA)), stroke = null, strokeLineWidth = 0.0f,
                    strokeLineCap = Butt, strokeLineJoin = Miter, strokeLineMiter = 4.0f,
                    pathFillType = EvenOdd) {
                moveTo(8.1468f, 1.2878f)
                curveTo(8.2524f, 0.5489f, 8.8853f, 0.0f, 9.6317f, 0.0f)
                horizontalLineTo(13.4788f)
                curveTo(14.2252f, 0.0f, 14.8581f, 0.5489f, 14.9637f, 1.2878f)
                lineTo(15.3505f, 3.9951f)
                curveTo(15.7839f, 4.2009f, 16.1978f, 4.4408f, 16.5887f, 4.7113f)
                lineTo(19.1277f, 3.6921f)
                curveTo(19.8205f, 3.414f, 20.6123f, 3.6877f, 20.9855f, 4.3342f)
                lineTo(22.909f, 7.6657f)
                curveTo(23.2823f, 8.3122f, 23.1233f, 9.1348f, 22.5361f, 9.5957f)
                lineTo(20.3838f, 11.2851f)
                curveTo(20.4027f, 11.5209f, 20.4124f, 11.7593f, 20.4124f, 12.0f)
                curveTo(20.4124f, 12.2406f, 20.4027f, 12.479f, 20.3838f, 12.7148f)
                lineTo(22.5361f, 14.4042f)
                curveTo(23.1233f, 14.8651f, 23.2823f, 15.6877f, 22.909f, 16.3342f)
                lineTo(20.9855f, 19.6658f)
                curveTo(20.6123f, 20.3122f, 19.8205f, 20.5859f, 19.1278f, 20.3078f)
                lineTo(16.5888f, 19.2887f)
                curveTo(16.1978f, 19.5592f, 15.7839f, 19.7991f, 15.3505f, 20.0049f)
                lineTo(14.9637f, 22.7122f)
                curveTo(14.8581f, 23.4511f, 14.2252f, 24.0f, 13.4788f, 24.0f)
                horizontalLineTo(9.6317f)
                curveTo(8.8853f, 24.0f, 8.2524f, 23.4511f, 8.1468f, 22.7122f)
                lineTo(7.76f, 20.0049f)
                curveTo(7.3265f, 19.7991f, 6.9124f, 19.5592f, 6.5213f, 19.2886f)
                lineTo(3.9824f, 20.3078f)
                curveTo(3.2896f, 20.5859f, 2.4978f, 20.3122f, 2.1246f, 19.6657f)
                lineTo(0.2011f, 16.3341f)
                curveTo(-0.1722f, 15.6877f, -0.0133f, 14.8652f, 0.5739f, 14.4042f)
                lineTo(2.7265f, 12.7145f)
                curveTo(2.7077f, 12.4787f, 2.6981f, 12.2403f, 2.6981f, 12.0f)
                curveTo(2.6981f, 11.7597f, 2.7077f, 11.5213f, 2.7265f, 11.2854f)
                lineTo(0.5739f, 9.5956f)
                curveTo(-0.0133f, 9.1347f, -0.1722f, 8.3122f, 0.2011f, 7.6658f)
                lineTo(2.1246f, 4.3341f)
                curveTo(2.4978f, 3.6877f, 3.2896f, 3.414f, 3.9823f, 3.6921f)
                lineTo(6.5217f, 4.7113f)
                curveTo(6.9126f, 4.4408f, 7.3266f, 4.2009f, 7.76f, 3.9951f)
                lineTo(8.1468f, 1.2878f)
                close()
                moveTo(10.0654f, 2.0f)
                lineTo(9.6198f, 5.1182f)
                curveTo(9.5944f, 5.296f, 9.4755f, 5.4465f, 9.3085f, 5.5125f)
                lineTo(9.0382f, 5.6192f)
                curveTo(8.4044f, 5.8695f, 7.8162f, 6.2118f, 7.2906f, 6.63f)
                lineTo(7.063f, 6.8111f)
                curveTo(6.9223f, 6.9231f, 6.7323f, 6.9509f, 6.5654f, 6.8839f)
                lineTo(3.6398f, 5.7097f)
                lineTo(2.1499f, 8.2902f)
                lineTo(4.6282f, 10.2357f)
                curveTo(4.7693f, 10.3464f, 4.8403f, 10.5244f, 4.8141f, 10.7019f)
                lineTo(4.7718f, 10.9889f)
                curveTo(4.7233f, 11.3182f, 4.6981f, 11.6558f, 4.6981f, 12.0f)
                curveTo(4.6981f, 12.3442f, 4.7233f, 12.6818f, 4.7718f, 13.0111f)
                lineTo(4.8141f, 13.2982f)
                curveTo(4.8403f, 13.4757f, 4.7693f, 13.6536f, 4.6282f, 13.7643f)
                lineTo(2.1499f, 15.7097f)
                lineTo(3.6398f, 18.2902f)
                lineTo(6.565f, 17.116f)
                curveTo(6.7319f, 17.049f, 6.9219f, 17.0768f, 7.0626f, 17.1888f)
                lineTo(7.2902f, 17.3699f)
                curveTo(7.8157f, 17.7881f, 8.4043f, 18.1305f, 9.0382f, 18.3808f)
                lineTo(9.3085f, 18.4875f)
                curveTo(9.4755f, 18.5535f, 9.5944f, 18.704f, 9.6198f, 18.8818f)
                lineTo(10.0654f, 22.0f)
                horizontalLineTo(13.0451f)
                lineTo(13.4907f, 18.8818f)
                curveTo(13.5161f, 18.704f, 13.6349f, 18.5535f, 13.802f, 18.4875f)
                lineTo(14.0723f, 18.3808f)
                curveTo(14.7061f, 18.1305f, 15.2943f, 17.7882f, 15.8199f, 17.37f)
                lineTo(16.0475f, 17.1889f)
                curveTo(16.1882f, 17.0769f, 16.3782f, 17.0491f, 16.5451f, 17.1161f)
                lineTo(19.4703f, 18.2902f)
                lineTo(20.9601f, 15.7097f)
                lineTo(18.4819f, 13.7646f)
                curveTo(18.3408f, 13.6538f, 18.2698f, 13.4759f, 18.296f, 13.2984f)
                lineTo(18.3383f, 13.0113f)
                curveTo(18.3869f, 12.6813f, 18.4124f, 12.3436f, 18.4124f, 12.0f)
                curveTo(18.4124f, 11.6564f, 18.3869f, 11.3187f, 18.3383f, 10.9885f)
                lineTo(18.296f, 10.7015f)
                curveTo(18.2699f, 10.524f, 18.3408f, 10.3461f, 18.4819f, 10.2353f)
                lineTo(20.9601f, 8.2902f)
                lineTo(19.4703f, 5.7097f)
                lineTo(16.5451f, 6.8839f)
                curveTo(16.3783f, 6.9509f, 16.1883f, 6.9231f, 16.0476f, 6.8112f)
                lineTo(15.82f, 6.6301f)
                curveTo(15.2943f, 6.2118f, 14.7061f, 5.8695f, 14.0723f, 5.6192f)
                lineTo(13.802f, 5.5125f)
                curveTo(13.6349f, 5.4465f, 13.5161f, 5.296f, 13.4907f, 5.1182f)
                lineTo(13.0451f, 2.0f)
                horizontalLineTo(10.0654f)
                close()
                moveTo(11.5552f, 9.8571f)
                curveTo(10.3719f, 9.8571f, 9.4124f, 10.8166f, 9.4124f, 12.0f)
                curveTo(9.4124f, 13.1834f, 10.3719f, 14.1429f, 11.5552f, 14.1429f)
                curveTo(12.7386f, 14.1429f, 13.6981f, 13.1834f, 13.6981f, 12.0f)
                curveTo(13.6981f, 10.8166f, 12.7386f, 9.8571f, 11.5552f, 9.8571f)
                close()
                moveTo(7.4124f, 12.0f)
                curveTo(7.4124f, 9.712f, 9.2674f, 7.8571f, 11.5552f, 7.8571f)
                curveTo(13.8431f, 7.8571f, 15.6981f, 9.712f, 15.6981f, 12.0f)
                curveTo(15.6981f, 14.288f, 13.8431f, 16.1429f, 11.5552f, 16.1429f)
                curveTo(9.2674f, 16.1429f, 7.4124f, 14.288f, 7.4124f, 12.0f)
                close()
            }
        }
        .build()
        return _settings!!
    }

private var _settings: ImageVector? = null
