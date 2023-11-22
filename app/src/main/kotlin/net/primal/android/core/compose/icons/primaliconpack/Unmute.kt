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

public val PrimalIcons.Unmute: ImageVector
    get() {
        if (_unmute != null) {
            return _unmute!!
        }
        _unmute = Builder(name = "Unmute", defaultWidth = 16.0.dp, defaultHeight = 16.0.dp,
                viewportWidth = 16.0f, viewportHeight = 16.0f).apply {
            group {
                path(fill = SolidColor(Color(0xFFffffff)), stroke = null, strokeLineWidth = 0.0f,
                        strokeLineCap = Butt, strokeLineJoin = Miter, strokeLineMiter = 4.0f,
                        pathFillType = NonZero) {
                    moveTo(1.5303f, 0.7197f)
                    curveTo(1.2374f, 0.4268f, 0.7626f, 0.4268f, 0.4697f, 0.7197f)
                    curveTo(0.1768f, 1.0126f, 0.1768f, 1.4874f, 0.4697f, 1.7803f)
                    lineTo(14.4697f, 15.7803f)
                    curveTo(14.7626f, 16.0732f, 15.2374f, 16.0732f, 15.5303f, 15.7803f)
                    curveTo(15.8232f, 15.4874f, 15.8232f, 15.0126f, 15.5303f, 14.7197f)
                    lineTo(1.5303f, 0.7197f)
                    close()
                }
                path(fill = SolidColor(Color(0xFFffffff)), stroke = null, strokeLineWidth = 0.0f,
                        strokeLineCap = Butt, strokeLineJoin = Miter, strokeLineMiter = 4.0f,
                        pathFillType = NonZero) {
                    moveTo(5.1269f, 2.5483f)
                    lineTo(8.0f, 5.4214f)
                    verticalLineTo(0.77f)
                    curveTo(8.0f, 0.5604f, 7.7575f, 0.4438f, 7.5938f, 0.5748f)
                    lineTo(5.1269f, 2.5483f)
                    close()
                }
                path(fill = SolidColor(Color(0xFFffffff)), stroke = null, strokeLineWidth = 0.0f,
                        strokeLineCap = Butt, strokeLineJoin = Miter, strokeLineMiter = 4.0f,
                        pathFillType = NonZero) {
                    moveTo(14.8108f, 12.2322f)
                    lineTo(13.6793f, 11.1007f)
                    curveTo(14.174f, 10.0782f, 14.4492f, 8.9439f, 14.4492f, 7.7498f)
                    curveTo(14.4492f, 5.7518f, 13.6786f, 3.9213f, 12.3988f, 2.5012f)
                    curveTo(12.1237f, 2.196f, 12.1207f, 1.7411f, 12.4235f, 1.4595f)
                    curveTo(12.7263f, 1.1778f, 13.2198f, 1.1761f, 13.4992f, 1.478f)
                    curveTo(15.0578f, 3.1619f, 16.0f, 5.3535f, 16.0f, 7.7498f)
                    curveTo(16.0f, 9.3685f, 15.5701f, 10.8937f, 14.8108f, 12.2322f)
                    close()
                }
                path(fill = SolidColor(Color(0xFFffffff)), stroke = null, strokeLineWidth = 0.0f,
                        strokeLineCap = Butt, strokeLineJoin = Miter, strokeLineMiter = 4.0f,
                        pathFillType = NonZero) {
                    moveTo(11.2596f, 8.681f)
                    lineTo(12.5099f, 9.9313f)
                    curveTo(12.7617f, 9.2471f, 12.8983f, 8.5132f, 12.8983f, 7.7496f)
                    curveTo(12.8983f, 6.15f, 12.2985f, 4.6806f, 11.2967f, 3.5255f)
                    curveTo(11.0278f, 3.2155f, 10.5331f, 3.2177f, 10.2303f, 3.4994f)
                    curveTo(9.9275f, 3.781f, 9.9324f, 4.2352f, 10.1915f, 4.5524f)
                    curveTo(10.9169f, 5.4404f, 11.3475f, 6.5483f, 11.3475f, 7.7496f)
                    curveTo(11.3475f, 8.0674f, 11.3174f, 8.3787f, 11.2596f, 8.681f)
                    close()
                }
                path(fill = SolidColor(Color(0xFFffffff)), stroke = null, strokeLineWidth = 0.0f,
                        strokeLineCap = Butt, strokeLineJoin = Miter, strokeLineMiter = 4.0f,
                        pathFillType = NonZero) {
                    moveTo(1.0f, 4.2498f)
                    horizontalLineTo(1.1716f)
                    lineTo(8.0f, 11.0783f)
                    verticalLineTo(14.7297f)
                    curveTo(8.0f, 14.9393f, 7.7575f, 15.0559f, 7.5938f, 14.9249f)
                    lineTo(3.0f, 11.2498f)
                    lineTo(1.0f, 11.2498f)
                    curveTo(0.4477f, 11.2498f, 0.0f, 10.8021f, 0.0f, 10.2498f)
                    verticalLineTo(5.2498f)
                    curveTo(0.0f, 4.6976f, 0.4477f, 4.2498f, 1.0f, 4.2498f)
                    close()
                }
            }
        }
        .build()
        return _unmute!!
    }

private var _unmute: ImageVector? = null
