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

public val PrimalIcons.Subtract: ImageVector
    get() {
        if (_subtract != null) {
            return _subtract!!
        }
        _subtract = Builder(name = "Subtract", defaultWidth = 35.0.dp, defaultHeight = 26.0.dp,
                viewportWidth = 35.0f, viewportHeight = 26.0f).apply {
            path(fill = SolidColor(Color(0xFFF9F9F9)), stroke = null, strokeLineWidth = 0.0f,
                    strokeLineCap = Butt, strokeLineJoin = Miter, strokeLineMiter = 4.0f,
                    pathFillType = EvenOdd) {
                moveTo(14.5867f, 0.0f)
                curveTo(12.1978f, 0.0f, 9.934f, 1.0675f, 8.4143f, 2.9107f)
                lineTo(0.4569f, 12.5614f)
                curveTo(-0.1865f, 13.3418f, -0.1461f, 14.4796f, 0.551f, 15.2123f)
                lineTo(8.4493f, 23.5142f)
                curveTo(9.9594f, 25.1015f, 12.0544f, 26.0f, 14.2452f, 26.0f)
                horizontalLineTo(28.0016f)
                curveTo(31.8676f, 26.0f, 35.0017f, 22.866f, 35.0017f, 19.0f)
                verticalLineTo(7.0f)
                curveTo(35.0017f, 3.134f, 31.8676f, 0.0f, 28.0016f, 0.0f)
                horizontalLineTo(14.5867f)
                close()
                moveTo(20.0343f, 12.9929f)
                lineTo(15.0574f, 8.059f)
                lineTo(16.8661f, 6.2684f)
                lineTo(21.8503f, 11.1952f)
                lineTo(26.8344f, 6.2684f)
                lineTo(28.6432f, 8.059f)
                lineTo(23.6663f, 12.9929f)
                lineTo(28.6432f, 17.9268f)
                lineTo(26.8344f, 19.7173f)
                lineTo(21.8503f, 14.7906f)
                lineTo(16.8661f, 19.7173f)
                lineTo(15.0574f, 17.9268f)
                lineTo(20.0343f, 12.9929f)
                close()
            }
        }
        .build()
        return _subtract!!
    }

private var _subtract: ImageVector? = null
