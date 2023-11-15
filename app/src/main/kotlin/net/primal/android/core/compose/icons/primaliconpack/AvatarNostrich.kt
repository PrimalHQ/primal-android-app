@file:Suppress("MagicNumber")

package net.primal.android.core.compose.icons.primaliconpack

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType.Companion.EvenOdd
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap.Companion.Butt
import androidx.compose.ui.graphics.StrokeJoin.Companion.Miter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.ImageVector.Builder
import androidx.compose.ui.graphics.vector.group
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp
import net.primal.android.core.compose.icons.PrimalIcons

public val PrimalIcons.AvatarNostrich: ImageVector
    get() {
        if (_avatarNostrich != null) {
            return _avatarNostrich!!
        }
        _avatarNostrich = Builder(name = "Avatarnostrich", defaultWidth = 52.0.dp, defaultHeight =
                52.0.dp, viewportWidth = 52.0f, viewportHeight = 52.0f).apply {
            group {
                path(fill = SolidColor(Color(0xFF444444)), stroke = null, strokeLineWidth = 0.0f,
                        strokeLineCap = Butt, strokeLineJoin = Miter, strokeLineMiter = 4.0f,
                        pathFillType = EvenOdd) {
                    moveTo(16.6465f, 48.109f)
                    curveTo(13.1616f, 42.4438f, 6.8505f, 31.0555f, 6.9066f, 22.9589f)
                    curveTo(6.9715f, 13.5939f, 11.5117f, 10.033f, 12.3538f, 9.3724f)
                    lineTo(12.3906f, 9.3435f)
                    curveTo(15.6435f, 6.782f, 19.899f, 5.4842f, 22.9531f, 6.9061f)
                    curveTo(23.4667f, 6.9719f, 23.9446f, 7.0278f, 24.3902f, 7.0798f)
                    curveTo(26.7669f, 7.3576f, 28.2227f, 7.5277f, 29.25f, 8.531f)
                    curveTo(29.7432f, 9.0127f, 30.0055f, 9.8762f, 30.2813f, 10.7838f)
                    curveTo(30.6875f, 12.1209f, 31.1229f, 13.5539f, 32.3685f, 14.0034f)
                    curveTo(33.2009f, 14.3037f, 34.0351f, 14.3669f, 34.9556f, 14.4366f)
                    curveTo(36.3477f, 14.5419f, 39.562f, 15.0685f, 41.6406f, 16.0466f)
                    curveTo(44.8061f, 17.5362f, 45.0938f, 19.0935f, 45.0938f, 19.0935f)
                    curveTo(45.0938f, 19.0935f, 45.5f, 20.3779f, 45.0938f, 20.9873f)
                    curveTo(44.6875f, 21.5966f, 43.875f, 21.531f, 42.8594f, 20.9216f)
                    curveTo(41.2344f, 20.5153f, 36.5625f, 20.1092f, 36.5625f, 20.1092f)
                    curveTo(36.5625f, 20.1092f, 32.7945f, 19.5435f, 31.0131f, 19.8215f)
                    curveTo(29.9638f, 19.9852f, 29.0158f, 20.4836f, 28.0577f, 20.9872f)
                    curveTo(27.3894f, 21.3386f, 26.7161f, 21.6925f, 26.0f, 21.9373f)
                    curveTo(24.2574f, 22.533f, 21.4673f, 23.0383f, 19.899f, 23.0383f)
                    curveTo(18.3306f, 23.0383f, 18.0982f, 26.5133f, 18.8921f, 28.1614f)
                    curveTo(19.1283f, 28.6518f, 19.3131f, 29.1035f, 19.4975f, 29.5542f)
                    curveTo(19.9327f, 30.6181f, 20.3657f, 31.6763f, 21.4673f, 33.2249f)
                    curveTo(23.8916f, 36.6327f, 32.5723f, 43.9376f, 36.8233f, 47.4267f)
                    curveTo(44.6408f, 43.47f, 50.0f, 35.3609f, 50.0f, 26.0f)
                    curveTo(50.0f, 12.7452f, 39.2548f, 2.0f, 26.0f, 2.0f)
                    curveTo(12.7452f, 2.0f, 2.0f, 12.7452f, 2.0f, 26.0f)
                    curveTo(2.0f, 35.9367f, 8.0387f, 44.4629f, 16.6465f, 48.109f)
                    close()
                    moveTo(52.0f, 26.0f)
                    curveTo(52.0f, 40.3594f, 40.3594f, 52.0f, 26.0f, 52.0f)
                    curveTo(11.6406f, 52.0f, 0.0f, 40.3594f, 0.0f, 26.0f)
                    curveTo(0.0f, 11.6406f, 11.6406f, 0.0f, 26.0f, 0.0f)
                    curveTo(40.3594f, 0.0f, 52.0f, 11.6406f, 52.0f, 26.0f)
                    close()
                    moveTo(25.5523f, 12.5906f)
                    curveTo(25.4912f, 13.2029f, 23.6587f, 14.0409f, 21.6712f, 13.8426f)
                    curveTo(19.6837f, 13.6442f, 18.355f, 11.7781f, 18.355f, 11.0597f)
                    curveTo(18.355f, 10.5622f, 20.1532f, 9.7545f, 22.1407f, 9.9529f)
                    curveTo(24.1282f, 10.1512f, 25.5926f, 12.1872f, 25.5523f, 12.5906f)
                    close()
                }
            }
        }
        .build()
        return _avatarNostrich!!
    }

private var _avatarNostrich: ImageVector? = null
