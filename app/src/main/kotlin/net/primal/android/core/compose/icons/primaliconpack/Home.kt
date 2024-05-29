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

public val PrimalIcons.Home: ImageVector
    get() {
        if (_home != null) {
            return _home!!
        }
        _home = Builder(name = "Home", defaultWidth = 24.0.dp, defaultHeight = 24.0.dp,
                viewportWidth = 24.0f, viewportHeight = 24.0f).apply {
            path(fill = SolidColor(Color(0xFFAAAAAA)), stroke = null, strokeLineWidth = 0.0f,
                    strokeLineCap = Butt, strokeLineJoin = Miter, strokeLineMiter = 4.0f,
                    pathFillType = EvenOdd) {
                moveTo(11.5486f, 1.401f)
                curveTo(11.8159f, 1.1997f, 12.1841f, 1.1997f, 12.4513f, 1.401f)
                lineTo(23.4376f, 9.6784f)
                curveTo(23.7747f, 9.9323f, 23.8528f, 10.4278f, 23.604f, 10.7822f)
                curveTo(23.3589f, 11.1314f, 22.8938f, 11.2045f, 22.5622f, 10.9547f)
                lineTo(20.7929f, 9.6216f)
                lineTo(19.75f, 20.7362f)
                verticalLineTo(20.7479f)
                curveTo(19.75f, 21.7144f, 18.9665f, 22.4979f, 18.0f, 22.4979f)
                horizontalLineTo(6.0001f)
                curveTo(5.0336f, 22.4979f, 4.2501f, 21.7144f, 4.2501f, 20.7479f)
                verticalLineTo(20.7362f)
                lineTo(3.2071f, 9.6216f)
                lineTo(1.4378f, 10.9547f)
                curveTo(1.1062f, 11.2045f, 0.6411f, 11.1314f, 0.3959f, 10.7822f)
                curveTo(0.1471f, 10.4278f, 0.2253f, 9.9323f, 0.5623f, 9.6784f)
                lineTo(11.5486f, 1.401f)
                close()
                moveTo(12.7521f, 3.5635f)
                curveTo(12.3068f, 3.228f, 11.6931f, 3.228f, 11.2478f, 3.5635f)
                lineTo(4.7386f, 8.4677f)
                lineTo(5.7501f, 19.6879f)
                curveTo(5.7565f, 20.3728f, 6.3137f, 20.926f, 7.0001f, 20.926f)
                horizontalLineTo(17.0f)
                curveTo(17.6864f, 20.926f, 18.2435f, 20.3728f, 18.2499f, 19.6879f)
                lineTo(19.2614f, 8.4678f)
                lineTo(12.7521f, 3.5635f)
                close()
            }
        }
        .build()
        return _home!!
    }

private var _home: ImageVector? = null
