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

public val PrimalIcons.Send: ImageVector
    get() {
        if (_send != null) {
            return _send!!
        }
        _send = Builder(name = "Send", defaultWidth = 20.0.dp, defaultHeight = 20.0.dp,
                viewportWidth = 20.0f, viewportHeight = 20.0f).apply {
            path(fill = SolidColor(Color(0xFFffffff)), stroke = null, strokeLineWidth = 0.0f,
                    strokeLineCap = Butt, strokeLineJoin = Miter, strokeLineMiter = 4.0f,
                    pathFillType = NonZero) {
                moveTo(19.5528f, 10.2236f)
                curveTo(19.737f, 10.1315f, 19.737f, 9.8685f, 19.5528f, 9.7764f)
                lineTo(0.5398f, 0.2699f)
                curveTo(0.3406f, 0.1703f, 0.1204f, 0.3613f, 0.1909f, 0.5726f)
                lineTo(2.7529f, 8.2586f)
                curveTo(2.8161f, 8.4484f, 2.9864f, 8.5822f, 3.1857f, 8.5988f)
                lineTo(9.1695f, 9.0975f)
                curveTo(9.6389f, 9.1366f, 10.0f, 9.529f, 10.0f, 10.0f)
                curveTo(10.0f, 10.471f, 9.6389f, 10.8634f, 9.1695f, 10.9025f)
                lineTo(3.1857f, 11.4012f)
                curveTo(2.9864f, 11.4178f, 2.8161f, 11.5516f, 2.7529f, 11.7414f)
                lineTo(0.1909f, 19.4274f)
                curveTo(0.1204f, 19.6387f, 0.3406f, 19.8297f, 0.5398f, 19.7301f)
                lineTo(19.5528f, 10.2236f)
                close()
            }
        }
        .build()
        return _send!!
    }

private var _send: ImageVector? = null
