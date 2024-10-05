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

public val PrimalIcons.LightningBoltFilled: ImageVector
    get() {
        if (_lightningBoltFilled != null) {
            return _lightningBoltFilled!!
        }
        _lightningBoltFilled = Builder(name = "LightningBoltFilled", defaultWidth = 10.0.dp,
                defaultHeight = 14.0.dp, viewportWidth = 10.0f, viewportHeight = 14.0f).apply {
            path(fill = SolidColor(Color(0xFFF5F5F5)), stroke = null, strokeLineWidth = 0.0f,
                    strokeLineCap = Butt, strokeLineJoin = Miter, strokeLineMiter = 4.0f,
                    pathFillType = EvenOdd) {
                moveTo(9.9374f, 5.3609f)
                curveTo(10.0948f, 5.138f, 9.9373f, 4.8283f, 9.6666f, 4.8283f)
                horizontalLineTo(6.3193f)
                lineTo(7.0986f, 0.8061f)
                curveTo(7.2343f, 0.1057f, 6.3418f, -0.2993f, 5.9153f, 0.269f)
                lineTo(0.0684f, 8.0605f)
                curveTo(-0.0981f, 8.2823f, 0.0583f, 8.6011f, 0.3335f, 8.6011f)
                horizontalLineTo(3.6567f)
                lineTo(2.8443f, 13.2052f)
                curveTo(2.7196f, 13.9123f, 3.6288f, 14.2997f, 4.0413f, 13.7152f)
                lineTo(9.9374f, 5.3609f)
                close()
            }
        }
        .build()
        return _lightningBoltFilled!!
    }

private var _lightningBoltFilled: ImageVector? = null
