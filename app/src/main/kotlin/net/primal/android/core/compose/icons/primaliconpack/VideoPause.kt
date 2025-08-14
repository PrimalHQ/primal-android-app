package net.primal.android.core.compose.icons.primaliconpack

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp
import net.primal.android.core.compose.icons.PrimalIcons

val PrimalIcons.VideoPause: ImageVector
    get() {
        if (_VideoPause != null) {
            return _VideoPause!!
        }
        _VideoPause = ImageVector.Builder(
            name = "VideoPause",
            defaultWidth = 48.dp,
            defaultHeight = 48.dp,
            viewportWidth = 48f,
            viewportHeight = 48f,
        ).apply {
            path(
                fill = SolidColor(Color.Black),
                fillAlpha = 0.25f,
            ) {
                moveTo(24f, 24f)
                moveToRelative(-24f, 0f)
                arcToRelative(24f, 24f, 0f, isMoreThanHalf = true, isPositiveArc = true, 48f, 0f)
                arcToRelative(24f, 24f, 0f, isMoreThanHalf = true, isPositiveArc = true, -48f, 0f)
            }
            path(fill = SolidColor(Color.White)) {
                moveTo(17.5f, 12f)
                curveTo(18.328f, 12f, 19f, 12.672f, 19f, 13.5f)
                verticalLineTo(34.5f)
                curveTo(19f, 35.328f, 18.328f, 36f, 17.5f, 36f)
                curveTo(16.672f, 36f, 16f, 35.328f, 16f, 34.5f)
                verticalLineTo(13.5f)
                curveTo(16f, 12.672f, 16.672f, 12f, 17.5f, 12f)
                close()
            }
            path(fill = SolidColor(Color.White)) {
                moveTo(30.5f, 12f)
                curveTo(31.328f, 12f, 32f, 12.672f, 32f, 13.5f)
                verticalLineTo(34.5f)
                curveTo(32f, 35.328f, 31.328f, 36f, 30.5f, 36f)
                curveTo(29.672f, 36f, 29f, 35.328f, 29f, 34.5f)
                verticalLineTo(13.5f)
                curveTo(29f, 12.672f, 29.672f, 12f, 30.5f, 12f)
                close()
            }
        }.build()

        return _VideoPause!!
    }

@Suppress("ObjectPropertyName")
private var _VideoPause: ImageVector? = null
