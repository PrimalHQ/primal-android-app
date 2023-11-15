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

public val PrimalIcons.Repost: ImageVector
    get() {
        if (_repost != null) {
            return _repost!!
        }
        _repost = Builder(name = "Repost", defaultWidth = 24.0.dp, defaultHeight = 24.0.dp,
                viewportWidth = 24.0f, viewportHeight = 24.0f).apply {
            group {
                path(fill = SolidColor(Color(0xFFffffff)), stroke = null, strokeLineWidth = 0.0f,
                        strokeLineCap = Butt, strokeLineJoin = Miter, strokeLineMiter = 4.0f,
                        pathFillType = NonZero) {
                    moveTo(8.346f, 7.7494f)
                    curveTo(8.717f, 8.0474f, 9.2667f, 7.7821f, 9.2667f, 7.3051f)
                    verticalLineTo(5.0754f)
                    horizontalLineTo(18.3333f)
                    curveTo(20.2111f, 5.0754f, 21.7333f, 6.6036f, 21.7333f, 8.4888f)
                    verticalLineTo(12.7612f)
                    curveTo(21.7333f, 13.1485f, 21.6691f, 13.5207f, 21.5507f, 13.8677f)
                    curveTo(21.464f, 14.1219f, 21.5225f, 14.4123f, 21.7315f, 14.5802f)
                    lineTo(22.6653f, 15.3302f)
                    curveTo(22.9314f, 15.544f, 23.3263f, 15.4757f, 23.4705f, 15.1656f)
                    curveTo(23.8103f, 14.4353f, 24.0f, 13.6205f, 24.0f, 12.7612f)
                    verticalLineTo(8.4888f)
                    curveTo(24.0f, 5.3468f, 21.463f, 2.7997f, 18.3333f, 2.7997f)
                    horizontalLineTo(9.2667f)
                    verticalLineTo(0.57f)
                    curveTo(9.2667f, 0.0929f, 8.717f, -0.1723f, 8.346f, 0.1257f)
                    lineTo(4.1531f, 3.4933f)
                    curveTo(3.8696f, 3.7211f, 3.8696f, 4.154f, 4.1531f, 4.3818f)
                    lineTo(8.346f, 7.7494f)
                    close()
                }
                path(fill = SolidColor(Color(0xFFffffff)), stroke = null, strokeLineWidth = 0.0f,
                        strokeLineCap = Butt, strokeLineJoin = Miter, strokeLineMiter = 4.0f,
                        pathFillType = NonZero) {
                    moveTo(2.6681f, 7.3365f)
                    curveTo(2.4019f, 7.1227f, 2.0071f, 7.191f, 1.8628f, 7.501f)
                    curveTo(1.5231f, 8.2314f, 1.3333f, 9.0462f, 1.3333f, 9.9055f)
                    verticalLineTo(14.1779f)
                    curveTo(1.3333f, 17.3199f, 3.8704f, 19.8669f, 7.0f, 19.8669f)
                    horizontalLineTo(16.0667f)
                    verticalLineTo(22.0967f)
                    curveTo(16.0667f, 22.5737f, 16.6163f, 22.8389f, 16.9873f, 22.5409f)
                    lineTo(21.1802f, 19.1734f)
                    curveTo(21.4638f, 18.9456f, 21.4638f, 18.5126f, 21.1802f, 18.2849f)
                    lineTo(16.9873f, 14.9173f)
                    curveTo(16.6163f, 14.6193f, 16.0667f, 14.8845f, 16.0667f, 15.3615f)
                    verticalLineTo(17.5913f)
                    horizontalLineTo(7.0f)
                    curveTo(5.1222f, 17.5913f, 3.6f, 16.0631f, 3.6f, 14.1779f)
                    verticalLineTo(9.9055f)
                    curveTo(3.6f, 9.5182f, 3.6642f, 9.146f, 3.7826f, 8.799f)
                    curveTo(3.8693f, 8.5448f, 3.8109f, 8.2543f, 3.6019f, 8.0865f)
                    lineTo(2.6681f, 7.3365f)
                    close()
                }
            }
        }
        .build()
        return _repost!!
    }

private var _repost: ImageVector? = null
