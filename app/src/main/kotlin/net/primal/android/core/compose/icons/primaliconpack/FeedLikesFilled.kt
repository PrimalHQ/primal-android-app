@file:Suppress("MagicNumber")

package net.primal.android.core.compose.icons.primaliconpack

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType.Companion.EvenOdd
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap.Companion.Butt
import androidx.compose.ui.graphics.StrokeJoin.Companion.Miter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.ImageVector.Builder
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp
import net.primal.android.core.compose.icons.PrimalIcons

public val PrimalIcons.FeedLikesFilled: ImageVector
    get() {
        if (_feedLikesFilled != null) {
            return _feedLikesFilled!!
        }
        _feedLikesFilled = Builder(name = "Feedlikesfilled", defaultWidth = 18.0.dp, defaultHeight =
                18.0.dp, viewportWidth = 18.0f, viewportHeight = 18.0f).apply {
            path(fill = SolidColor(Color(0xFFBC1870)), stroke = null, strokeLineWidth = 0.0f,
                    strokeLineCap = Butt, strokeLineJoin = Miter, strokeLineMiter = 4.0f,
                    pathFillType = EvenOdd) {
                moveTo(3.6419f, 10.3968f)
                lineTo(3.641f, 10.3959f)
                curveTo(3.6299f, 10.3859f, 3.6146f, 10.3721f, 3.5956f, 10.3545f)
                curveTo(3.5574f, 10.3193f, 3.504f, 10.269f, 3.4384f, 10.2049f)
                curveTo(3.3075f, 10.0769f, 3.1274f, 9.8926f, 2.9241f, 9.6622f)
                curveTo(2.5208f, 9.2051f, 2.0103f, 8.5482f, 1.6173f, 7.7729f)
                curveTo(1.2261f, 7.0012f, 0.9265f, 6.0629f, 1.0159f, 5.0643f)
                curveTo(1.1075f, 4.0398f, 1.6014f, 3.0365f, 2.642f, 2.1568f)
                curveTo(3.6654f, 1.2917f, 4.6938f, 0.9444f, 5.68f, 1.0072f)
                curveTo(6.6448f, 1.0686f, 7.4544f, 1.5155f, 8.076f, 2.0203f)
                curveTo(8.5925f, 2.4399f, 9.0117f, 2.9253f, 9.3227f, 3.3421f)
                curveTo(9.6337f, 2.9253f, 10.0529f, 2.4399f, 10.5694f, 2.0203f)
                curveTo(11.1909f, 1.5155f, 12.0005f, 1.0686f, 12.9653f, 1.0072f)
                curveTo(13.9515f, 0.9444f, 14.98f, 1.2917f, 16.0034f, 2.1568f)
                curveTo(17.0039f, 3.0025f, 17.5602f, 3.8983f, 17.7734f, 4.7994f)
                curveTo(17.9858f, 5.6971f, 17.8394f, 6.5226f, 17.5721f, 7.2006f)
                curveTo(17.3069f, 7.8733f, 16.9157f, 8.4204f, 16.601f, 8.7928f)
                curveTo(16.4419f, 8.9811f, 16.298f, 9.13f, 16.1918f, 9.2336f)
                curveTo(16.1385f, 9.2856f, 16.0944f, 9.3265f, 16.0622f, 9.3556f)
                curveTo(16.0495f, 9.3671f, 16.0386f, 9.3768f, 16.0297f, 9.3846f)
                lineTo(9.6739f, 15.6537f)
                curveTo(9.4791f, 15.8457f, 9.1663f, 15.8457f, 8.9716f, 15.6537f)
                lineTo(3.6419f, 10.3968f)
                close()
            }
        }
        .build()
        return _feedLikesFilled!!
    }

private var _feedLikesFilled: ImageVector? = null
