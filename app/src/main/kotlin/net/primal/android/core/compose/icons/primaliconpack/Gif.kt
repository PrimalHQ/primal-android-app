package net.primal.android.core.compose.icons.primaliconpack

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp
import net.primal.android.core.compose.icons.PrimalIcons

val PrimalIcons.Gif: ImageVector
    get() {
        if (_Gif != null) {
            return _Gif!!
        }
        _Gif = ImageVector.Builder(
            name = "Gif",
            defaultWidth = 21.dp,
            defaultHeight = 20.dp,
            viewportWidth = 21f,
            viewportHeight = 20f
        ).apply {
            path(fill = SolidColor(Color(0xFFAAAAAA))) {
                moveTo(6.366f, 6.5f)
                curveTo(7.719f, 6.5f, 8.757f, 7.095f, 9.154f, 7.864f)
                curveTo(9.245f, 8.038f, 9.278f, 8.183f, 9.278f, 8.338f)
                curveTo(9.278f, 8.774f, 8.972f, 9.069f, 8.522f, 9.069f)
                curveTo(8.169f, 9.069f, 7.906f, 8.933f, 7.69f, 8.624f)
                curveTo(7.361f, 8.113f, 6.95f, 7.883f, 6.371f, 7.883f)
                curveTo(5.377f, 7.883f, 4.788f, 8.647f, 4.788f, 9.965f)
                curveTo(4.788f, 11.306f, 5.439f, 12.117f, 6.447f, 12.117f)
                curveTo(7.241f, 12.117f, 7.796f, 11.657f, 7.844f, 10.977f)
                lineTo(7.849f, 10.874f)
                horizontalLineTo(7.165f)
                curveTo(6.763f, 10.874f, 6.495f, 10.663f, 6.495f, 10.283f)
                curveTo(6.495f, 9.904f, 6.759f, 9.693f, 7.165f, 9.693f)
                horizontalLineTo(8.666f)
                curveTo(9.221f, 9.693f, 9.541f, 10.017f, 9.541f, 10.589f)
                verticalLineTo(10.636f)
                curveTo(9.541f, 12.3f, 8.417f, 13.5f, 6.409f, 13.5f)
                curveTo(4.281f, 13.5f, 3f, 12.211f, 3f, 9.988f)
                curveTo(3f, 7.794f, 4.281f, 6.5f, 6.366f, 6.5f)
                close()
            }
            path(fill = SolidColor(Color(0xFFAAAAAA))) {
                moveTo(11.291f, 6.514f)
                curveTo(11.845f, 6.514f, 12.166f, 6.837f, 12.166f, 7.409f)
                verticalLineTo(12.591f)
                curveTo(12.166f, 13.163f, 11.846f, 13.486f, 11.291f, 13.486f)
                curveTo(10.737f, 13.486f, 10.412f, 13.163f, 10.412f, 12.591f)
                verticalLineTo(7.409f)
                curveTo(10.412f, 6.838f, 10.737f, 6.514f, 11.291f, 6.514f)
                close()
            }
            path(fill = SolidColor(Color(0xFFAAAAAA))) {
                moveTo(17.221f, 6.617f)
                curveTo(17.694f, 6.617f, 18f, 6.871f, 18f, 7.312f)
                curveTo(18f, 7.752f, 17.684f, 8.005f, 17.221f, 8.005f)
                horizontalLineTo(14.968f)
                verticalLineTo(9.543f)
                horizontalLineTo(16.996f)
                curveTo(17.441f, 9.543f, 17.728f, 9.787f, 17.728f, 10.204f)
                curveTo(17.728f, 10.616f, 17.45f, 10.86f, 16.996f, 10.86f)
                horizontalLineTo(14.968f)
                verticalLineTo(12.591f)
                curveTo(14.968f, 13.163f, 14.647f, 13.486f, 14.093f, 13.486f)
                curveTo(13.538f, 13.486f, 13.214f, 13.162f, 13.214f, 12.591f)
                verticalLineTo(7.518f)
                curveTo(13.214f, 6.946f, 13.538f, 6.617f, 14.093f, 6.617f)
                horizontalLineTo(17.221f)
                close()
            }
            path(
                fill = SolidColor(Color(0xFFAAAAAA)),
                pathFillType = PathFillType.EvenOdd
            ) {
                moveTo(17f, 0f)
                curveTo(19.209f, 0f, 21f, 1.791f, 21f, 4f)
                verticalLineTo(16f)
                curveTo(21f, 18.14f, 19.319f, 19.888f, 17.206f, 19.995f)
                lineTo(17f, 20f)
                horizontalLineTo(4f)
                lineTo(3.794f, 19.995f)
                curveTo(1.749f, 19.891f, 0.109f, 18.251f, 0.005f, 16.206f)
                lineTo(0f, 16f)
                verticalLineTo(4f)
                curveTo(0f, 1.791f, 1.791f, 0f, 4f, 0f)
                horizontalLineTo(17f)
                close()
                moveTo(4f, 1.5f)
                curveTo(2.619f, 1.5f, 1.5f, 2.619f, 1.5f, 4f)
                verticalLineTo(16f)
                curveTo(1.5f, 17.381f, 2.619f, 18.5f, 4f, 18.5f)
                horizontalLineTo(17f)
                curveTo(18.381f, 18.5f, 19.5f, 17.381f, 19.5f, 16f)
                verticalLineTo(4f)
                curveTo(19.5f, 2.619f, 18.381f, 1.5f, 17f, 1.5f)
                horizontalLineTo(4f)
                close()
            }
        }.build()

        return _Gif!!
    }

@Suppress("ObjectPropertyName")
private var _Gif: ImageVector? = null
