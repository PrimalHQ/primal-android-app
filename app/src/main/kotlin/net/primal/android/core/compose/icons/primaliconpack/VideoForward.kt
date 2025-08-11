package net.primal.android.core.compose.icons.primaliconpack

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val VideoForward: ImageVector
    get() {
        if (_VideoForward != null) {
            return _VideoForward!!
        }
        _VideoForward = ImageVector.Builder(
            name = "VideoForward",
            defaultWidth = 40.dp,
            defaultHeight = 40.dp,
            viewportWidth = 40f,
            viewportHeight = 40f
        ).apply {
            path(
                fill = SolidColor(Color.Black),
                fillAlpha = 0.25f
            ) {
                moveTo(20f, 20f)
                moveToRelative(-20f, 0f)
                arcToRelative(20f, 20f, 0f, isMoreThanHalf = true, isPositiveArc = true, 40f, 0f)
                arcToRelative(20f, 20f, 0f, isMoreThanHalf = true, isPositiveArc = true, -40f, 0f)
            }
            path(fill = SolidColor(Color.White)) {
                moveTo(18f, 4.25f)
                curveTo(18f, 4.051f, 18.223f, 3.932f, 18.389f, 4.042f)
                lineTo(23.688f, 7.575f)
                curveTo(23.836f, 7.674f, 23.836f, 7.892f, 23.688f, 7.991f)
                lineTo(18.389f, 11.524f)
                curveTo(18.223f, 11.634f, 18f, 11.515f, 18f, 11.316f)
                verticalLineTo(8.709f)
                curveTo(12.602f, 9.656f, 8.5f, 14.364f, 8.5f, 20.033f)
                curveTo(8.5f, 26.384f, 13.649f, 31.533f, 20f, 31.533f)
                curveTo(26.351f, 31.533f, 31.5f, 26.384f, 31.5f, 20.033f)
                curveTo(31.5f, 15.881f, 29.299f, 12.243f, 26.001f, 10.222f)
                curveTo(25.522f, 9.928f, 25.472f, 9.218f, 25.94f, 8.907f)
                curveTo(26.164f, 8.757f, 26.455f, 8.744f, 26.687f, 8.883f)
                curveTo(30.469f, 11.156f, 33f, 15.299f, 33f, 20.033f)
                curveTo(33f, 27.212f, 27.18f, 33.033f, 20f, 33.033f)
                curveTo(12.82f, 33.033f, 7f, 27.212f, 7f, 20.033f)
                curveTo(7f, 13.533f, 11.77f, 8.148f, 18f, 7.186f)
                verticalLineTo(4.25f)
                close()
            }
            path(fill = SolidColor(Color.White)) {
                moveTo(16.488f, 15.105f)
                curveTo(18.228f, 15.105f, 19.393f, 15.909f, 19.393f, 17.457f)
                curveTo(19.392f, 18.525f, 18.697f, 19.257f, 17.725f, 19.545f)
                curveTo(18.889f, 19.821f, 19.513f, 20.589f, 19.513f, 21.753f)
                curveTo(19.513f, 23.373f, 18.204f, 24.201f, 16.488f, 24.201f)
                curveTo(14.545f, 24.201f, 13.477f, 23.493f, 13.333f, 21.573f)
                horizontalLineTo(14.845f)
                curveTo(14.941f, 22.401f, 15.397f, 22.857f, 16.44f, 22.857f)
                curveTo(17.328f, 22.857f, 17.988f, 22.437f, 17.988f, 21.573f)
                curveTo(17.988f, 20.649f, 17.244f, 20.253f, 15.948f, 20.253f)
                horizontalLineTo(15.313f)
                verticalLineTo(18.933f)
                horizontalLineTo(15.948f)
                curveTo(16.872f, 18.933f, 17.869f, 18.716f, 17.869f, 17.66f)
                curveTo(17.869f, 16.881f, 17.329f, 16.449f, 16.465f, 16.449f)
                curveTo(15.577f, 16.449f, 15.073f, 16.917f, 14.977f, 17.733f)
                horizontalLineTo(13.465f)
                curveTo(13.549f, 16.149f, 14.473f, 15.105f, 16.488f, 15.105f)
                close()
            }
            path(
                fill = SolidColor(Color.White),
                pathFillType = PathFillType.EvenOdd
            ) {
                moveTo(23.6f, 15.105f)
                curveTo(25.916f, 15.105f, 26.732f, 16.701f, 26.732f, 19.653f)
                curveTo(26.732f, 22.593f, 25.916f, 24.201f, 23.6f, 24.201f)
                curveTo(21.284f, 24.2f, 20.468f, 22.593f, 20.468f, 19.653f)
                curveTo(20.468f, 16.701f, 21.284f, 15.105f, 23.6f, 15.105f)
                close()
                moveTo(23.6f, 16.449f)
                curveTo(22.388f, 16.449f, 21.992f, 17.445f, 21.992f, 19.653f)
                curveTo(21.992f, 21.848f, 22.388f, 22.857f, 23.6f, 22.857f)
                curveTo(24.812f, 22.857f, 25.208f, 21.849f, 25.208f, 19.653f)
                curveTo(25.208f, 17.445f, 24.812f, 16.449f, 23.6f, 16.449f)
                close()
            }
        }.build()

        return _VideoForward!!
    }

@Suppress("ObjectPropertyName")
private var _VideoForward: ImageVector? = null
