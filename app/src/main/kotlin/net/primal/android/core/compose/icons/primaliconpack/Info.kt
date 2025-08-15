package net.primal.android.core.compose.icons.primaliconpack

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp
import net.primal.android.core.compose.icons.PrimalIcons

val PrimalIcons.Info: ImageVector
    get() {
        if (_Info != null) {
            return _Info!!
        }
        _Info = ImageVector.Builder(
            name = "Info",
            defaultWidth = 22.dp,
            defaultHeight = 22.dp,
            viewportWidth = 22f,
            viewportHeight = 22f
        ).apply {
            path(fill = SolidColor(Color.White)) {
                moveTo(9.922f, 8.45f)
                curveTo(10.989f, 7.917f, 12.222f, 8.783f, 12.083f, 9.968f)
                lineTo(11.407f, 15.708f)
                lineTo(12.165f, 15.329f)
                curveTo(12.535f, 15.144f, 12.986f, 15.295f, 13.171f, 15.665f)
                curveTo(13.356f, 16.035f, 13.205f, 16.486f, 12.835f, 16.671f)
                lineTo(12.078f, 17.05f)
                curveTo(11.011f, 17.583f, 9.778f, 16.717f, 9.917f, 15.532f)
                lineTo(10.593f, 9.792f)
                lineTo(9.835f, 10.171f)
                curveTo(9.465f, 10.356f, 9.014f, 10.205f, 8.829f, 9.835f)
                curveTo(8.644f, 9.465f, 8.795f, 9.014f, 9.165f, 8.829f)
                lineTo(9.922f, 8.45f)
                close()
            }
            path(fill = SolidColor(Color.White)) {
                moveTo(11.125f, 4f)
                curveTo(11.746f, 4f, 12.25f, 4.504f, 12.25f, 5.125f)
                curveTo(12.25f, 5.746f, 11.746f, 6.25f, 11.125f, 6.25f)
                curveTo(10.504f, 6.25f, 10f, 5.746f, 10f, 5.125f)
                curveTo(10f, 4.504f, 10.504f, 4f, 11.125f, 4f)
                close()
            }
            path(
                fill = SolidColor(Color.White),
                pathFillType = PathFillType.EvenOdd
            ) {
                moveTo(11f, 0f)
                curveTo(17.075f, 0f, 22f, 4.925f, 22f, 11f)
                curveTo(22f, 17.075f, 17.075f, 22f, 11f, 22f)
                curveTo(4.925f, 22f, 0f, 17.075f, 0f, 11f)
                curveTo(0f, 4.925f, 4.925f, 0f, 11f, 0f)
                close()
                moveTo(11f, 1.25f)
                curveTo(5.615f, 1.25f, 1.25f, 5.615f, 1.25f, 11f)
                curveTo(1.25f, 16.385f, 5.615f, 20.75f, 11f, 20.75f)
                curveTo(16.385f, 20.75f, 20.75f, 16.385f, 20.75f, 11f)
                curveTo(20.75f, 5.615f, 16.385f, 1.25f, 11f, 1.25f)
                close()
            }
        }.build()

        return _Info!!
    }

@Suppress("ObjectPropertyName")
private var _Info: ImageVector? = null
