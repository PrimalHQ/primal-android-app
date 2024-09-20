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

val PrimalIcons.FeedNewLikeFilled: ImageVector
    get() {
        if (_FeedNewLikeFilled != null) {
            return _FeedNewLikeFilled!!
        }
        _FeedNewLikeFilled = ImageVector.Builder(
            name = "FeedNewLikeFilled",
            defaultWidth = 20.dp,
            defaultHeight = 19.dp,
            viewportWidth = 20f,
            viewportHeight = 19f
        ).apply {
            path(
                fill = SolidColor(Color(0xFFCA077C)),
                pathFillType = PathFillType.EvenOdd
            ) {
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

        return _FeedNewLikeFilled!!
    }

@Suppress("ObjectPropertyName")
private var _FeedNewLikeFilled: ImageVector? = null

@Preview(showBackground = true)
@Composable
private fun FeedNewLikeFilledPreview() {
    Box(modifier = Modifier.padding(12.dp)) {
        Image(imageVector = PrimalIcons.FeedNewLikeFilled, contentDescription = null)
    }
}
