package net.primal.android.core.compose.icons.primaliconpack

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp
import net.primal.android.core.compose.icons.PrimalIcons

val PrimalIcons.GenericLinkIcon: ImageVector
    get() {
        if (_GenericLinkIcon != null) {
            return _GenericLinkIcon!!
        }
        _GenericLinkIcon = ImageVector.Builder(
            name = "GenericLinkIcon",
            defaultWidth = 44.dp,
            defaultHeight = 44.dp,
            viewportWidth = 44f,
            viewportHeight = 44f
        ).apply {
            path(
                fill = SolidColor(Color(0xFF808080)),
                pathFillType = PathFillType.EvenOdd
            ) {
                moveTo(19.556f, 0f)
                curveTo(19.351f, 0f, 19.148f, 0.003f, 18.945f, 0.009f)
                curveTo(18.26f, 0.03f, 17.583f, 0.087f, 16.918f, 0.176f)
                curveTo(16.253f, 0.266f, 15.598f, 0.389f, 14.956f, 0.544f)
                curveTo(14.861f, 0.567f, 14.767f, 0.59f, 14.674f, 0.614f)
                curveTo(14.569f, 0.641f, 14.465f, 0.669f, 14.361f, 0.698f)
                curveTo(8.095f, 2.42f, 3.09f, 7.179f, 1.024f, 13.294f)
                curveTo(1.006f, 13.349f, 0.987f, 13.404f, 0.969f, 13.459f)
                curveTo(0.795f, 13.991f, 0.642f, 14.534f, 0.513f, 15.085f)
                curveTo(0.222f, 16.33f, 0.05f, 17.622f, 0.009f, 18.945f)
                lineTo(0.008f, 18.988f)
                curveTo(0.007f, 19.033f, 0.006f, 19.077f, 0.005f, 19.122f)
                lineTo(0.002f, 19.246f)
                curveTo(0.001f, 19.349f, 0f, 19.452f, 0f, 19.556f)
                curveTo(0f, 19.638f, 0.001f, 19.721f, 0.002f, 19.803f)
                curveTo(0.002f, 19.849f, 0.003f, 19.895f, 0.004f, 19.941f)
                curveTo(0.005f, 19.998f, 0.006f, 20.056f, 0.008f, 20.113f)
                lineTo(0.009f, 20.166f)
                curveTo(0.05f, 21.49f, 0.222f, 22.781f, 0.513f, 24.026f)
                curveTo(0.524f, 24.072f, 0.535f, 24.117f, 0.546f, 24.163f)
                curveTo(0.681f, 24.724f, 0.841f, 25.276f, 1.024f, 25.817f)
                curveTo(3.105f, 31.979f, 8.172f, 36.764f, 14.504f, 38.452f)
                curveTo(15.29f, 38.662f, 16.096f, 38.824f, 16.918f, 38.935f)
                curveTo(17.583f, 39.024f, 18.26f, 39.081f, 18.945f, 39.102f)
                curveTo(19.148f, 39.108f, 19.351f, 39.111f, 19.556f, 39.111f)
                curveTo(19.745f, 39.111f, 19.935f, 39.108f, 20.123f, 39.103f)
                lineTo(20.166f, 39.102f)
                curveTo(20.891f, 39.079f, 21.607f, 39.018f, 22.31f, 38.919f)
                curveTo(23.141f, 38.801f, 23.956f, 38.632f, 24.75f, 38.414f)
                curveTo(25.642f, 38.168f, 26.509f, 37.861f, 27.345f, 37.498f)
                curveTo(27.049f, 36.607f, 26.889f, 35.653f, 26.889f, 34.654f)
                verticalLineTo(32.497f)
                curveTo(26.889f, 31.182f, 27.549f, 30.105f, 28.499f, 29.45f)
                curveTo(28.047f, 28.797f, 27.677f, 28.084f, 27.406f, 27.323f)
                curveTo(25.308f, 27.685f, 23.04f, 27.908f, 20.661f, 27.961f)
                verticalLineTo(20.661f)
                horizontalLineTo(26.889f)
                verticalLineTo(19.124f)
                curveTo(26.889f, 18.897f, 26.897f, 18.672f, 26.913f, 18.45f)
                horizontalLineTo(20.661f)
                verticalLineTo(11.15f)
                curveTo(23.18f, 11.207f, 25.575f, 11.453f, 27.775f, 11.854f)
                curveTo(27.952f, 12.673f, 28.102f, 13.523f, 28.222f, 14.399f)
                curveTo(29.211f, 12.791f, 30.686f, 11.505f, 32.431f, 10.703f)
                curveTo(31.528f, 10.427f, 30.581f, 10.18f, 29.596f, 9.964f)
                curveTo(29.01f, 7.736f, 28.225f, 5.724f, 27.279f, 4.021f)
                curveTo(30.06f, 5.406f, 32.418f, 7.517f, 34.103f, 10.106f)
                curveTo(34.871f, 9.905f, 35.674f, 9.793f, 36.496f, 9.779f)
                curveTo(33.955f, 5.386f, 29.754f, 2.073f, 24.75f, 0.698f)
                curveTo(24.702f, 0.684f, 24.655f, 0.672f, 24.607f, 0.659f)
                curveTo(24.085f, 0.52f, 23.555f, 0.402f, 23.017f, 0.306f)
                curveTo(22.745f, 0.257f, 22.47f, 0.214f, 22.194f, 0.176f)
                curveTo(21.528f, 0.087f, 20.851f, 0.03f, 20.166f, 0.009f)
                curveTo(19.964f, 0.003f, 19.761f, 0f, 19.557f, 0f)
                horizontalLineTo(19.556f)
                close()
                moveTo(13.493f, 5.604f)
                curveTo(14.091f, 4.439f, 14.738f, 3.476f, 15.407f, 2.71f)
                curveTo(16.391f, 2.468f, 17.408f, 2.311f, 18.45f, 2.245f)
                verticalLineTo(8.939f)
                curveTo(16.179f, 8.988f, 13.994f, 9.184f, 11.94f, 9.509f)
                curveTo(12.376f, 8.067f, 12.9f, 6.755f, 13.493f, 5.604f)
                close()
                moveTo(20.661f, 8.939f)
                verticalLineTo(2.245f)
                curveTo(21.703f, 2.311f, 22.72f, 2.468f, 23.704f, 2.71f)
                curveTo(24.374f, 3.476f, 25.02f, 4.439f, 25.619f, 5.604f)
                curveTo(26.211f, 6.755f, 26.735f, 8.067f, 27.171f, 9.509f)
                curveTo(25.117f, 9.184f, 22.932f, 8.988f, 20.661f, 8.939f)
                close()
                moveTo(9.515f, 9.964f)
                curveTo(7.555f, 10.394f, 5.746f, 10.946f, 4.138f, 11.601f)
                curveTo(5.829f, 8.329f, 8.532f, 5.665f, 11.832f, 4.021f)
                curveTo(10.886f, 5.724f, 10.101f, 7.736f, 9.515f, 9.964f)
                close()
                moveTo(10.559f, 18.45f)
                curveTo(10.624f, 16.108f, 10.897f, 13.887f, 11.336f, 11.854f)
                curveTo(13.537f, 11.453f, 15.931f, 11.207f, 18.45f, 11.15f)
                verticalLineTo(18.45f)
                horizontalLineTo(10.559f)
                close()
                moveTo(4.258f, 13.953f)
                curveTo(5.638f, 13.336f, 7.227f, 12.797f, 8.98f, 12.36f)
                curveTo(8.622f, 14.278f, 8.403f, 16.323f, 8.347f, 18.45f)
                horizontalLineTo(2.245f)
                curveTo(2.329f, 17.123f, 2.561f, 15.837f, 2.926f, 14.608f)
                curveTo(3.342f, 14.383f, 3.786f, 14.165f, 4.258f, 13.953f)
                close()
                moveTo(2.926f, 24.503f)
                curveTo(2.561f, 23.274f, 2.329f, 21.988f, 2.245f, 20.661f)
                horizontalLineTo(8.347f)
                curveTo(8.403f, 22.788f, 8.622f, 24.833f, 8.98f, 26.751f)
                curveTo(7.227f, 26.315f, 5.638f, 25.775f, 4.258f, 25.158f)
                curveTo(3.786f, 24.947f, 3.342f, 24.728f, 2.926f, 24.503f)
                close()
                moveTo(11.336f, 27.257f)
                curveTo(10.897f, 25.224f, 10.624f, 23.003f, 10.559f, 20.661f)
                horizontalLineTo(18.45f)
                verticalLineTo(27.961f)
                curveTo(15.931f, 27.905f, 13.537f, 27.658f, 11.336f, 27.257f)
                close()
                moveTo(11.832f, 35.09f)
                curveTo(8.532f, 33.447f, 5.83f, 30.782f, 4.138f, 27.51f)
                curveTo(5.746f, 28.165f, 7.555f, 28.717f, 9.515f, 29.147f)
                curveTo(10.101f, 31.375f, 10.886f, 33.387f, 11.832f, 35.09f)
                close()
                moveTo(13.493f, 33.507f)
                curveTo(12.9f, 32.356f, 12.376f, 31.044f, 11.94f, 29.602f)
                curveTo(13.994f, 29.927f, 16.179f, 30.123f, 18.45f, 30.172f)
                verticalLineTo(36.866f)
                curveTo(17.408f, 36.8f, 16.391f, 36.643f, 15.407f, 36.401f)
                curveTo(14.738f, 35.635f, 14.091f, 34.672f, 13.493f, 33.507f)
                close()
                moveTo(20.661f, 36.866f)
                verticalLineTo(30.172f)
                curveTo(22.932f, 30.123f, 25.117f, 29.927f, 27.171f, 29.602f)
                curveTo(26.735f, 31.044f, 26.211f, 32.356f, 25.619f, 33.507f)
                curveTo(25.02f, 34.672f, 24.374f, 35.635f, 23.704f, 36.401f)
                curveTo(22.72f, 36.643f, 21.703f, 36.8f, 20.661f, 36.866f)
                close()
            }
            path(fill = SolidColor(Color(0xFF808080))) {
                moveTo(44f, 21.281f)
                curveTo(44f, 21.996f, 43.384f, 22.575f, 42.625f, 22.575f)
                curveTo(41.866f, 22.575f, 41.25f, 21.996f, 41.25f, 21.281f)
                verticalLineTo(19.124f)
                curveTo(41.25f, 16.742f, 39.198f, 14.811f, 36.667f, 14.811f)
                curveTo(34.135f, 14.811f, 32.083f, 16.742f, 32.083f, 19.124f)
                verticalLineTo(24.301f)
                curveTo(32.083f, 26.683f, 34.135f, 28.614f, 36.667f, 28.614f)
                curveTo(37.197f, 28.614f, 37.591f, 28.53f, 37.917f, 28.374f)
                curveTo(38.545f, 28.073f, 39.417f, 28.294f, 39.417f, 28.957f)
                verticalLineTo(29.533f)
                curveTo(39.417f, 29.995f, 39.157f, 30.426f, 38.736f, 30.662f)
                curveTo(38.115f, 31.01f, 37.677f, 31.203f, 36.667f, 31.203f)
                curveTo(32.617f, 31.203f, 29.333f, 28.112f, 29.333f, 24.301f)
                verticalLineTo(19.124f)
                curveTo(29.333f, 15.312f, 32.617f, 12.222f, 36.667f, 12.222f)
                curveTo(40.717f, 12.222f, 44f, 15.312f, 44f, 19.124f)
                verticalLineTo(21.281f)
                close()
            }
            path(fill = SolidColor(Color(0xFF808080))) {
                moveTo(29.333f, 32.497f)
                curveTo(29.333f, 31.782f, 29.949f, 31.203f, 30.708f, 31.203f)
                curveTo(31.468f, 31.203f, 32.083f, 31.782f, 32.083f, 32.497f)
                verticalLineTo(34.654f)
                curveTo(32.083f, 37.036f, 34.135f, 38.967f, 36.667f, 38.967f)
                curveTo(39.198f, 38.967f, 41.25f, 37.036f, 41.25f, 34.654f)
                verticalLineTo(29.477f)
                curveTo(41.25f, 27.095f, 39.198f, 25.163f, 36.667f, 25.163f)
                curveTo(36.136f, 25.163f, 35.742f, 25.248f, 35.416f, 25.404f)
                curveTo(34.789f, 25.705f, 33.917f, 25.484f, 33.917f, 24.821f)
                lineTo(33.917f, 24.245f)
                curveTo(33.917f, 23.783f, 34.176f, 23.352f, 34.598f, 23.115f)
                curveTo(35.218f, 22.767f, 35.656f, 22.575f, 36.667f, 22.575f)
                curveTo(40.717f, 22.575f, 44f, 25.665f, 44f, 29.477f)
                verticalLineTo(34.654f)
                curveTo(44f, 38.465f, 40.717f, 41.556f, 36.667f, 41.556f)
                curveTo(32.617f, 41.556f, 29.333f, 38.465f, 29.333f, 34.654f)
                verticalLineTo(32.497f)
                close()
            }
        }.build()

        return _GenericLinkIcon!!
    }

@Suppress("ObjectPropertyName")
private var _GenericLinkIcon: ImageVector? = null
