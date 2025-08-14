package net.primal.android.core.compose.icons.primaliconpack

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp
import net.primal.android.core.compose.icons.PrimalIcons

val PrimalIcons.FullScreen: ImageVector
    get() {
        if (_FullScreen != null) {
            return _FullScreen!!
        }
        _FullScreen = ImageVector.Builder(
            name = "FullScreen",
            defaultWidth = 28.dp,
            defaultHeight = 28.dp,
            viewportWidth = 28f,
            viewportHeight = 28f
        ).apply {
            path(fill = SolidColor(Color.White)) {
                moveTo(4.75f, 17f)
                curveTo(5.164f, 17f, 5.5f, 17.336f, 5.5f, 17.75f)
                verticalLineTo(20f)
                curveTo(5.5f, 20.828f, 6.172f, 21.5f, 7f, 21.5f)
                horizontalLineTo(10.25f)
                curveTo(10.664f, 21.5f, 11f, 21.836f, 11f, 22.25f)
                curveTo(11f, 22.664f, 10.664f, 23f, 10.25f, 23f)
                horizontalLineTo(7f)
                curveTo(5.395f, 23f, 4.084f, 21.739f, 4.004f, 20.154f)
                lineTo(4f, 20f)
                verticalLineTo(17.75f)
                curveTo(4f, 17.336f, 4.336f, 17f, 4.75f, 17f)
                close()
            }
            path(fill = SolidColor(Color.White)) {
                moveTo(24f, 20f)
                curveTo(24f, 21.657f, 22.657f, 23f, 21f, 23f)
                horizontalLineTo(17.75f)
                curveTo(17.336f, 23f, 17f, 22.664f, 17f, 22.25f)
                curveTo(17f, 21.836f, 17.336f, 21.5f, 17.75f, 21.5f)
                horizontalLineTo(21f)
                curveTo(21.828f, 21.5f, 22.5f, 20.828f, 22.5f, 20f)
                verticalLineTo(17.75f)
                curveTo(22.5f, 17.336f, 22.836f, 17f, 23.25f, 17f)
                curveTo(23.664f, 17f, 24f, 17.336f, 24f, 17.75f)
                verticalLineTo(20f)
                close()
            }
            path(fill = SolidColor(Color.White)) {
                moveTo(21f, 5f)
                curveTo(22.657f, 5f, 24f, 6.343f, 24f, 8f)
                verticalLineTo(10.25f)
                curveTo(24f, 10.664f, 23.664f, 11f, 23.25f, 11f)
                curveTo(22.836f, 11f, 22.5f, 10.664f, 22.5f, 10.25f)
                verticalLineTo(8f)
                curveTo(22.5f, 7.172f, 21.828f, 6.5f, 21f, 6.5f)
                horizontalLineTo(17.75f)
                curveTo(17.336f, 6.5f, 17f, 6.164f, 17f, 5.75f)
                curveTo(17f, 5.336f, 17.336f, 5f, 17.75f, 5f)
                horizontalLineTo(21f)
                close()
            }
            path(fill = SolidColor(Color.White)) {
                moveTo(11f, 5.75f)
                curveTo(11f, 6.164f, 10.664f, 6.5f, 10.25f, 6.5f)
                horizontalLineTo(7f)
                curveTo(6.172f, 6.5f, 5.5f, 7.172f, 5.5f, 8f)
                verticalLineTo(10.25f)
                curveTo(5.5f, 10.664f, 5.164f, 11f, 4.75f, 11f)
                curveTo(4.336f, 11f, 4f, 10.664f, 4f, 10.25f)
                verticalLineTo(8f)
                curveTo(4f, 6.343f, 5.343f, 5f, 7f, 5f)
                horizontalLineTo(10.25f)
                curveTo(10.664f, 5f, 11f, 5.336f, 11f, 5.75f)
                close()
            }
        }.build()

        return _FullScreen!!
    }

@Suppress("ObjectPropertyName")
private var _FullScreen: ImageVector? = null
