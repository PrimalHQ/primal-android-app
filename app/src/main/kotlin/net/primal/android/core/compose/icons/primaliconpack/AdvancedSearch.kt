package net.primal.android.core.compose.icons.primaliconpack

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp
import net.primal.android.core.compose.icons.PrimalIcons

val PrimalIcons.AdvancedSearch: ImageVector
    get() {
        if (_SearchSettings != null) {
            return _SearchSettings!!
        }
        _SearchSettings = ImageVector.Builder(
            name = "SearchSettings",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(
                fill = SolidColor(Color(0xFFAAAAAA)),
                pathFillType = PathFillType.EvenOdd
            ) {
                moveTo(9.93f, 7.75f)
                curveTo(9.579f, 9.601f, 7.953f, 11f, 6f, 11f)
                curveTo(3.791f, 11f, 2f, 9.209f, 2f, 7f)
                curveTo(2f, 4.791f, 3.791f, 3f, 6f, 3f)
                curveTo(8.04f, 3f, 9.723f, 4.527f, 9.969f, 6.5f)
                horizontalLineTo(21.375f)
                curveTo(21.72f, 6.5f, 22f, 6.78f, 22f, 7.125f)
                curveTo(22f, 7.47f, 21.72f, 7.75f, 21.375f, 7.75f)
                horizontalLineTo(9.93f)
                close()
                moveTo(8.75f, 7f)
                curveTo(8.75f, 8.519f, 7.519f, 9.75f, 6f, 9.75f)
                curveTo(4.481f, 9.75f, 3.25f, 8.519f, 3.25f, 7f)
                curveTo(3.25f, 5.481f, 4.481f, 4.25f, 6f, 4.25f)
                curveTo(7.519f, 4.25f, 8.75f, 5.481f, 8.75f, 7f)
                close()
            }
            path(
                fill = SolidColor(Color(0xFFAAAAAA)),
                pathFillType = PathFillType.EvenOdd
            ) {
                moveTo(3f, 17.125f)
                curveTo(3f, 16.78f, 3.28f, 16.5f, 3.625f, 16.5f)
                horizontalLineTo(14.031f)
                curveTo(14.277f, 14.527f, 15.96f, 13f, 18f, 13f)
                curveTo(20.209f, 13f, 22f, 14.791f, 22f, 17f)
                curveTo(22f, 19.209f, 20.209f, 21f, 18f, 21f)
                curveTo(16.047f, 21f, 14.421f, 19.601f, 14.07f, 17.75f)
                horizontalLineTo(3.625f)
                curveTo(3.28f, 17.75f, 3f, 17.47f, 3f, 17.125f)
                close()
                moveTo(20.75f, 17f)
                curveTo(20.75f, 18.519f, 19.519f, 19.75f, 18f, 19.75f)
                curveTo(16.481f, 19.75f, 15.25f, 18.519f, 15.25f, 17f)
                curveTo(15.25f, 15.481f, 16.481f, 14.25f, 18f, 14.25f)
                curveTo(19.519f, 14.25f, 20.75f, 15.481f, 20.75f, 17f)
                close()
            }
        }.build()

        return _SearchSettings!!
    }

@Suppress("ObjectPropertyName")
private var _SearchSettings: ImageVector? = null
