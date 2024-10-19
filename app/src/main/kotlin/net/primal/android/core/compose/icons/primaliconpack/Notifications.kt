package net.primal.android.core.compose.icons.primaliconpack

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp
import net.primal.android.core.compose.icons.PrimalIcons

val PrimalIcons.Notifications: ImageVector
    get() {
        if (_Notifications != null) {
            return _Notifications!!
        }
        _Notifications = ImageVector.Builder(
            name = "Notifications",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(
                fill = SolidColor(Color(0xFFAAAAAA)),
                pathFillType = PathFillType.EvenOdd
            ) {
                moveTo(4.711f, 10.333f)
                curveTo(4.711f, 5.846f, 7.515f, 2.208f, 12.002f, 2.208f)
                curveTo(16.49f, 2.208f, 19.294f, 5.846f, 19.294f, 10.333f)
                verticalLineTo(12.704f)
                curveTo(19.294f, 13.366f, 19.421f, 14.023f, 19.667f, 14.638f)
                lineTo(20.519f, 16.768f)
                curveTo(20.683f, 17.178f, 20.38f, 17.625f, 19.938f, 17.625f)
                horizontalLineTo(15.961f)
                verticalLineTo(17.833f)
                curveTo(15.961f, 20.02f, 14.189f, 21.792f, 12.002f, 21.792f)
                curveTo(9.816f, 21.792f, 8.044f, 20.02f, 8.044f, 17.833f)
                verticalLineTo(17.625f)
                horizontalLineTo(4.067f)
                curveTo(3.624f, 17.625f, 3.322f, 17.178f, 3.486f, 16.768f)
                lineTo(4.338f, 14.638f)
                curveTo(4.584f, 14.023f, 4.711f, 13.366f, 4.711f, 12.704f)
                verticalLineTo(10.333f)
                close()
                moveTo(12.002f, 3.458f)
                curveTo(8.206f, 3.458f, 5.961f, 6.536f, 5.961f, 10.333f)
                verticalLineTo(12.704f)
                curveTo(5.961f, 13.525f, 5.804f, 14.34f, 5.499f, 15.102f)
                lineTo(4.99f, 16.375f)
                horizontalLineTo(19.015f)
                lineTo(18.506f, 15.102f)
                curveTo(18.201f, 14.34f, 18.044f, 13.525f, 18.044f, 12.704f)
                verticalLineTo(10.333f)
                curveTo(18.044f, 6.536f, 15.799f, 3.458f, 12.002f, 3.458f)
                close()
                moveTo(14.711f, 17.833f)
                verticalLineTo(17.625f)
                horizontalLineTo(9.294f)
                verticalLineTo(17.833f)
                curveTo(9.294f, 19.329f, 10.507f, 20.542f, 12.002f, 20.542f)
                curveTo(13.498f, 20.542f, 14.711f, 19.329f, 14.711f, 17.833f)
                close()
            }
        }.build()

        return _Notifications!!
    }

@Suppress("ObjectPropertyName")
private var _Notifications: ImageVector? = null
