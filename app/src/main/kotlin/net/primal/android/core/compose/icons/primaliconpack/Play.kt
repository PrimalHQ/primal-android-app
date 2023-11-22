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

public val PrimalIcons.Play: ImageVector
    get() {
        if (_play != null) {
            return _play!!
        }
        _play = Builder(name = "Play", defaultWidth = 33.0.dp, defaultHeight = 36.0.dp,
                viewportWidth = 33.0f, viewportHeight = 36.0f).apply {
            group {
                path(fill = SolidColor(Color(0xFFffffff)), stroke = null, fillAlpha = 0.8f,
                        strokeLineWidth = 0.0f, strokeLineCap = Butt, strokeLineJoin = Miter,
                        strokeLineMiter = 4.0f, pathFillType = NonZero) {
                    moveTo(0.0f, 2.4687f)
                    verticalLineTo(33.5313f)
                    curveTo(0.0f, 35.411f, 2.0262f, 36.5991f, 3.6749f, 35.6862f)
                    lineTo(31.7277f, 20.1549f)
                    curveTo(33.4241f, 19.2157f, 33.4241f, 16.7843f, 31.7277f, 15.8451f)
                    lineTo(3.6749f, 0.3138f)
                    curveTo(2.0262f, -0.5991f, 0.0f, 0.589f, 0.0f, 2.4687f)
                    close()
                }
            }
        }
        .build()
        return _play!!
    }

private var _play: ImageVector? = null
