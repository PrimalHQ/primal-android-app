package net.primal.android.core.compose.icons.primaliconpack

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType.Companion.NonZero
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap.Companion.Butt
import androidx.compose.ui.graphics.StrokeJoin.Companion.Miter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.ImageVector.Builder
import androidx.compose.ui.graphics.vector.group
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp
import net.primal.android.core.compose.icons.PrimalIcons

public val PrimalIcons.Mute: ImageVector
    get() {
        if (_mute != null) {
            return _mute!!
        }
        _mute = Builder(name = "Mute", defaultWidth = 16.0.dp, defaultHeight = 16.0.dp,
                viewportWidth = 16.0f, viewportHeight = 16.0f).apply {
            group {
                path(fill = SolidColor(Color(0xFFffffff)), stroke = null, strokeLineWidth = 0.0f,
                        strokeLineCap = Butt, strokeLineJoin = Miter, strokeLineMiter = 4.0f,
                        pathFillType = NonZero) {
                    moveTo(3.0f, 4.2303f)
                    lineTo(7.5938f, 0.5553f)
                    curveTo(7.7575f, 0.4243f, 8.0f, 0.5408f, 8.0f, 0.7505f)
                    verticalLineTo(14.7102f)
                    curveTo(8.0f, 14.9198f, 7.7575f, 15.0363f, 7.5938f, 14.9054f)
                    lineTo(3.0f, 11.2303f)
                    lineTo(1.0f, 11.2303f)
                    curveTo(0.4477f, 11.2303f, 0.0f, 10.7826f, 0.0f, 10.2303f)
                    verticalLineTo(5.2303f)
                    curveTo(0.0f, 4.678f, 0.4477f, 4.2303f, 1.0f, 4.2303f)
                    horizontalLineTo(3.0f)
                    close()
                }
                path(fill = SolidColor(Color(0xFFffffff)), stroke = null, strokeLineWidth = 0.0f,
                        strokeLineCap = Butt, strokeLineJoin = Miter, strokeLineMiter = 4.0f,
                        pathFillType = NonZero) {
                    moveTo(12.3988f, 2.4817f)
                    curveTo(12.1237f, 2.1764f, 12.1207f, 1.7216f, 12.4235f, 1.4399f)
                    curveTo(12.7263f, 1.1582f, 13.2198f, 1.1566f, 13.4992f, 1.4585f)
                    curveTo(15.0578f, 3.1423f, 16.0f, 5.334f, 16.0f, 7.7303f)
                    curveTo(16.0f, 10.1267f, 15.0578f, 12.3183f, 13.4992f, 14.0022f)
                    curveTo(13.2198f, 14.304f, 12.7263f, 14.3024f, 12.4235f, 14.0207f)
                    curveTo(12.1207f, 13.739f, 12.1237f, 13.2842f, 12.3988f, 12.9789f)
                    curveTo(13.6786f, 11.5589f, 14.4492f, 9.7283f, 14.4492f, 7.7303f)
                    curveTo(14.4492f, 5.7323f, 13.6786f, 3.9017f, 12.3988f, 2.4817f)
                    close()
                }
                path(fill = SolidColor(Color(0xFFffffff)), stroke = null, strokeLineWidth = 0.0f,
                        strokeLineCap = Butt, strokeLineJoin = Miter, strokeLineMiter = 4.0f,
                        pathFillType = NonZero) {
                    moveTo(10.1915f, 4.5329f)
                    curveTo(9.9324f, 4.2157f, 9.9275f, 3.7615f, 10.2303f, 3.4798f)
                    curveTo(10.5331f, 3.1981f, 11.0278f, 3.196f, 11.2967f, 3.506f)
                    curveTo(12.2985f, 4.6611f, 12.8983f, 6.1304f, 12.8983f, 7.7301f)
                    curveTo(12.8983f, 9.3298f, 12.2985f, 10.7991f, 11.2967f, 11.9542f)
                    curveTo(11.0278f, 12.2642f, 10.5331f, 12.262f, 10.2303f, 11.9804f)
                    curveTo(9.9275f, 11.6987f, 9.9324f, 11.2445f, 10.1915f, 10.9273f)
                    curveTo(10.9169f, 10.0393f, 11.3475f, 8.9315f, 11.3475f, 7.7301f)
                    curveTo(11.3475f, 6.5287f, 10.9169f, 5.4209f, 10.1915f, 4.5329f)
                    close()
                }
            }
        }
        .build()
        return _mute!!
    }

private var _mute: ImageVector? = null
