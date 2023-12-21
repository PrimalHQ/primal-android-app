package net.primal.android.core.compose.icons.primaliconpack

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType.Companion.NonZero
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap.Companion.Butt
import androidx.compose.ui.graphics.StrokeJoin.Companion.Miter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.ImageVector.Builder
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp
import net.primal.android.core.compose.icons.PrimalIcons

public val PrimalIcons.NavMessagesFilled: ImageVector
    get() {
        if (_navmessagesfilled != null) {
            return _navmessagesfilled!!
        }
        _navmessagesfilled = Builder(name = "Navmessagesfilled", defaultWidth = 24.0.dp,
                defaultHeight = 24.0.dp, viewportWidth = 24.0f, viewportHeight = 24.0f).apply {
            path(fill = SolidColor(Color(0xFFffffff)), stroke = null, strokeLineWidth = 0.0f,
                    strokeLineCap = Butt, strokeLineJoin = Miter, strokeLineMiter = 4.0f,
                    pathFillType = NonZero) {
                moveTo(0.3983f, 6.6935f)
                curveTo(0.2332f, 6.5719f, 0.0f, 6.6898f, 0.0f, 6.8948f)
                verticalLineTo(18.0f)
                curveTo(0.0f, 20.2091f, 1.7909f, 22.0f, 4.0f, 22.0f)
                horizontalLineTo(20.0f)
                curveTo(22.2091f, 22.0f, 24.0f, 20.2091f, 24.0f, 18.0f)
                verticalLineTo(6.8948f)
                curveTo(24.0f, 6.6898f, 23.7668f, 6.5719f, 23.6017f, 6.6935f)
                lineTo(13.1864f, 14.368f)
                curveTo(12.4809f, 14.8878f, 11.5191f, 14.8878f, 10.8136f, 14.368f)
                lineTo(0.3983f, 6.6935f)
                close()
            }
            path(fill = SolidColor(Color(0xFFffffff)), stroke = null, strokeLineWidth = 0.0f,
                    strokeLineCap = Butt, strokeLineJoin = Miter, strokeLineMiter = 4.0f,
                    pathFillType = NonZero) {
                moveTo(23.2462f, 4.4712f)
                curveTo(23.4416f, 4.3272f, 23.5082f, 4.0615f, 23.3779f, 3.8567f)
                curveTo(22.6682f, 2.7405f, 21.4206f, 2.0f, 20.0f, 2.0f)
                horizontalLineTo(4.0f)
                curveTo(2.5794f, 2.0f, 1.3318f, 2.7405f, 0.6221f, 3.8567f)
                curveTo(0.4918f, 4.0615f, 0.5584f, 4.3272f, 0.7538f, 4.4712f)
                lineTo(11.4068f, 12.3208f)
                curveTo(11.7596f, 12.5807f, 12.2404f, 12.5807f, 12.5932f, 12.3208f)
                lineTo(23.2462f, 4.4712f)
                close()
            }
        }
        .build()
        return _navmessagesfilled!!
    }

private var _navmessagesfilled: ImageVector? = null
