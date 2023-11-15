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
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp
import net.primal.android.core.compose.icons.PrimalIcons

public val PrimalIcons.Discuss: ImageVector
    get() {
        if (_discuss != null) {
            return _discuss!!
        }
        _discuss = Builder(name = "Discuss", defaultWidth = 24.0.dp, defaultHeight = 24.0.dp,
                viewportWidth = 24.0f, viewportHeight = 24.0f).apply {
            path(fill = SolidColor(Color(0xFFAAAAAA)), stroke = null, strokeLineWidth = 0.0f,
                    strokeLineCap = Butt, strokeLineJoin = Miter, strokeLineMiter = 4.0f,
                    pathFillType = NonZero) {
                moveTo(19.1161f, 15.1162f)
                curveTo(18.0876f, 15.5872f, 16.9437f, 15.8872f, 15.7322f, 15.9739f)
                curveTo(15.9065f, 15.35f, 15.9999f, 14.6901f, 15.9999f, 14.0f)
                lineTo(15.9997f, 13.9409f)
                curveTo(19.559f, 13.5141f, 21.9999f, 10.84f, 21.9999f, 8.0f)
                curveTo(21.9999f, 4.9012f, 19.0939f, 2.0f, 14.9999f, 2.0f)
                curveTo(11.7676f, 2.0f, 9.2758f, 3.8085f, 8.3701f, 6.0949f)
                curveTo(7.9193f, 6.0322f, 7.4613f, 6.0f, 6.9999f, 6.0f)
                curveTo(6.7575f, 6.0f, 6.5159f, 6.0089f, 6.2759f, 6.0265f)
                curveTo(7.2646f, 2.5626f, 10.7956f, 0.0f, 14.9999f, 0.0f)
                curveTo(19.9705f, 0.0f, 23.9999f, 3.5817f, 23.9999f, 8.0f)
                verticalLineTo(20.0f)
                lineTo(19.1161f, 15.1162f)
                close()
            }
            path(fill = SolidColor(Color(0xFFAAAAAA)), stroke = null, strokeLineWidth = 0.0f,
                    strokeLineCap = Butt, strokeLineJoin = Miter, strokeLineMiter = 4.0f,
                    pathFillType = EvenOdd) {
                moveTo(7.0f, 20.0f)
                curveTo(10.866f, 20.0f, 14.0f, 17.3137f, 14.0f, 14.0f)
                curveTo(14.0f, 10.6863f, 10.866f, 8.0f, 7.0f, 8.0f)
                curveTo(3.134f, 8.0f, 0.0f, 10.6863f, 0.0f, 14.0f)
                verticalLineTo(24.0f)
                lineTo(4.4205f, 19.5795f)
                curveTo(5.2188f, 19.8509f, 6.089f, 20.0f, 7.0f, 20.0f)
                close()
                moveTo(7.0f, 18.0f)
                curveTo(10.0618f, 18.0f, 12.0f, 15.931f, 12.0f, 14.0f)
                curveTo(12.0f, 12.069f, 10.0618f, 10.0f, 7.0f, 10.0f)
                curveTo(3.9382f, 10.0f, 2.0f, 12.069f, 2.0f, 14.0f)
                curveTo(2.0f, 15.931f, 3.9382f, 18.0f, 7.0f, 18.0f)
                close()
            }
        }
        .build()
        return _discuss!!
    }

private var _discuss: ImageVector? = null
