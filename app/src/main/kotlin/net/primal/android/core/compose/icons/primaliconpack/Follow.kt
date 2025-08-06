package net.primal.android.core.compose.icons.primaliconpack

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val Follow: ImageVector
    get() {
        if (_Follow != null) {
            return _Follow!!
        }
        _Follow = ImageVector.Builder(
            name = "Follow",
            defaultWidth = 28.dp,
            defaultHeight = 24.dp,
            viewportWidth = 28f,
            viewportHeight = 24f
        ).apply {
            path(
                fill = SolidColor(Color(0xFFAAAAAA)),
                pathFillType = PathFillType.EvenOdd
            ) {
                moveTo(19.5f, 6.5f)
                curveTo(19.5f, 9.538f, 17.038f, 12f, 14f, 12f)
                curveTo(10.962f, 12f, 8.5f, 9.538f, 8.5f, 6.5f)
                curveTo(8.5f, 3.462f, 10.962f, 1f, 14f, 1f)
                curveTo(17.038f, 1f, 19.5f, 3.462f, 19.5f, 6.5f)
                close()
            }
            path(
                fill = SolidColor(Color(0xFFAAAAAA)),
                pathFillType = PathFillType.EvenOdd
            ) {
                moveTo(4.1f, 18.6f)
                curveTo(4.1f, 16.17f, 6.07f, 14.2f, 8.5f, 14.2f)
                horizontalLineTo(19.5f)
                curveTo(21.93f, 14.2f, 23.9f, 16.17f, 23.9f, 18.6f)
                curveTo(23.9f, 21.03f, 21.93f, 23f, 19.5f, 23f)
                horizontalLineTo(8.5f)
                curveTo(6.07f, 23f, 4.1f, 21.03f, 4.1f, 18.6f)
                close()
            }
        }.build()

        return _Follow!!
    }

@Suppress("ObjectPropertyName")
private var _Follow: ImageVector? = null
