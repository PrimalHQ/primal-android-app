package net.primal.android.core.compose.icons.primaliconpack

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp
import net.primal.android.core.compose.icons.PrimalIcons

val PrimalIcons.DetailsRounded: ImageVector
    get() {
        if (_DetailsRounded != null) {
            return _DetailsRounded!!
        }
        _DetailsRounded = ImageVector.Builder(
            name = "DetailsRounded",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(fill = SolidColor(Color(0xFFAAAAAA))) {
                moveTo(7f, 13.5f)
                curveTo(7.828f, 13.5f, 8.5f, 12.828f, 8.5f, 12f)
                curveTo(8.5f, 11.172f, 7.828f, 10.5f, 7f, 10.5f)
                curveTo(6.172f, 10.5f, 5.5f, 11.172f, 5.5f, 12f)
                curveTo(5.5f, 12.828f, 6.172f, 13.5f, 7f, 13.5f)
                close()
            }
            path(fill = SolidColor(Color(0xFFAAAAAA))) {
                moveTo(18.5f, 12f)
                curveTo(18.5f, 12.828f, 17.828f, 13.5f, 17f, 13.5f)
                curveTo(16.172f, 13.5f, 15.5f, 12.828f, 15.5f, 12f)
                curveTo(15.5f, 11.172f, 16.172f, 10.5f, 17f, 10.5f)
                curveTo(17.828f, 10.5f, 18.5f, 11.172f, 18.5f, 12f)
                close()
            }
            path(fill = SolidColor(Color(0xFFAAAAAA))) {
                moveTo(12f, 13.5f)
                curveTo(12.828f, 13.5f, 13.5f, 12.828f, 13.5f, 12f)
                curveTo(13.5f, 11.172f, 12.828f, 10.5f, 12f, 10.5f)
                curveTo(11.172f, 10.5f, 10.5f, 11.172f, 10.5f, 12f)
                curveTo(10.5f, 12.828f, 11.172f, 13.5f, 12f, 13.5f)
                close()
            }
            path(
                fill = SolidColor(Color(0xFFAAAAAA)),
                pathFillType = PathFillType.EvenOdd
            ) {
                moveTo(24f, 12f)
                curveTo(24f, 18.627f, 18.627f, 24f, 12f, 24f)
                curveTo(5.373f, 24f, 0f, 18.627f, 0f, 12f)
                curveTo(0f, 5.373f, 5.373f, 0f, 12f, 0f)
                curveTo(18.627f, 0f, 24f, 5.373f, 24f, 12f)
                close()
                moveTo(22f, 12f)
                curveTo(22f, 17.523f, 17.523f, 22f, 12f, 22f)
                curveTo(6.477f, 22f, 2f, 17.523f, 2f, 12f)
                curveTo(2f, 6.477f, 6.477f, 2f, 12f, 2f)
                curveTo(17.523f, 2f, 22f, 6.477f, 22f, 12f)
                close()
            }
        }.build()

        return _DetailsRounded!!
    }

@Suppress("ObjectPropertyName")
private var _DetailsRounded: ImageVector? = null
