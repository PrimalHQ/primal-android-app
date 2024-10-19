package net.primal.android.core.compose.icons.primaliconpack

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp
import net.primal.android.core.compose.icons.PrimalIcons

val PrimalIcons.LongReadFilled: ImageVector
    get() {
        if (_LongReadFilled != null) {
            return _LongReadFilled!!
        }
        _LongReadFilled = ImageVector.Builder(
            name = "LongReadFilled",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(fill = SolidColor(Color(0xFFFFFFFF))) {
                moveTo(3.25f, 3.667f)
                curveTo(2.79f, 3.667f, 2.417f, 4.04f, 2.417f, 4.5f)
                curveTo(2.417f, 4.96f, 2.79f, 5.333f, 3.25f, 5.333f)
                horizontalLineTo(20.75f)
                curveTo(21.21f, 5.333f, 21.583f, 4.96f, 21.583f, 4.5f)
                curveTo(21.583f, 4.04f, 21.21f, 3.667f, 20.75f, 3.667f)
                horizontalLineTo(3.25f)
                close()
            }
            path(fill = SolidColor(Color(0xFFFFFFFF))) {
                moveTo(3.25f, 8.667f)
                curveTo(2.79f, 8.667f, 2.417f, 9.04f, 2.417f, 9.5f)
                curveTo(2.417f, 9.96f, 2.79f, 10.333f, 3.25f, 10.333f)
                horizontalLineTo(20.75f)
                curveTo(21.21f, 10.333f, 21.583f, 9.96f, 21.583f, 9.5f)
                curveTo(21.583f, 9.04f, 21.21f, 8.667f, 20.75f, 8.667f)
                horizontalLineTo(3.25f)
                close()
            }
            path(fill = SolidColor(Color(0xFFFFFFFF))) {
                moveTo(2.417f, 14.5f)
                curveTo(2.417f, 14.04f, 2.79f, 13.667f, 3.25f, 13.667f)
                horizontalLineTo(20.75f)
                curveTo(21.21f, 13.667f, 21.583f, 14.04f, 21.583f, 14.5f)
                curveTo(21.583f, 14.96f, 21.21f, 15.333f, 20.75f, 15.333f)
                horizontalLineTo(3.25f)
                curveTo(2.79f, 15.333f, 2.417f, 14.96f, 2.417f, 14.5f)
                close()
            }
            path(fill = SolidColor(Color(0xFFFFFFFF))) {
                moveTo(3.25f, 18.667f)
                curveTo(2.79f, 18.667f, 2.417f, 19.04f, 2.417f, 19.5f)
                curveTo(2.417f, 19.96f, 2.79f, 20.333f, 3.25f, 20.333f)
                horizontalLineTo(14.083f)
                curveTo(14.543f, 20.333f, 14.917f, 19.96f, 14.917f, 19.5f)
                curveTo(14.917f, 19.04f, 14.543f, 18.667f, 14.083f, 18.667f)
                horizontalLineTo(3.25f)
                close()
            }
        }.build()

        return _LongReadFilled!!
    }

@Suppress("ObjectPropertyName")
private var _LongReadFilled: ImageVector? = null
