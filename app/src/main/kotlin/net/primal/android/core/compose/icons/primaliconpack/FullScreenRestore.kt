package net.primal.android.core.compose.icons.primaliconpack

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp
import net.primal.android.core.compose.icons.PrimalIcons

val PrimalIcons.FullScreenRestore: ImageVector
    get() {
        if (_FullScreenRestore != null) {
            return _FullScreenRestore!!
        }
        _FullScreenRestore = ImageVector.Builder(
            name = "FullScreenRestore",
            defaultWidth = 28.dp,
            defaultHeight = 28.dp,
            viewportWidth = 28f,
            viewportHeight = 28f
        ).apply {
            path(fill = SolidColor(Color(0xFFFFFFFF))) {
                moveTo(17.75f, 5f)
                curveTo(18.164f, 5f, 18.5f, 5.336f, 18.5f, 5.75f)
                verticalLineTo(8f)
                curveTo(18.5f, 8.828f, 19.172f, 9.5f, 20f, 9.5f)
                horizontalLineTo(23.25f)
                curveTo(23.664f, 9.5f, 24f, 9.836f, 24f, 10.25f)
                curveTo(24f, 10.664f, 23.664f, 11f, 23.25f, 11f)
                horizontalLineTo(20f)
                curveTo(18.395f, 11f, 17.084f, 9.739f, 17.004f, 8.154f)
                lineTo(17f, 8f)
                verticalLineTo(5.75f)
                curveTo(17f, 5.336f, 17.336f, 5f, 17.75f, 5f)
                close()
            }
            path(fill = SolidColor(Color(0xFFFFFFFF))) {
                moveTo(11f, 8f)
                curveTo(11f, 9.657f, 9.657f, 11f, 8f, 11f)
                horizontalLineTo(4.75f)
                curveTo(4.336f, 11f, 4f, 10.664f, 4f, 10.25f)
                curveTo(4f, 9.836f, 4.336f, 9.5f, 4.75f, 9.5f)
                horizontalLineTo(8f)
                curveTo(8.828f, 9.5f, 9.5f, 8.828f, 9.5f, 8f)
                verticalLineTo(5.75f)
                curveTo(9.5f, 5.336f, 9.836f, 5f, 10.25f, 5f)
                curveTo(10.664f, 5f, 11f, 5.336f, 11f, 5.75f)
                verticalLineTo(8f)
                close()
            }
            path(fill = SolidColor(Color(0xFFFFFFFF))) {
                moveTo(8f, 17f)
                curveTo(9.657f, 17f, 11f, 18.343f, 11f, 20f)
                verticalLineTo(22.25f)
                curveTo(11f, 22.664f, 10.664f, 23f, 10.25f, 23f)
                curveTo(9.836f, 23f, 9.5f, 22.664f, 9.5f, 22.25f)
                verticalLineTo(20f)
                curveTo(9.5f, 19.172f, 8.828f, 18.5f, 8f, 18.5f)
                horizontalLineTo(4.75f)
                curveTo(4.336f, 18.5f, 4f, 18.164f, 4f, 17.75f)
                curveTo(4f, 17.336f, 4.336f, 17f, 4.75f, 17f)
                horizontalLineTo(8f)
                close()
            }
            path(fill = SolidColor(Color(0xFFFFFFFF))) {
                moveTo(24f, 17.75f)
                curveTo(24f, 18.164f, 23.664f, 18.5f, 23.25f, 18.5f)
                horizontalLineTo(20f)
                curveTo(19.172f, 18.5f, 18.5f, 19.172f, 18.5f, 20f)
                verticalLineTo(22.25f)
                curveTo(18.5f, 22.664f, 18.164f, 23f, 17.75f, 23f)
                curveTo(17.336f, 23f, 17f, 22.664f, 17f, 22.25f)
                verticalLineTo(20f)
                curveTo(17f, 18.343f, 18.343f, 17f, 20f, 17f)
                horizontalLineTo(23.25f)
                curveTo(23.664f, 17f, 24f, 17.336f, 24f, 17.75f)
                close()
            }
        }.build()

        return _FullScreenRestore!!
    }

@Suppress("ObjectPropertyName")
private var _FullScreenRestore: ImageVector? = null
