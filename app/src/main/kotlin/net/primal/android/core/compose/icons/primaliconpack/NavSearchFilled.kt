@file:Suppress("MagicNumber")

package net.primal.android.core.compose.icons.primaliconpack

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType.Companion.EvenOdd
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

public val PrimalIcons.NavSearchFilled: ImageVector
    get() {
        if (_navsearchfilled != null) {
            return _navsearchfilled!!
        }
        _navsearchfilled = Builder(name = "Navsearchfilled", defaultWidth = 24.0.dp, defaultHeight =
                24.0.dp, viewportWidth = 24.0f, viewportHeight = 24.0f).apply {
            group {
                path(fill = SolidColor(Color(0xFFffffff)), stroke = null, strokeLineWidth = 0.0f,
                        strokeLineCap = Butt, strokeLineJoin = Miter, strokeLineMiter = 4.0f,
                        pathFillType = EvenOdd) {
                    moveTo(14.7554f, 17.4135f)
                    curveTo(13.2502f, 18.4152f, 11.443f, 18.9988f, 9.4994f, 18.9988f)
                    curveTo(4.253f, 18.9988f, 0.0f, 14.7458f, 0.0f, 9.4994f)
                    curveTo(0.0f, 4.253f, 4.253f, 0.0f, 9.4994f, 0.0f)
                    curveTo(14.7458f, 0.0f, 18.9988f, 4.253f, 18.9988f, 9.4994f)
                    curveTo(18.9988f, 11.443f, 18.4152f, 13.2502f, 17.4135f, 14.7554f)
                    lineTo(22.6695f, 20.0114f)
                    curveTo(23.4035f, 20.7454f, 23.4035f, 21.9355f, 22.6695f, 22.6695f)
                    curveTo(21.9355f, 23.4035f, 20.7454f, 23.4035f, 20.0114f, 22.6695f)
                    lineTo(14.7554f, 17.4135f)
                    close()
                    moveTo(16.999f, 9.4994f)
                    curveTo(16.999f, 13.6413f, 13.6413f, 16.999f, 9.4994f, 16.999f)
                    curveTo(5.3575f, 16.999f, 1.9999f, 13.6413f, 1.9999f, 9.4994f)
                    curveTo(1.9999f, 5.3575f, 5.3575f, 1.9999f, 9.4994f, 1.9999f)
                    curveTo(13.6413f, 1.9999f, 16.999f, 5.3575f, 16.999f, 9.4994f)
                    close()
                }
                path(fill = SolidColor(Color(0xFFffffff)), stroke = null, strokeLineWidth = 0.0f,
                        strokeLineCap = Butt, strokeLineJoin = Miter, strokeLineMiter = 4.0f,
                        pathFillType = NonZero) {
                    moveTo(15.839f, 9.4999f)
                    curveTo(15.839f, 13.0009f, 13.0009f, 15.839f, 9.4999f, 15.839f)
                    curveTo(5.999f, 15.839f, 3.1609f, 13.0009f, 3.1609f, 9.4999f)
                    curveTo(3.1609f, 5.999f, 5.999f, 3.1609f, 9.4999f, 3.1609f)
                    curveTo(13.0009f, 3.1609f, 15.839f, 5.999f, 15.839f, 9.4999f)
                    close()
                }
            }
        }
        .build()
        return _navsearchfilled!!
    }

private var _navsearchfilled: ImageVector? = null
