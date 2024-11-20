package net.primal.android.core.compose.icons.primaliconpack

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp
import net.primal.android.core.compose.icons.PrimalIcons

val PrimalIcons.DrawerSignOut: ImageVector
    get() {
        if (_DrawerSignOut != null) {
            return _DrawerSignOut!!
        }
        _DrawerSignOut = ImageVector.Builder(
            name = "DrawerSignOut",
            defaultWidth = 20.dp,
            defaultHeight = 20.dp,
            viewportWidth = 20f,
            viewportHeight = 20f
        ).apply {
            path(fill = SolidColor(Color(0xFFAAAAAA))) {
                moveTo(16f, 1.25f)
                horizontalLineTo(8f)
                curveTo(7.249f, 1.25f, 6.608f, 1.723f, 6.36f, 2.388f)
                curveTo(6.239f, 2.711f, 5.97f, 3f, 5.625f, 3f)
                curveTo(5.28f, 3f, 4.993f, 2.717f, 5.064f, 2.379f)
                curveTo(5.35f, 1.02f, 6.556f, 0f, 8f, 0f)
                horizontalLineTo(16f)
                curveTo(17.657f, 0f, 19f, 1.343f, 19f, 3f)
                verticalLineTo(17f)
                curveTo(19f, 18.657f, 17.657f, 20f, 16f, 20f)
                horizontalLineTo(8f)
                curveTo(6.556f, 20f, 5.35f, 18.98f, 5.064f, 17.621f)
                curveTo(4.993f, 17.283f, 5.28f, 17f, 5.625f, 17f)
                curveTo(5.97f, 17f, 6.239f, 17.289f, 6.36f, 17.612f)
                curveTo(6.608f, 18.277f, 7.249f, 18.75f, 8f, 18.75f)
                horizontalLineTo(16f)
                curveTo(16.966f, 18.75f, 17.75f, 17.966f, 17.75f, 17f)
                verticalLineTo(3f)
                curveTo(17.75f, 2.033f, 16.966f, 1.25f, 16f, 1.25f)
                close()
            }
            path(fill = SolidColor(Color(0xFFAAAAAA))) {
                moveTo(5.467f, 5.915f)
                curveTo(5.696f, 5.657f, 5.673f, 5.262f, 5.415f, 5.033f)
                curveTo(5.157f, 4.804f, 4.762f, 4.827f, 4.533f, 5.085f)
                lineTo(0.459f, 9.668f)
                curveTo(0.291f, 9.857f, 0.291f, 10.143f, 0.459f, 10.332f)
                lineTo(4.533f, 14.915f)
                curveTo(4.762f, 15.173f, 5.157f, 15.196f, 5.415f, 14.967f)
                curveTo(5.673f, 14.738f, 5.696f, 14.343f, 5.467f, 14.085f)
                lineTo(2.392f, 10.625f)
                horizontalLineTo(12f)
                curveTo(12.345f, 10.625f, 12.625f, 10.345f, 12.625f, 10f)
                curveTo(12.625f, 9.655f, 12.345f, 9.375f, 12f, 9.375f)
                horizontalLineTo(2.392f)
                lineTo(5.467f, 5.915f)
                close()
            }
        }.build()

        return _DrawerSignOut!!
    }

@Suppress("ObjectPropertyName")
private var _DrawerSignOut: ImageVector? = null
