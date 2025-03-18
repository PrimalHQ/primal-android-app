package net.primal.android.core.compose.icons.primaliconpack

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp
import net.primal.android.core.compose.icons.PrimalIcons

val PrimalIcons.VerifiedFilled: ImageVector
    get() {
        if (_VerifiedFilled != null) {
            return _VerifiedFilled!!
        }
        _VerifiedFilled = ImageVector.Builder(
            name = "VerifiedFilled",
            defaultWidth = 64.dp,
            defaultHeight = 60.dp,
            viewportWidth = 64f,
            viewportHeight = 60f
        ).apply {
            path(
                fill = Brush.linearGradient(
                    colorStops = arrayOf(
                        0f to Color(0xFFEF404A),
                        1f to Color(0xFF5B12A4)
                    ),
                    start = Offset(8.764f, -0.415f),
                    end = Offset(56.841f, 63.574f)
                )
            ) {
                moveTo(47.8f, 9.565f)
                curveTo(47.41f, 9.504f, 47.072f, 9.261f, 46.89f, 8.91f)
                lineTo(42.619f, 0.67f)
                curveTo(42.307f, 0.068f, 41.57f, -0.172f, 40.965f, 0.131f)
                lineTo(32.551f, 4.345f)
                curveTo(32.202f, 4.52f, 31.792f, 4.52f, 31.443f, 4.345f)
                lineTo(23.038f, 0.135f)
                curveTo(22.432f, -0.168f, 21.696f, 0.072f, 21.384f, 0.673f)
                lineTo(17.114f, 8.911f)
                curveTo(16.933f, 9.262f, 16.595f, 9.505f, 16.205f, 9.565f)
                lineTo(6.914f, 11.013f)
                curveTo(6.235f, 11.119f, 5.772f, 11.758f, 5.881f, 12.438f)
                lineTo(7.345f, 21.525f)
                curveTo(7.409f, 21.923f, 7.276f, 22.327f, 6.989f, 22.609f)
                lineTo(0.371f, 29.116f)
                curveTo(-0.124f, 29.603f, -0.124f, 30.401f, 0.371f, 30.888f)
                lineTo(6.99f, 37.394f)
                curveTo(7.277f, 37.676f, 7.41f, 38.08f, 7.346f, 38.478f)
                lineTo(5.881f, 47.565f)
                curveTo(5.772f, 48.244f, 6.235f, 48.883f, 6.914f, 48.989f)
                lineTo(16.21f, 50.435f)
                curveTo(16.6f, 50.496f, 16.938f, 50.739f, 17.12f, 51.09f)
                lineTo(21.388f, 59.328f)
                curveTo(21.7f, 59.93f, 22.437f, 60.17f, 23.042f, 59.867f)
                lineTo(31.443f, 55.657f)
                curveTo(31.792f, 55.482f, 32.202f, 55.482f, 32.551f, 55.657f)
                lineTo(40.96f, 59.869f)
                curveTo(41.565f, 60.172f, 42.302f, 59.932f, 42.614f, 59.33f)
                lineTo(46.885f, 51.09f)
                curveTo(47.067f, 50.739f, 47.404f, 50.496f, 47.794f, 50.435f)
                lineTo(57.085f, 48.989f)
                curveTo(57.764f, 48.884f, 58.228f, 48.245f, 58.118f, 47.565f)
                lineTo(56.655f, 38.478f)
                curveTo(56.591f, 38.08f, 56.724f, 37.676f, 57.011f, 37.394f)
                lineTo(63.629f, 30.887f)
                curveTo(64.124f, 30.401f, 64.124f, 29.602f, 63.629f, 29.115f)
                lineTo(57.01f, 22.609f)
                curveTo(56.723f, 22.327f, 56.59f, 21.923f, 56.654f, 21.525f)
                lineTo(58.12f, 12.435f)
                curveTo(58.23f, 11.755f, 57.767f, 11.116f, 57.087f, 11.01f)
                lineTo(47.8f, 9.565f)
                close()
            }
            path(fill = SolidColor(Color(0xFFCA077C))) {
                moveTo(47.8f, 9.565f)
                curveTo(47.41f, 9.504f, 47.072f, 9.261f, 46.89f, 8.91f)
                lineTo(42.619f, 0.67f)
                curveTo(42.307f, 0.068f, 41.57f, -0.172f, 40.965f, 0.131f)
                lineTo(32.551f, 4.345f)
                curveTo(32.202f, 4.52f, 31.792f, 4.52f, 31.443f, 4.345f)
                lineTo(23.038f, 0.135f)
                curveTo(22.432f, -0.168f, 21.696f, 0.072f, 21.384f, 0.673f)
                lineTo(17.114f, 8.911f)
                curveTo(16.933f, 9.262f, 16.595f, 9.505f, 16.205f, 9.565f)
                lineTo(6.914f, 11.013f)
                curveTo(6.235f, 11.119f, 5.772f, 11.758f, 5.881f, 12.438f)
                lineTo(7.345f, 21.525f)
                curveTo(7.409f, 21.923f, 7.276f, 22.327f, 6.989f, 22.609f)
                lineTo(0.371f, 29.116f)
                curveTo(-0.124f, 29.603f, -0.124f, 30.401f, 0.371f, 30.888f)
                lineTo(6.99f, 37.394f)
                curveTo(7.277f, 37.676f, 7.41f, 38.08f, 7.346f, 38.478f)
                lineTo(5.881f, 47.565f)
                curveTo(5.772f, 48.244f, 6.235f, 48.883f, 6.914f, 48.989f)
                lineTo(16.21f, 50.435f)
                curveTo(16.6f, 50.496f, 16.938f, 50.739f, 17.12f, 51.09f)
                lineTo(21.388f, 59.328f)
                curveTo(21.7f, 59.93f, 22.437f, 60.17f, 23.042f, 59.867f)
                lineTo(31.443f, 55.657f)
                curveTo(31.792f, 55.482f, 32.202f, 55.482f, 32.551f, 55.657f)
                lineTo(40.96f, 59.869f)
                curveTo(41.565f, 60.172f, 42.302f, 59.932f, 42.614f, 59.33f)
                lineTo(46.885f, 51.09f)
                curveTo(47.067f, 50.739f, 47.404f, 50.496f, 47.794f, 50.435f)
                lineTo(57.085f, 48.989f)
                curveTo(57.764f, 48.884f, 58.228f, 48.245f, 58.118f, 47.565f)
                lineTo(56.655f, 38.478f)
                curveTo(56.591f, 38.08f, 56.724f, 37.676f, 57.011f, 37.394f)
                lineTo(63.629f, 30.887f)
                curveTo(64.124f, 30.401f, 64.124f, 29.602f, 63.629f, 29.115f)
                lineTo(57.01f, 22.609f)
                curveTo(56.723f, 22.327f, 56.59f, 21.923f, 56.654f, 21.525f)
                lineTo(58.12f, 12.435f)
                curveTo(58.23f, 11.755f, 57.767f, 11.116f, 57.087f, 11.01f)
                lineTo(47.8f, 9.565f)
                close()
            }
            path(fill = SolidColor(Color(0xFFFFFFFF))) {
                moveTo(26.257f, 44.192f)
                lineTo(17.865f, 35.631f)
                curveTo(16.682f, 34.425f, 16.682f, 32.492f, 17.865f, 31.287f)
                curveTo(19.077f, 30.049f, 21.067f, 30.049f, 22.28f, 31.287f)
                lineTo(26.578f, 35.671f)
                lineTo(41.72f, 20.226f)
                curveTo(42.933f, 18.989f, 44.923f, 18.989f, 46.135f, 20.226f)
                curveTo(47.318f, 21.432f, 47.318f, 23.365f, 46.135f, 24.571f)
                lineTo(26.899f, 44.192f)
                curveTo(26.723f, 44.371f, 26.434f, 44.371f, 26.257f, 44.192f)
                close()
            }
        }.build()

        return _VerifiedFilled!!
    }

@Suppress("ObjectPropertyName")
private var _VerifiedFilled: ImageVector? = null
