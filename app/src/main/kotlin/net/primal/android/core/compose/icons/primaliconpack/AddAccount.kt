package net.primal.android.core.compose.icons.primaliconpack

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp
import net.primal.android.core.compose.icons.PrimalIcons

val PrimalIcons.AddAccount: ImageVector
    get() {
        if (_AddAccount != null) {
            return _AddAccount!!
        }
        _AddAccount = ImageVector.Builder(
            name = "AddAccount",
            defaultWidth = 32.dp,
            defaultHeight = 32.dp,
            viewportWidth = 32f,
            viewportHeight = 32f
        ).apply {
            path(
                fill = SolidColor(Color(0xFFAAAAAA)),
                pathFillType = PathFillType.EvenOdd
            ) {
                moveTo(16f, 4f)
                curveTo(16.617f, 4f, 17.222f, 4.048f, 17.813f, 4.139f)
                curveTo(18.178f, 4.195f, 18.338f, 4.627f, 18.199f, 4.97f)
                curveTo(18.096f, 5.223f, 17.838f, 5.405f, 17.567f, 5.365f)
                curveTo(17.056f, 5.29f, 16.532f, 5.25f, 16f, 5.25f)
                curveTo(10.063f, 5.25f, 5.25f, 10.063f, 5.25f, 16f)
                curveTo(5.25f, 18.388f, 6.03f, 20.593f, 7.347f, 22.377f)
                curveTo(7.969f, 20.977f, 9.369f, 20f, 11f, 20f)
                horizontalLineTo(21f)
                curveTo(22.631f, 20f, 24.03f, 20.977f, 24.652f, 22.377f)
                curveTo(25.97f, 20.593f, 26.75f, 18.388f, 26.75f, 16f)
                curveTo(26.75f, 15.467f, 26.709f, 14.943f, 26.634f, 14.432f)
                curveTo(26.594f, 14.161f, 26.776f, 13.903f, 27.029f, 13.8f)
                curveTo(27.372f, 13.661f, 27.804f, 13.821f, 27.86f, 14.186f)
                curveTo(27.951f, 14.777f, 28f, 15.383f, 28f, 16f)
                curveTo(28f, 22.627f, 22.627f, 28f, 16f, 28f)
                curveTo(9.373f, 28f, 4f, 22.627f, 4f, 16f)
                curveTo(4f, 9.373f, 9.373f, 4f, 16f, 4f)
                close()
                moveTo(11f, 21.25f)
                curveTo(9.653f, 21.25f, 8.535f, 22.218f, 8.298f, 23.496f)
                curveTo(10.251f, 25.502f, 12.979f, 26.75f, 16f, 26.75f)
                curveTo(19.021f, 26.75f, 21.748f, 25.502f, 23.701f, 23.496f)
                curveTo(23.464f, 22.218f, 22.347f, 21.25f, 21f, 21.25f)
                horizontalLineTo(11f)
                close()
            }
            path(
                fill = SolidColor(Color(0xFFAAAAAA)),
                pathFillType = PathFillType.EvenOdd
            ) {
                moveTo(16f, 10f)
                curveTo(18.209f, 10f, 20f, 11.791f, 20f, 14f)
                curveTo(20f, 16.209f, 18.209f, 18f, 16f, 18f)
                curveTo(13.791f, 18f, 12f, 16.209f, 12f, 14f)
                curveTo(12f, 11.791f, 13.791f, 10f, 16f, 10f)
                close()
                moveTo(16f, 11.25f)
                curveTo(14.481f, 11.25f, 13.25f, 12.481f, 13.25f, 14f)
                curveTo(13.25f, 15.519f, 14.481f, 16.75f, 16f, 16.75f)
                curveTo(17.519f, 16.75f, 18.75f, 15.519f, 18.75f, 14f)
                curveTo(18.75f, 12.481f, 17.519f, 11.25f, 16f, 11.25f)
                close()
            }
            path(fill = SolidColor(Color(0xFFAAAAAA))) {
                moveTo(26f, 0f)
                curveTo(26.414f, 0f, 26.75f, 0.336f, 26.75f, 0.75f)
                verticalLineTo(5.25f)
                horizontalLineTo(31.25f)
                curveTo(31.664f, 5.25f, 32f, 5.586f, 32f, 6f)
                curveTo(32f, 6.414f, 31.664f, 6.75f, 31.25f, 6.75f)
                horizontalLineTo(26.75f)
                verticalLineTo(11.25f)
                curveTo(26.75f, 11.664f, 26.414f, 12f, 26f, 12f)
                curveTo(25.586f, 12f, 25.25f, 11.664f, 25.25f, 11.25f)
                verticalLineTo(6.75f)
                horizontalLineTo(20.75f)
                curveTo(20.336f, 6.75f, 20f, 6.414f, 20f, 6f)
                curveTo(20f, 5.586f, 20.336f, 5.25f, 20.75f, 5.25f)
                horizontalLineTo(25.25f)
                verticalLineTo(0.75f)
                curveTo(25.25f, 0.336f, 25.586f, 0f, 26f, 0f)
                close()
            }
        }.build()

        return _AddAccount!!
    }

@Suppress("ObjectPropertyName")
private var _AddAccount: ImageVector? = null
