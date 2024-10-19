package net.primal.android.core.compose.icons.primaliconpack

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp
import net.primal.android.core.compose.icons.PrimalIcons

val PrimalIcons.NotificationsFilled: ImageVector
    get() {
        if (_NotificationsFilled != null) {
            return _NotificationsFilled!!
        }
        _NotificationsFilled = ImageVector.Builder(
            name = "NotificationsFilled",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(fill = SolidColor(Color(0xFFFFFFFF))) {
                moveTo(3.991f, 17.83f)
                curveTo(3.444f, 17.781f, 3.084f, 17.214f, 3.294f, 16.691f)
                lineTo(4.146f, 14.561f)
                curveTo(4.382f, 13.97f, 4.503f, 13.34f, 4.503f, 12.704f)
                verticalLineTo(10.333f)
                curveTo(4.503f, 5.731f, 7.401f, 2f, 12.003f, 2f)
                curveTo(16.606f, 2f, 19.503f, 5.731f, 19.503f, 10.333f)
                verticalLineTo(12.704f)
                curveTo(19.503f, 13.34f, 19.625f, 13.97f, 19.861f, 14.561f)
                lineTo(20.713f, 16.691f)
                curveTo(20.922f, 17.214f, 20.563f, 17.781f, 20.015f, 17.83f)
                horizontalLineTo(3.991f)
                close()
            }
            path(fill = SolidColor(Color(0xFFFFFFFF))) {
                moveTo(15.98f, 19.08f)
                curveTo(15.45f, 20.772f, 13.87f, 22f, 12.003f, 22f)
                curveTo(10.136f, 22f, 8.556f, 20.772f, 8.026f, 19.08f)
                horizontalLineTo(15.98f)
                close()
            }
        }.build()

        return _NotificationsFilled!!
    }

@Suppress("ObjectPropertyName")
private var _NotificationsFilled: ImageVector? = null
