package net.primal.android.core.compose.icons.primaliconpack

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp
import net.primal.android.core.compose.icons.PrimalIcons

val PrimalIcons.LowSecurity: ImageVector
    get() {
        if (_LowSecurity != null) {
            return _LowSecurity!!
        }
        _LowSecurity = ImageVector.Builder(
            name = "LowSecurity",
            defaultWidth = 25.dp,
            defaultHeight = 26.dp,
            viewportWidth = 25f,
            viewportHeight = 26f
        ).apply {
            path(fill = SolidColor(Color.White)) {
                moveTo(20.24f, 4.049f)
                curveTo(20.656f, 4.049f, 20.991f, 4.392f, 20.973f, 4.808f)
                curveTo(20.86f, 7.355f, 20.462f, 11.469f, 19.155f, 14.69f)
                curveTo(18.211f, 17.017f, 16.418f, 18.872f, 14.775f, 20.175f)
                curveTo(13.964f, 20.818f, 13.216f, 21.308f, 12.673f, 21.634f)
                curveTo(12.59f, 21.683f, 12.512f, 21.728f, 12.439f, 21.77f)
                curveTo(12.386f, 21.742f, 12.328f, 21.713f, 12.269f, 21.682f)
                curveTo(11.755f, 21.408f, 11.039f, 20.983f, 10.255f, 20.387f)
                curveTo(8.68f, 19.19f, 6.879f, 17.344f, 5.83f, 14.701f)
                curveTo(4.414f, 11.132f, 4.044f, 7.198f, 3.966f, 4.796f)
                curveTo(3.952f, 4.384f, 4.286f, 4.049f, 4.697f, 4.049f)
                horizontalLineTo(20.24f)
                close()
            }
            path(
                fill = SolidColor(Color.White),
                pathFillType = PathFillType.EvenOdd
            ) {
                moveTo(23.289f, 0f)
                curveTo(24.237f, 0f, 25.008f, 0.751f, 24.986f, 1.699f)
                curveTo(24.919f, 4.662f, 24.528f, 10.924f, 22.546f, 15.804f)
                curveTo(19.862f, 22.41f, 12.558f, 25.978f, 12.512f, 26f)
                curveTo(12.512f, 26f, 5.414f, 23.196f, 2.478f, 15.804f)
                curveTo(0.332f, 10.403f, 0.015f, 4.457f, 0f, 1.646f)
                curveTo(-0.005f, 0.72f, 0.754f, 0f, 1.681f, 0f)
                horizontalLineTo(23.289f)
                close()
                moveTo(3.886f, 2.549f)
                curveTo(3.092f, 2.549f, 2.441f, 3.169f, 2.447f, 3.963f)
                lineTo(2.469f, 4.932f)
                curveTo(2.556f, 7.429f, 2.948f, 11.505f, 4.436f, 15.254f)
                lineTo(4.666f, 15.801f)
                curveTo(6.824f, 20.621f, 11.158f, 22.848f, 12.241f, 23.343f)
                lineTo(12.49f, 23.451f)
                curveTo(12.49f, 23.451f, 18.021f, 20.758f, 20.332f, 15.746f)
                lineTo(20.546f, 15.254f)
                curveTo(21.921f, 11.865f, 22.342f, 7.647f, 22.466f, 5.025f)
                lineTo(22.502f, 4.007f)
                curveTo(22.522f, 3.194f, 21.863f, 2.549f, 21.05f, 2.549f)
                horizontalLineTo(3.886f)
                close()
            }
        }.build()

        return _LowSecurity!!
    }

@Suppress("ObjectPropertyName")
private var _LowSecurity: ImageVector? = null
