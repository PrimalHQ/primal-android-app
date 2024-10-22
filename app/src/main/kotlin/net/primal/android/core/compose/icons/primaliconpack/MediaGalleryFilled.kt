package net.primal.android.core.compose.icons.primaliconpack

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp
import kotlin.Suppress
import net.primal.android.core.compose.icons.PrimalIcons

val PrimalIcons.MediaGalleryFilled: ImageVector
    get() {
        if (_MediaGalleryFilled != null) {
            return _MediaGalleryFilled!!
        }
        _MediaGalleryFilled = ImageVector.Builder(
            name = "MediaGalleryFilled",
            defaultWidth = 18.dp,
            defaultHeight = 18.dp,
            viewportWidth = 18f,
            viewportHeight = 18f
        ).apply {
            path(fill = SolidColor(Color(0xFFFFFFFF))) {
                moveTo(2.5f, 0.5f)
                curveTo(1.396f, 0.5f, 0.5f, 1.395f, 0.5f, 2.5f)
                verticalLineTo(12.5f)
                curveTo(0.5f, 13.605f, 1.396f, 14.5f, 2.5f, 14.5f)
                horizontalLineTo(12.5f)
                curveTo(13.604f, 14.5f, 14.5f, 13.605f, 14.5f, 12.5f)
                verticalLineTo(2.5f)
                curveTo(14.5f, 1.395f, 13.604f, 0.5f, 12.5f, 0.5f)
                horizontalLineTo(2.5f)
                close()
            }
            path(fill = SolidColor(Color(0xFFFFFFFF))) {
                moveTo(5.5f, 17.5f)
                curveTo(4.55f, 17.5f, 3.755f, 16.837f, 3.551f, 15.949f)
                curveTo(3.695f, 15.982f, 3.846f, 16f, 4f, 16f)
                horizontalLineTo(14f)
                curveTo(15.105f, 16f, 16f, 15.105f, 16f, 14f)
                verticalLineTo(4f)
                curveTo(16f, 3.845f, 15.983f, 3.695f, 15.95f, 3.551f)
                curveTo(16.838f, 3.755f, 17.5f, 4.55f, 17.5f, 5.5f)
                verticalLineTo(15.5f)
                curveTo(17.5f, 16.605f, 16.605f, 17.5f, 15.5f, 17.5f)
                horizontalLineTo(5.5f)
                close()
            }
        }.build()

        return _MediaGalleryFilled!!
    }

@Suppress("ObjectPropertyName")
private var _MediaGalleryFilled: ImageVector? = null
