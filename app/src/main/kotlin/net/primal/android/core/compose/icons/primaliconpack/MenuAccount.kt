package net.primal.android.core.compose.icons.primaliconpack

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp
import net.primal.android.core.compose.icons.PrimalIcons

val PrimalIcons.MenuAccount: ImageVector
    get() {
        if (_MenuAccount != null) {
            return _MenuAccount!!
        }
        _MenuAccount = ImageVector.Builder(
            name = "MenuAccount",
            defaultWidth = 32.dp,
            defaultHeight = 32.dp,
            viewportWidth = 32f,
            viewportHeight = 32f
        ).apply {
            path(fill = SolidColor(Color(0xFFAAAAAA))) {
                moveTo(10.5f, 14.5f)
                curveTo(11.328f, 14.5f, 12f, 15.172f, 12f, 16f)
                curveTo(12f, 16.828f, 11.328f, 17.5f, 10.5f, 17.5f)
                curveTo(9.672f, 17.5f, 9f, 16.828f, 9f, 16f)
                curveTo(9f, 15.172f, 9.672f, 14.5f, 10.5f, 14.5f)
                close()
            }
            path(fill = SolidColor(Color(0xFFAAAAAA))) {
                moveTo(16f, 14.5f)
                curveTo(16.828f, 14.5f, 17.5f, 15.172f, 17.5f, 16f)
                curveTo(17.5f, 16.828f, 16.828f, 17.5f, 16f, 17.5f)
                curveTo(15.172f, 17.5f, 14.5f, 16.828f, 14.5f, 16f)
                curveTo(14.5f, 15.172f, 15.172f, 14.5f, 16f, 14.5f)
                close()
            }
            path(fill = SolidColor(Color(0xFFAAAAAA))) {
                moveTo(21.5f, 14.5f)
                curveTo(22.328f, 14.5f, 23f, 15.172f, 23f, 16f)
                curveTo(23f, 16.828f, 22.328f, 17.5f, 21.5f, 17.5f)
                curveTo(20.672f, 17.5f, 20f, 16.828f, 20f, 16f)
                curveTo(20f, 15.172f, 20.672f, 14.5f, 21.5f, 14.5f)
                close()
            }
            path(
                fill = SolidColor(Color(0xFFAAAAAA)),
                pathFillType = PathFillType.EvenOdd
            ) {
                moveTo(16f, 4f)
                curveTo(22.627f, 4f, 28f, 9.373f, 28f, 16f)
                curveTo(28f, 22.627f, 22.627f, 28f, 16f, 28f)
                curveTo(9.373f, 28f, 4f, 22.627f, 4f, 16f)
                curveTo(4f, 9.373f, 9.373f, 4f, 16f, 4f)
                close()
                moveTo(16f, 5.25f)
                curveTo(10.063f, 5.25f, 5.25f, 10.063f, 5.25f, 16f)
                curveTo(5.25f, 21.937f, 10.063f, 26.75f, 16f, 26.75f)
                curveTo(21.937f, 26.75f, 26.75f, 21.937f, 26.75f, 16f)
                curveTo(26.75f, 10.063f, 21.937f, 5.25f, 16f, 5.25f)
                close()
            }
        }.build()

        return _MenuAccount!!
    }

@Suppress("ObjectPropertyName")
private var _MenuAccount: ImageVector? = null
