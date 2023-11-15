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

public val PrimalIcons.FeedRepliesFilled: ImageVector
    get() {
        if (_feedrepliesfilled != null) {
            return _feedrepliesfilled!!
        }
        _feedrepliesfilled = Builder(name = "Feedrepliesfilled", defaultWidth = 18.0.dp,
                defaultHeight = 18.0.dp, viewportWidth = 18.0f, viewportHeight = 18.0f).apply {
            group {
                path(fill = SolidColor(Color(0xFFCCCCCC)), stroke = null, strokeLineWidth = 0.0f,
                        strokeLineCap = Butt, strokeLineJoin = Miter, strokeLineMiter = 4.0f,
                        pathFillType = NonZero) {
                    moveTo(9.0f, 14.2373f)
                    curveTo(13.9706f, 14.2373f, 18.0f, 11.1761f, 18.0f, 7.3999f)
                    curveTo(18.0f, 3.6237f, 13.9706f, 0.5625f, 9.0f, 0.5625f)
                    curveTo(4.0294f, 0.5625f, 0.0f, 3.6237f, 0.0f, 7.3999f)
                    curveTo(0.0f, 9.1652f, 0.8806f, 10.7743f, 2.3262f, 11.9874f)
                    lineTo(1.9016f, 16.8267f)
                    curveTo(1.8605f, 17.2951f, 2.3781f, 17.6042f, 2.7709f, 17.3459f)
                    lineTo(7.6206f, 14.1575f)
                    curveTo(8.0703f, 14.2101f, 8.531f, 14.2373f, 9.0f, 14.2373f)
                    close()
                }
            }
        }
        .build()
        return _feedrepliesfilled!!
    }

private var _feedrepliesfilled: ImageVector? = null
