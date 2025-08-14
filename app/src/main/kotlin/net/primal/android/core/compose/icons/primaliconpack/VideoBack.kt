package net.primal.android.core.compose.icons.primaliconpack

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp
import net.primal.android.core.compose.icons.PrimalIcons

val PrimalIcons.VideoBack: ImageVector
    get() {
        if (_VideoBack != null) {
            return _VideoBack!!
        }
        _VideoBack = ImageVector.Builder(
            name = "VideoBack",
            defaultWidth = 40.dp,
            defaultHeight = 40.dp,
            viewportWidth = 40f,
            viewportHeight = 40f,
        ).apply {
            path(
                fill = SolidColor(Color.Black),
                fillAlpha = 0.25f,
            ) {
                moveTo(20f, 20f)
                moveToRelative(-20f, 0f)
                arcToRelative(20f, 20f, 0f, isMoreThanHalf = true, isPositiveArc = true, 40f, 0f)
                arcToRelative(20f, 20f, 0f, isMoreThanHalf = true, isPositiveArc = true, -40f, 0f)
            }
            path(fill = SolidColor(Color.White)) {
                moveTo(21.611f, 4.042f)
                curveTo(21.777f, 3.932f, 22f, 4.051f, 22f, 4.25f)
                verticalLineTo(7.186f)
                curveTo(28.23f, 8.148f, 33f, 13.533f, 33f, 20.033f)
                curveTo(33f, 27.212f, 27.18f, 33.033f, 20f, 33.033f)
                curveTo(12.82f, 33.033f, 7f, 27.212f, 7f, 20.033f)
                curveTo(7f, 15.299f, 9.531f, 11.156f, 13.314f, 8.883f)
                curveTo(13.545f, 8.744f, 13.836f, 8.757f, 14.061f, 8.907f)
                curveTo(14.528f, 9.218f, 14.477f, 9.928f, 13.999f, 10.222f)
                curveTo(10.701f, 12.243f, 8.5f, 15.881f, 8.5f, 20.033f)
                curveTo(8.5f, 26.384f, 13.649f, 31.533f, 20f, 31.533f)
                curveTo(26.351f, 31.533f, 31.5f, 26.384f, 31.5f, 20.033f)
                curveTo(31.5f, 14.364f, 27.397f, 9.656f, 22f, 8.709f)
                verticalLineTo(11.316f)
                curveTo(22f, 11.515f, 21.777f, 11.634f, 21.611f, 11.524f)
                lineTo(16.313f, 7.991f)
                curveTo(16.164f, 7.892f, 16.164f, 7.674f, 16.313f, 7.575f)
                lineTo(21.611f, 4.042f)
                close()
            }
            path(fill = SolidColor(Color.White)) {
                moveTo(25.396f, 16.593f)
                horizontalLineTo(21.52f)
                lineTo(21.172f, 19.221f)
                curveTo(21.508f, 18.477f, 22.168f, 18.273f, 23.008f, 18.273f)
                curveTo(24.604f, 18.273f, 25.72f, 19.257f, 25.72f, 21.189f)
                curveTo(25.72f, 22.881f, 24.616f, 24.201f, 22.672f, 24.201f)
                curveTo(20.668f, 24.201f, 19.684f, 23.409f, 19.54f, 21.693f)
                horizontalLineTo(21.065f)
                curveTo(21.184f, 22.437f, 21.616f, 22.857f, 22.588f, 22.857f)
                curveTo(23.584f, 22.857f, 24.196f, 22.245f, 24.196f, 21.225f)
                curveTo(24.196f, 20.085f, 23.5f, 19.617f, 22.648f, 19.617f)
                curveTo(21.88f, 19.617f, 21.4f, 19.893f, 21.112f, 20.457f)
                horizontalLineTo(19.648f)
                lineTo(20.248f, 15.273f)
                horizontalLineTo(25.396f)
                verticalLineTo(16.593f)
                close()
            }
            path(fill = SolidColor(Color.White)) {
                moveTo(17.024f, 24.033f)
                horizontalLineTo(15.537f)
                verticalLineTo(17.061f)
                horizontalLineTo(13.641f)
                verticalLineTo(16.197f)
                lineTo(15.837f, 15.249f)
                horizontalLineTo(17.024f)
                verticalLineTo(24.033f)
                close()
            }
        }.build()

        return _VideoBack!!
    }

@Suppress("ObjectPropertyName")
private var _VideoBack: ImageVector? = null
