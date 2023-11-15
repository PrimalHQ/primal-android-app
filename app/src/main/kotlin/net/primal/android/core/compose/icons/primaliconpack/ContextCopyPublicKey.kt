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

public val PrimalIcons.ContextCopyPublicKey: ImageVector
    get() {
        if (_contextCopyPublicKey != null) {
            return _contextCopyPublicKey!!
        }
        _contextCopyPublicKey = Builder(name = "ContextCopyPublicKey", defaultWidth = 20.0.dp,
                defaultHeight = 20.0.dp, viewportWidth = 20.0f, viewportHeight = 20.0f).apply {
            path(fill = SolidColor(Color(0xFFffffff)), stroke = null, strokeLineWidth = 0.0f,
                    strokeLineCap = Butt, strokeLineJoin = Miter, strokeLineMiter = 4.0f,
                    pathFillType = NonZero) {
                moveTo(13.0f, 1.5f)
                horizontalLineTo(2.0f)
                curveTo(1.7239f, 1.5f, 1.5f, 1.7239f, 1.5f, 2.0f)
                verticalLineTo(13.0f)
                curveTo(1.5f, 13.2761f, 1.7239f, 13.5f, 2.0f, 13.5f)
                horizontalLineTo(3.25f)
                curveTo(3.6642f, 13.5f, 4.0f, 13.8358f, 4.0f, 14.25f)
                curveTo(4.0f, 14.6642f, 3.6642f, 15.0f, 3.25f, 15.0f)
                horizontalLineTo(2.0f)
                curveTo(0.8954f, 15.0f, 0.0f, 14.1046f, 0.0f, 13.0f)
                verticalLineTo(2.0f)
                curveTo(0.0f, 0.8954f, 0.8954f, 0.0f, 2.0f, 0.0f)
                horizontalLineTo(13.0f)
                curveTo(14.1046f, 0.0f, 15.0f, 0.8954f, 15.0f, 2.0f)
                verticalLineTo(3.25f)
                curveTo(15.0f, 3.6642f, 14.6642f, 4.0f, 14.25f, 4.0f)
                curveTo(13.8358f, 4.0f, 13.5f, 3.6642f, 13.5f, 3.25f)
                verticalLineTo(2.0f)
                curveTo(13.5f, 1.7239f, 13.2761f, 1.5f, 13.0f, 1.5f)
                close()
            }
            path(fill = SolidColor(Color(0xFFffffff)), stroke = null, strokeLineWidth = 0.0f,
                    strokeLineCap = Butt, strokeLineJoin = Miter, strokeLineMiter = 4.0f,
                    pathFillType = EvenOdd) {
                moveTo(17.8929f, 12.8344f)
                curveTo(16.8761f, 13.8881f, 15.2035f, 14.0216f, 13.6782f, 13.3137f)
                lineTo(12.6148f, 14.4157f)
                curveTo(12.5688f, 14.4634f, 12.5041f, 14.4881f, 12.4376f, 14.483f)
                lineTo(10.7966f, 14.3558f)
                lineTo(10.9625f, 15.8294f)
                curveTo(10.9702f, 15.8974f, 10.9468f, 15.9603f, 10.9048f, 16.0057f)
                curveTo(10.8609f, 16.0537f, 10.7959f, 16.0818f, 10.7243f, 16.0762f)
                lineTo(9.247f, 15.9617f)
                lineTo(9.4129f, 17.4353f)
                curveTo(9.4214f, 17.5114f, 9.3913f, 17.5811f, 9.3396f, 17.6272f)
                curveTo(9.2964f, 17.6655f, 9.2383f, 17.6872f, 9.1748f, 17.6822f)
                lineTo(6.4081f, 17.4678f)
                curveTo(6.1575f, 17.4484f, 5.9703f, 17.2272f, 5.9925f, 16.977f)
                lineTo(6.0515f, 16.3171f)
                curveTo(6.0694f, 16.1143f, 6.1562f, 15.9245f, 6.2969f, 15.7786f)
                lineTo(11.0895f, 10.8118f)
                curveTo(10.3293f, 9.311f, 10.4042f, 7.6334f, 11.421f, 6.5796f)
                curveTo(12.7904f, 5.1605f, 15.3493f, 5.4104f, 17.1363f, 7.1375f)
                curveTo(18.9233f, 8.8646f, 19.2623f, 11.4152f, 17.8929f, 12.8344f)
                close()
                moveTo(14.347f, 10.0282f)
                curveTo(14.8833f, 10.5465f, 15.7341f, 10.535f, 16.2476f, 10.0028f)
                curveTo(16.761f, 9.4707f, 16.7428f, 8.6194f, 16.2066f, 8.1011f)
                curveTo(15.6703f, 7.5828f, 14.8195f, 7.5943f, 14.306f, 8.1264f)
                curveTo(13.7926f, 8.6586f, 13.8108f, 9.5099f, 14.347f, 10.0282f)
                close()
            }
        }
        .build()
        return _contextCopyPublicKey!!
    }

private var _contextCopyPublicKey: ImageVector? = null
