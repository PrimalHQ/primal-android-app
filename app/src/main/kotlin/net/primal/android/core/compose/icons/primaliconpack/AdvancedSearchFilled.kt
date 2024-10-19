package net.primal.android.core.compose.icons.primaliconpack

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp
import net.primal.android.core.compose.icons.PrimalIcons

val PrimalIcons.AdvancedSearchFilled: ImageVector
    get() {
        if (_SearchSettingsSelected != null) {
            return _SearchSettingsSelected!!
        }
        _SearchSettingsSelected = ImageVector.Builder(
            name = "SearchSettingsSelected",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(
                fill = SolidColor(Color(0xFFFFFFFF)),
                pathFillType = PathFillType.EvenOdd
            ) {
                moveTo(1.75f, 7f)
                curveTo(1.75f, 4.653f, 3.653f, 2.75f, 6f, 2.75f)
                curveTo(8.091f, 2.75f, 9.83f, 4.26f, 10.184f, 6.25f)
                horizontalLineTo(21.375f)
                curveTo(21.858f, 6.25f, 22.25f, 6.642f, 22.25f, 7.125f)
                curveTo(22.25f, 7.608f, 21.858f, 8f, 21.375f, 8f)
                horizontalLineTo(10.132f)
                curveTo(9.682f, 9.865f, 8.003f, 11.25f, 6f, 11.25f)
                curveTo(3.653f, 11.25f, 1.75f, 9.347f, 1.75f, 7f)
                close()
                moveTo(13.816f, 16.25f)
                curveTo(14.17f, 14.26f, 15.909f, 12.75f, 18f, 12.75f)
                curveTo(20.347f, 12.75f, 22.25f, 14.653f, 22.25f, 17f)
                curveTo(22.25f, 19.347f, 20.347f, 21.25f, 18f, 21.25f)
                curveTo(15.997f, 21.25f, 14.318f, 19.865f, 13.868f, 18f)
                horizontalLineTo(3.625f)
                curveTo(3.142f, 18f, 2.75f, 17.608f, 2.75f, 17.125f)
                curveTo(2.75f, 16.642f, 3.142f, 16.25f, 3.625f, 16.25f)
                horizontalLineTo(13.816f)
                close()
            }
        }.build()

        return _SearchSettingsSelected!!
    }

@Suppress("ObjectPropertyName")
private var _SearchSettingsSelected: ImageVector? = null
