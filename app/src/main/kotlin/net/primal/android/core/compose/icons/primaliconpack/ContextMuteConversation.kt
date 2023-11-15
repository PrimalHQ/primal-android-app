@file:Suppress("MagicNumber")

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

public val PrimalIcons.ContextMuteConversation: ImageVector
    get() {
        if (_contextMuteConversation != null) {
            return _contextMuteConversation!!
        }
        _contextMuteConversation = Builder(name = "ContextMuteConversation", defaultWidth = 20.0.dp,
                defaultHeight = 20.0.dp, viewportWidth = 20.0f, viewportHeight = 20.0f).apply {
            group {
                path(fill = SolidColor(Color(0xFFffffff)), stroke = null, strokeLineWidth = 0.0f,
                        strokeLineCap = Butt, strokeLineJoin = Miter, strokeLineMiter = 4.0f,
                        pathFillType = NonZero) {
                    moveTo(19.749f, 0.5288f)
                    curveTo(20.0837f, 0.8588f, 20.0837f, 1.3938f, 19.749f, 1.7239f)
                    lineTo(2.4632f, 18.752f)
                    curveTo(2.1285f, 19.0821f, 1.5858f, 19.0821f, 1.251f, 18.752f)
                    curveTo(0.9163f, 18.422f, 0.9163f, 17.887f, 1.251f, 17.5569f)
                    lineTo(18.5368f, 0.5288f)
                    curveTo(18.8715f, 0.1987f, 19.4142f, 0.1987f, 19.749f, 0.5288f)
                    close()
                }
                path(fill = SolidColor(Color(0xFFffffff)), stroke = null, strokeLineWidth = 0.0f,
                        strokeLineCap = Butt, strokeLineJoin = Miter, strokeLineMiter = 4.0f,
                        pathFillType = NonZero) {
                    moveTo(15.2353f, 1.0f)
                    curveTo(15.4838f, 1.0f, 15.7267f, 1.0281f, 15.9617f, 1.0817f)
                    lineTo(14.5231f, 2.5f)
                    horizontalLineTo(4.7647f)
                    curveTo(3.722f, 2.5f, 2.5f, 3.5694f, 2.5f, 5.3903f)
                    verticalLineTo(10.7993f)
                    curveTo(2.5f, 11.9458f, 2.9845f, 12.7944f, 3.6036f, 13.2657f)
                    lineTo(2.5254f, 14.3288f)
                    curveTo(1.5999f, 13.5289f, 1.0f, 12.2458f, 1.0f, 10.7993f)
                    verticalLineTo(5.3903f)
                    curveTo(1.0f, 2.9656f, 2.6855f, 1.0f, 4.7647f, 1.0f)
                    horizontalLineTo(15.2353f)
                    close()
                }
                path(fill = SolidColor(Color(0xFFffffff)), stroke = null, strokeLineWidth = 0.0f,
                        strokeLineCap = Butt, strokeLineJoin = Miter, strokeLineMiter = 4.0f,
                        pathFillType = NonZero) {
                    moveTo(18.9262f, 4.5209f)
                    lineTo(17.5f, 5.9271f)
                    verticalLineTo(10.7993f)
                    curveTo(17.5f, 12.6202f, 16.278f, 13.6896f, 15.2353f, 13.6896f)
                    horizontalLineTo(13.7353f)
                    verticalLineTo(16.6417f)
                    lineTo(11.2943f, 13.6896f)
                    horizontalLineTo(9.6266f)
                    lineTo(8.1052f, 15.1896f)
                    horizontalLineTo(10.5882f)
                    lineTo(14.4319f, 19.8382f)
                    curveTo(14.7284f, 20.1839f, 15.2353f, 19.939f, 15.2353f, 19.4501f)
                    verticalLineTo(15.1896f)
                    curveTo(17.3145f, 15.1896f, 19.0f, 13.224f, 19.0f, 10.7993f)
                    verticalLineTo(5.3903f)
                    curveTo(19.0f, 5.0927f, 18.9746f, 4.802f, 18.9262f, 4.5209f)
                    close()
                }
            }
        }
        .build()
        return _contextMuteConversation!!
    }

private var _contextMuteConversation: ImageVector? = null
