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

public val PrimalIcons.FeedReplies: ImageVector
    get() {
        if (_feedreplies != null) {
            return _feedreplies!!
        }
        _feedreplies = Builder(name = "Feedreplies", defaultWidth = 18.0.dp, defaultHeight =
                18.0.dp, viewportWidth = 18.0f, viewportHeight = 18.0f).apply {
            group {
                path(fill = SolidColor(Color(0xFF666666)), stroke = null, strokeLineWidth = 0.0f,
                        strokeLineCap = Butt, strokeLineJoin = Miter, strokeLineMiter = 4.0f,
                        pathFillType = EvenOdd) {
                    moveTo(4.0841f, 11.2595f)
                    lineTo(3.7858f, 14.6592f)
                    lineTo(7.2066f, 12.4102f)
                    lineTo(7.8165f, 12.4814f)
                    curveTo(8.201f, 12.5264f, 8.5963f, 12.5498f, 9.0f, 12.5498f)
                    curveTo(11.1503f, 12.5498f, 13.0338f, 11.8857f, 14.3431f, 10.891f)
                    curveTo(15.6512f, 9.8972f, 16.3125f, 8.6471f, 16.3125f, 7.3999f)
                    curveTo(16.3125f, 6.1527f, 15.6512f, 4.9026f, 14.3431f, 3.9088f)
                    curveTo(13.0338f, 2.9141f, 11.1503f, 2.25f, 9.0f, 2.25f)
                    curveTo(6.8496f, 2.25f, 4.9662f, 2.9141f, 3.6569f, 3.9088f)
                    curveTo(2.3488f, 4.9026f, 1.6875f, 6.1527f, 1.6875f, 7.3999f)
                    curveTo(1.6875f, 8.5686f, 2.2655f, 9.7335f, 3.411f, 10.6947f)
                    lineTo(4.0841f, 11.2595f)
                    close()
                    moveTo(2.7709f, 17.3459f)
                    curveTo(2.3781f, 17.6042f, 1.8605f, 17.2951f, 1.9016f, 16.8267f)
                    lineTo(2.3262f, 11.9874f)
                    curveTo(0.8806f, 10.7743f, 0.0f, 9.1652f, 0.0f, 7.3999f)
                    curveTo(0.0f, 3.6237f, 4.0294f, 0.5625f, 9.0f, 0.5625f)
                    curveTo(13.9706f, 0.5625f, 18.0f, 3.6237f, 18.0f, 7.3999f)
                    curveTo(18.0f, 11.1761f, 13.9706f, 14.2373f, 9.0f, 14.2373f)
                    curveTo(8.531f, 14.2373f, 8.0703f, 14.2101f, 7.6206f, 14.1575f)
                    lineTo(2.7709f, 17.3459f)
                    close()
                }
            }
        }
        .build()
        return _feedreplies!!
    }

private var _feedreplies: ImageVector? = null
