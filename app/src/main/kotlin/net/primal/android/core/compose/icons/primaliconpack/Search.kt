package net.primal.android.core.compose.icons.primaliconpack

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp
import net.primal.android.core.compose.icons.PrimalIcons

val PrimalIcons.Search: ImageVector
    get() {
        if (_Search != null) {
            return _Search!!
        }
        _Search = ImageVector.Builder(
            name = "Search",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(
                fill = SolidColor(Color(0xFFAAAAAA)),
                pathFillType = PathFillType.Companion.EvenOdd
            ) {
                moveTo(2.208f, 9.917f)
                curveTo(2.208f, 5.659f, 5.66f, 2.208f, 9.917f, 2.208f)
                curveTo(14.174f, 2.208f, 17.625f, 5.659f, 17.625f, 9.917f)
                curveTo(17.625f, 11.494f, 17.152f, 12.96f, 16.339f, 14.182f)
                lineTo(16.244f, 14.324f)
                lineTo(20.745f, 18.824f)
                curveTo(21.275f, 19.355f, 21.275f, 20.215f, 20.745f, 20.745f)
                curveTo(20.215f, 21.275f, 19.355f, 21.275f, 18.825f, 20.745f)
                lineTo(14.324f, 16.244f)
                lineTo(14.182f, 16.339f)
                curveTo(12.96f, 17.151f, 11.494f, 17.625f, 9.917f, 17.625f)
                curveTo(5.66f, 17.625f, 2.208f, 14.174f, 2.208f, 9.917f)
                close()
                moveTo(9.917f, 3.458f)
                curveTo(6.35f, 3.458f, 3.458f, 6.35f, 3.458f, 9.917f)
                curveTo(3.458f, 13.483f, 6.35f, 16.375f, 9.917f, 16.375f)
                curveTo(13.483f, 16.375f, 16.375f, 13.483f, 16.375f, 9.917f)
                curveTo(16.375f, 6.35f, 13.483f, 3.458f, 9.917f, 3.458f)
                close()
            }
        }.build()

        return _Search!!
    }

@Suppress("ObjectPropertyName")
private var _Search: ImageVector? = null
