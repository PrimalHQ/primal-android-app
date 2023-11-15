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

public val PrimalIcons.Edit: ImageVector
    get() {
        if (_edit != null) {
            return _edit!!
        }
        _edit = Builder(name = "Edit", defaultWidth = 24.0.dp, defaultHeight = 24.0.dp,
                viewportWidth = 24.0f, viewportHeight = 24.0f).apply {
            path(fill = SolidColor(Color(0xFFAAAAAA)), stroke = null, strokeLineWidth = 0.0f,
                    strokeLineCap = Butt, strokeLineJoin = Miter, strokeLineMiter = 4.0f,
                    pathFillType = EvenOdd) {
                moveTo(22.8099f, 1.1835f)
                curveTo(21.2232f, -0.3945f, 18.6506f, -0.3945f, 17.0638f, 1.1835f)
                lineTo(7.1039f, 11.089f)
                curveTo(6.4563f, 11.733f, 5.9959f, 12.5391f, 5.7714f, 13.4221f)
                lineTo(4.6913f, 17.6691f)
                curveTo(4.4555f, 18.5963f, 5.3013f, 19.4375f, 6.2336f, 19.203f)
                lineTo(10.504f, 18.1288f)
                curveTo(11.3918f, 17.9055f, 12.2024f, 17.4476f, 12.85f, 16.8036f)
                lineTo(22.8099f, 6.8982f)
                curveTo(24.3967f, 5.3201f, 24.3967f, 2.7616f, 22.8099f, 1.1835f)
                close()
                moveTo(18.5004f, 2.6122f)
                curveTo(19.2937f, 1.8232f, 20.58f, 1.8232f, 21.3734f, 2.6122f)
                curveTo(22.1668f, 3.4012f, 22.1668f, 4.6805f, 21.3734f, 5.4695f)
                lineTo(11.4135f, 15.3749f)
                curveTo(11.0249f, 15.7613f, 10.5386f, 16.0361f, 10.0059f, 16.1701f)
                lineTo(6.9741f, 16.9327f)
                lineTo(7.7409f, 13.9175f)
                curveTo(7.8756f, 13.3877f, 8.1519f, 12.904f, 8.5404f, 12.5176f)
                lineTo(18.5004f, 2.6122f)
                close()
            }
            path(fill = SolidColor(Color(0xFFAAAAAA)), stroke = null, strokeLineWidth = 0.0f,
                    strokeLineCap = Butt, strokeLineJoin = Miter, strokeLineMiter = 4.0f,
                    pathFillType = NonZero) {
                moveTo(19.2998f, 13.8112f)
                curveTo(19.2998f, 13.5433f, 19.4068f, 13.2863f, 19.5973f, 13.0969f)
                lineTo(20.9845f, 11.7172f)
                curveTo(21.1125f, 11.5899f, 21.3313f, 11.6801f, 21.3313f, 11.8601f)
                verticalLineTo(21.9796f)
                curveTo(21.3313f, 23.0954f, 20.4218f, 24.0f, 19.2998f, 24.0f)
                horizontalLineTo(2.0315f)
                curveTo(0.9096f, 24.0f, 0.0f, 23.0954f, 0.0f, 21.9796f)
                verticalLineTo(4.8059f)
                curveTo(0.0f, 3.6901f, 0.9096f, 2.7855f, 2.0315f, 2.7855f)
                horizontalLineTo(12.2068f)
                curveTo(12.3877f, 2.7855f, 12.4784f, 3.0031f, 12.3504f, 3.1304f)
                lineTo(10.9632f, 4.51f)
                curveTo(10.7727f, 4.6995f, 10.5143f, 4.8059f, 10.2449f, 4.8059f)
                horizontalLineTo(3.0473f)
                curveTo(2.4863f, 4.8059f, 2.0315f, 5.2582f, 2.0315f, 5.8161f)
                verticalLineTo(20.9694f)
                curveTo(2.0315f, 21.5273f, 2.4863f, 21.9796f, 3.0473f, 21.9796f)
                horizontalLineTo(18.284f)
                curveTo(18.845f, 21.9796f, 19.2998f, 21.5273f, 19.2998f, 20.9694f)
                verticalLineTo(13.8112f)
                close()
            }
        }
        .build()
        return _edit!!
    }

private var _edit: ImageVector? = null
