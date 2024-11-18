package net.primal.android.core.compose.icons.primaliconpack

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.group
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp
import net.primal.android.core.compose.icons.PrimalIcons

val PrimalIcons.CopyAlt: ImageVector
    get() {
        if (_CopyAlt != null) {
            return _CopyAlt!!
        }
        _CopyAlt = ImageVector.Builder(
            name = "CopyAlt",
            defaultWidth = 18.dp,
            defaultHeight = 18.dp,
            viewportWidth = 18f,
            viewportHeight = 18f
        ).apply {
            group {
                path(fill = SolidColor(Color(0xFFFFFFFF))) {
                    moveTo(1.8f, 1.35f)
                    horizontalLineTo(11.7f)
                    curveTo(11.948f, 1.35f, 12.15f, 1.551f, 12.15f, 1.8f)
                    verticalLineTo(2.025f)
                    curveTo(12.15f, 2.398f, 12.452f, 2.7f, 12.825f, 2.7f)
                    curveTo(13.198f, 2.7f, 13.5f, 2.398f, 13.5f, 2.025f)
                    verticalLineTo(1.8f)
                    curveTo(13.5f, 0.806f, 12.694f, 0f, 11.7f, 0f)
                    horizontalLineTo(1.8f)
                    curveTo(0.806f, 0f, 0f, 0.806f, 0f, 1.8f)
                    verticalLineTo(11.7f)
                    curveTo(0f, 12.694f, 0.806f, 13.5f, 1.8f, 13.5f)
                    horizontalLineTo(2.025f)
                    curveTo(2.398f, 13.5f, 2.7f, 13.198f, 2.7f, 12.825f)
                    curveTo(2.7f, 12.452f, 2.398f, 12.15f, 2.025f, 12.15f)
                    horizontalLineTo(1.8f)
                    curveTo(1.551f, 12.15f, 1.35f, 11.948f, 1.35f, 11.7f)
                    verticalLineTo(1.8f)
                    curveTo(1.35f, 1.551f, 1.551f, 1.35f, 1.8f, 1.35f)
                    close()
                }
                path(
                    fill = SolidColor(Color(0xFFFFFFFF)),
                    pathFillType = PathFillType.EvenOdd
                ) {
                    moveTo(4.5f, 6.3f)
                    curveTo(4.5f, 5.306f, 5.306f, 4.5f, 6.3f, 4.5f)
                    horizontalLineTo(16.2f)
                    curveTo(17.194f, 4.5f, 18f, 5.306f, 18f, 6.3f)
                    verticalLineTo(16.2f)
                    curveTo(18f, 17.194f, 17.194f, 18f, 16.2f, 18f)
                    horizontalLineTo(6.3f)
                    curveTo(5.306f, 18f, 4.5f, 17.194f, 4.5f, 16.2f)
                    verticalLineTo(6.3f)
                    close()
                    moveTo(6.3f, 5.85f)
                    horizontalLineTo(16.2f)
                    curveTo(16.448f, 5.85f, 16.65f, 6.051f, 16.65f, 6.3f)
                    verticalLineTo(16.2f)
                    curveTo(16.65f, 16.448f, 16.448f, 16.65f, 16.2f, 16.65f)
                    horizontalLineTo(6.3f)
                    curveTo(6.051f, 16.65f, 5.85f, 16.448f, 5.85f, 16.2f)
                    verticalLineTo(6.3f)
                    curveTo(5.85f, 6.051f, 6.051f, 5.85f, 6.3f, 5.85f)
                    close()
                }
            }
        }.build()

        return _CopyAlt!!
    }

@Suppress("ObjectPropertyName")
private var _CopyAlt: ImageVector? = null
