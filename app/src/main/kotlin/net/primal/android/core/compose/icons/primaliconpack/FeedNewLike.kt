package net.primal.android.core.compose.icons.primaliconpack

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import net.primal.android.core.compose.icons.PrimalIcons

val PrimalIcons.FeedNewLike: ImageVector
    get() {
        if (_FeedNewLike != null) {
            return _FeedNewLike!!
        }
        _FeedNewLike = ImageVector.Builder(
            name = "FeedNewLike",
            defaultWidth = 20.dp,
            defaultHeight = 19.dp,
            viewportWidth = 20f,
            viewportHeight = 19f
        ).apply {
            path(
                fill = SolidColor(Color(0xFF666666)),
                pathFillType = PathFillType.EvenOdd
            ) {
                moveTo(10.682f, 5.368f)
                lineTo(10.41f, 5.906f)
                curveTo(10.185f, 6.35f, 9.543f, 6.35f, 9.318f, 5.906f)
                lineTo(9.046f, 5.369f)
                lineTo(9.042f, 5.361f)
                curveTo(9.038f, 5.353f, 9.031f, 5.34f, 9.021f, 5.323f)
                curveTo(9.002f, 5.287f, 8.972f, 5.232f, 8.931f, 5.162f)
                curveTo(8.849f, 5.022f, 8.726f, 4.821f, 8.565f, 4.59f)
                curveTo(8.24f, 4.122f, 7.781f, 3.557f, 7.223f, 3.103f)
                curveTo(6.663f, 2.648f, 6.059f, 2.35f, 5.429f, 2.31f)
                curveTo(4.824f, 2.272f, 4.06f, 2.462f, 3.137f, 3.242f)
                curveTo(2.235f, 4.005f, 1.905f, 4.771f, 1.842f, 5.476f)
                curveTo(1.777f, 6.21f, 1.994f, 6.979f, 2.369f, 7.72f)
                curveTo(2.742f, 8.456f, 3.243f, 9.107f, 3.661f, 9.581f)
                curveTo(3.868f, 9.816f, 4.05f, 10.002f, 4.179f, 10.127f)
                curveTo(4.243f, 10.19f, 4.294f, 10.238f, 4.327f, 10.268f)
                curveTo(4.343f, 10.283f, 4.356f, 10.295f, 4.363f, 10.301f)
                lineTo(4.37f, 10.308f)
                lineTo(4.371f, 10.309f)
                lineTo(4.39f, 10.325f)
                lineTo(9.513f, 15.378f)
                curveTo(9.708f, 15.57f, 10.02f, 15.57f, 10.215f, 15.378f)
                lineTo(16.564f, 9.116f)
                lineTo(16.588f, 9.095f)
                lineTo(16.589f, 9.094f)
                lineTo(16.592f, 9.092f)
                lineTo(16.615f, 9.072f)
                curveTo(16.637f, 9.052f, 16.672f, 9.019f, 16.718f, 8.975f)
                curveTo(16.809f, 8.886f, 16.938f, 8.752f, 17.084f, 8.58f)
                curveTo(17.378f, 8.232f, 17.715f, 7.751f, 17.935f, 7.194f)
                curveTo(18.152f, 6.644f, 18.246f, 6.042f, 18.097f, 5.413f)
                curveTo(17.949f, 4.789f, 17.541f, 4.045f, 16.591f, 3.242f)
                curveTo(15.668f, 2.462f, 14.904f, 2.272f, 14.299f, 2.31f)
                curveTo(13.669f, 2.35f, 13.065f, 2.648f, 12.505f, 3.103f)
                curveTo(11.947f, 3.557f, 11.488f, 4.122f, 11.162f, 4.59f)
                curveTo(11.002f, 4.821f, 10.879f, 5.022f, 10.797f, 5.162f)
                curveTo(10.756f, 5.232f, 10.726f, 5.287f, 10.707f, 5.322f)
                curveTo(10.697f, 5.34f, 10.69f, 5.353f, 10.686f, 5.361f)
                lineTo(10.682f, 5.368f)
                close()
                moveTo(3.131f, 11.637f)
                lineTo(3.13f, 11.636f)
                curveTo(3.117f, 11.624f, 3.099f, 11.608f, 3.076f, 11.587f)
                curveTo(3.031f, 11.545f, 2.968f, 11.486f, 2.89f, 11.41f)
                curveTo(2.735f, 11.258f, 2.521f, 11.039f, 2.28f, 10.766f)
                curveTo(1.802f, 10.225f, 1.197f, 9.446f, 0.732f, 8.527f)
                curveTo(0.268f, 7.613f, -0.087f, 6.5f, 0.019f, 5.317f)
                curveTo(0.127f, 4.103f, 0.713f, 2.914f, 1.946f, 1.871f)
                curveTo(3.159f, 0.846f, 4.378f, 0.434f, 5.547f, 0.508f)
                curveTo(6.69f, 0.581f, 7.65f, 1.111f, 8.386f, 1.709f)
                curveTo(8.999f, 2.207f, 9.495f, 2.782f, 9.864f, 3.276f)
                curveTo(10.233f, 2.782f, 10.729f, 2.207f, 11.342f, 1.709f)
                curveTo(12.078f, 1.111f, 13.038f, 0.581f, 14.181f, 0.508f)
                curveTo(15.35f, 0.434f, 16.569f, 0.846f, 17.782f, 1.871f)
                curveTo(18.968f, 2.873f, 19.627f, 3.935f, 19.88f, 5.003f)
                curveTo(20.131f, 6.067f, 19.958f, 7.045f, 19.641f, 7.849f)
                curveTo(19.327f, 8.646f, 18.863f, 9.295f, 18.49f, 9.736f)
                curveTo(18.302f, 9.959f, 18.131f, 10.135f, 18.005f, 10.258f)
                curveTo(17.942f, 10.32f, 17.89f, 10.368f, 17.851f, 10.403f)
                curveTo(17.836f, 10.417f, 17.823f, 10.428f, 17.813f, 10.437f)
                lineTo(10.215f, 17.931f)
                curveTo(10.02f, 18.124f, 9.708f, 18.124f, 9.513f, 17.931f)
                lineTo(3.131f, 11.637f)
                close()
            }
        }.build()

        return _FeedNewLike!!
    }

@Suppress("ObjectPropertyName")
private var _FeedNewLike: ImageVector? = null

@Preview(showBackground = true)
@Composable
private fun FeedNewLikePreview() {
    Box(modifier = Modifier.padding(12.dp)) {
        Image(imageVector = PrimalIcons.FeedNewLike, contentDescription = null)
    }
}
