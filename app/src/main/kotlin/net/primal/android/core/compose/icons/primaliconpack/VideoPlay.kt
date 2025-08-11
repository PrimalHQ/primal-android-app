package net.primal.android.core.compose.icons.primaliconpack

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val VideoPlay: ImageVector
    get() {
        if (_VideoPlay != null) {
            return _VideoPlay!!
        }
        _VideoPlay = ImageVector.Builder(
            name = "VideoPlay",
            defaultWidth = 48.dp,
            defaultHeight = 48.dp,
            viewportWidth = 48f,
            viewportHeight = 48f
        ).apply {
            path(
                fill = SolidColor(Color.Black),
                fillAlpha = 0.25f
            ) {
                moveTo(24f, 24f)
                moveToRelative(-24f, 0f)
                arcToRelative(24f, 24f, 0f, isMoreThanHalf = true, isPositiveArc = true, 48f, 0f)
                arcToRelative(24f, 24f, 0f, isMoreThanHalf = true, isPositiveArc = true, -48f, 0f)
            }
            path(fill = SolidColor(Color.White)) {
                moveTo(35.285f, 25.216f)
                curveTo(36.237f, 24.683f, 36.239f, 23.348f, 35.287f, 22.813f)
                lineTo(18.16f, 13.189f)
                curveTo(17.208f, 12.654f, 16.017f, 13.32f, 16.017f, 14.388f)
                lineTo(16f, 33.61f)
                curveTo(15.999f, 34.677f, 17.188f, 35.346f, 18.141f, 34.813f)
                lineTo(35.285f, 25.216f)
                close()
            }
        }.build()

        return _VideoPlay!!
    }

@Suppress("ObjectPropertyName")
private var _VideoPlay: ImageVector? = null
