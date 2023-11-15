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

public val PrimalIcons.Key: ImageVector
    get() {
        if (_key != null) {
            return _key!!
        }
        _key = Builder(name = "Key", defaultWidth = 18.0.dp, defaultHeight = 28.0.dp, viewportWidth
                = 18.0f, viewportHeight = 28.0f).apply {
            path(fill = SolidColor(Color(0xFFAAAAAA)), stroke = null, strokeLineWidth = 0.0f,
                    strokeLineCap = Butt, strokeLineJoin = Miter, strokeLineMiter = 4.0f,
                    pathFillType = EvenOdd) {
                moveTo(12.0885f, 12.7182f)
                curveTo(15.0907f, 11.6393f, 17.1599f, 9.2554f, 17.1102f, 6.5224f)
                curveTo(17.0433f, 2.8418f, 13.1587f, -0.0776f, 8.4344f, 0.0016f)
                curveTo(3.7101f, 0.0807f, -0.0661f, 3.1284f, 9.0E-4f, 6.809f)
                curveTo(0.0506f, 9.542f, 2.2052f, 11.8552f, 5.2448f, 12.8328f)
                lineTo(5.479f, 25.7145f)
                curveTo(5.4859f, 26.0928f, 5.6246f, 26.4579f, 5.8731f, 26.7491f)
                lineTo(6.6809f, 27.6983f)
                curveTo(6.9874f, 28.0577f, 7.5364f, 28.1026f, 7.8994f, 27.7975f)
                lineTo(11.907f, 24.4286f)
                curveTo(11.9992f, 24.3513f, 12.0483f, 24.246f, 12.0547f, 24.1384f)
                curveTo(12.0624f, 24.0093f, 12.009f, 23.8776f, 11.8954f, 23.7883f)
                lineTo(9.6913f, 22.0623f)
                lineTo(11.8313f, 20.2635f)
                curveTo(11.935f, 20.1763f, 11.9846f, 20.0534f, 11.979f, 19.9322f)
                curveTo(11.9744f, 19.8168f, 11.9214f, 19.7029f, 11.8197f, 19.6232f)
                lineTo(9.6155f, 17.8972f)
                lineTo(11.9926f, 15.8991f)
                curveTo(12.0889f, 15.8181f, 12.1428f, 15.7001f, 12.1405f, 15.5764f)
                lineTo(12.0885f, 12.7182f)
                close()
                moveTo(11.0917f, 4.9567f)
                curveTo(11.1168f, 6.3368f, 9.9884f, 7.475f, 8.5707f, 7.4988f)
                curveTo(7.153f, 7.5225f, 5.984f, 6.4228f, 5.9589f, 5.0427f)
                curveTo(5.9338f, 3.6626f, 7.0621f, 2.5244f, 8.4798f, 2.5006f)
                curveTo(9.8975f, 2.4769f, 11.0666f, 3.5766f, 11.0917f, 4.9567f)
                close()
            }
        }
        .build()
        return _key!!
    }

private var _key: ImageVector? = null
