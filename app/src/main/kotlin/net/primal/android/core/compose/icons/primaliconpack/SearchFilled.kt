package net.primal.android.core.compose.icons.primaliconpack

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp
import net.primal.android.core.compose.icons.PrimalIcons

val PrimalIcons.SearchFilled: ImageVector
    get() {
        if (_SearchSelected != null) {
            return _SearchSelected!!
        }
        _SearchSelected = ImageVector.Builder(
            name = "SearchSelected",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(
                fill = SolidColor(Color(0xFFFFFFFF)),
                pathFillType = PathFillType.EvenOdd
            ) {
                moveTo(14.297f, 16.512f)
                curveTo(13.042f, 17.347f, 11.536f, 17.833f, 9.917f, 17.833f)
                curveTo(5.544f, 17.833f, 2f, 14.289f, 2f, 9.917f)
                curveTo(2f, 5.544f, 5.544f, 2f, 9.917f, 2f)
                curveTo(14.289f, 2f, 17.833f, 5.544f, 17.833f, 9.917f)
                curveTo(17.833f, 11.536f, 17.347f, 13.042f, 16.512f, 14.297f)
                lineTo(20.892f, 18.677f)
                curveTo(21.504f, 19.289f, 21.504f, 20.281f, 20.892f, 20.892f)
                curveTo(20.281f, 21.504f, 19.289f, 21.504f, 18.677f, 20.892f)
                lineTo(14.297f, 16.512f)
                close()
                moveTo(16.167f, 9.917f)
                curveTo(16.167f, 13.368f, 13.368f, 16.167f, 9.917f, 16.167f)
                curveTo(6.465f, 16.167f, 3.667f, 13.368f, 3.667f, 9.917f)
                curveTo(3.667f, 6.465f, 6.465f, 3.667f, 9.917f, 3.667f)
                curveTo(13.368f, 3.667f, 16.167f, 6.465f, 16.167f, 9.917f)
                close()
            }
        }.build()

        return _SearchSelected!!
    }

@Suppress("ObjectPropertyName")
private var _SearchSelected: ImageVector? = null
