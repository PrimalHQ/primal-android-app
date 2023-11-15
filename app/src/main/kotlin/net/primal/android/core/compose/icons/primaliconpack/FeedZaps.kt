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

public val PrimalIcons.FeedZaps: ImageVector
    get() {
        if (_feedZaps != null) {
            return _feedZaps!!
        }
        _feedZaps = Builder(name = "Feedzaps", defaultWidth = 18.0.dp, defaultHeight = 18.0.dp,
                viewportWidth = 18.0f, viewportHeight = 18.0f).apply {
            group {
                path(fill = SolidColor(Color(0xFF666666)), stroke = null, strokeLineWidth = 0.0f,
                        strokeLineCap = Butt, strokeLineJoin = Miter, strokeLineMiter = 4.0f,
                        pathFillType = EvenOdd) {
                    moveTo(17.5499f, 6.2078f)
                    curveTo(17.9154f, 6.2078f, 18.128f, 6.606f, 17.9155f, 6.8926f)
                    lineTo(9.9558f, 17.6339f)
                    curveTo(9.3989f, 18.3854f, 8.1714f, 17.8873f, 8.3399f, 16.9781f)
                    lineTo(9.4366f, 11.0585f)
                    horizontalLineTo(4.9502f)
                    curveTo(4.5787f, 11.0585f, 4.3676f, 10.6486f, 4.5923f, 10.3635f)
                    lineTo(12.4856f, 0.3459f)
                    curveTo(13.0615f, -0.3849f, 14.2663f, 0.1359f, 14.0831f, 1.0364f)
                    lineTo(13.0311f, 6.2078f)
                    horizontalLineTo(17.5499f)
                    close()
                    moveTo(11.1202f, 7.7236f)
                    lineTo(11.9809f, 3.4927f)
                    lineTo(7.2139f, 9.5427f)
                    horizontalLineTo(11.3149f)
                    lineTo(10.4135f, 14.4083f)
                    lineTo(15.3671f, 7.7236f)
                    horizontalLineTo(11.1202f)
                    close()
                    moveTo(1.8f, 4.3875f)
                    curveTo(1.8f, 3.9526f, 2.1525f, 3.6f, 2.5875f, 3.6f)
                    horizontalLineTo(5.9625f)
                    curveTo(6.3974f, 3.6f, 6.75f, 3.9526f, 6.75f, 4.3875f)
                    curveTo(6.75f, 4.8225f, 6.3974f, 5.175f, 5.9625f, 5.175f)
                    horizontalLineTo(2.5875f)
                    curveTo(2.1525f, 5.175f, 1.8f, 4.8225f, 1.8f, 4.3875f)
                    close()
                    moveTo(0.7875f, 8.1f)
                    curveTo(0.3526f, 8.1f, 0.0f, 8.4526f, 0.0f, 8.8875f)
                    curveTo(0.0f, 9.3225f, 0.3526f, 9.675f, 0.7875f, 9.675f)
                    horizontalLineTo(2.8125f)
                    curveTo(3.2475f, 9.675f, 3.6f, 9.3225f, 3.6f, 8.8875f)
                    curveTo(3.6f, 8.4526f, 3.2475f, 8.1f, 2.8125f, 8.1f)
                    horizontalLineTo(0.7875f)
                    close()
                    moveTo(0.9f, 13.3875f)
                    curveTo(0.9f, 12.9526f, 1.2526f, 12.6f, 1.6875f, 12.6f)
                    horizontalLineTo(5.5125f)
                    curveTo(5.9475f, 12.6f, 6.3f, 12.9526f, 6.3f, 13.3875f)
                    curveTo(6.3f, 13.8225f, 5.9475f, 14.175f, 5.5125f, 14.175f)
                    horizontalLineTo(1.6875f)
                    curveTo(1.2526f, 14.175f, 0.9f, 13.8225f, 0.9f, 13.3875f)
                    close()
                }
            }
        }
        .build()
        return _feedZaps!!
    }

private var _feedZaps: ImageVector? = null
