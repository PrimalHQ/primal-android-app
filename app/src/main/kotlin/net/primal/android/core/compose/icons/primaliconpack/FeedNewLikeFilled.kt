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
            defaultWidth = 16.dp,
            defaultHeight = 16.dp,
            viewportWidth = 16f,
            viewportHeight = 16f
        ).apply {
            path(
                fill = SolidColor(Color(0xFFE52093)),
                pathFillType = PathFillType.EvenOdd
            ) {
                moveTo(3.192f, 10.144f)
                lineTo(3.191f, 10.143f)
                curveTo(3.182f, 10.134f, 3.169f, 10.123f, 3.153f, 10.107f)
                curveTo(3.122f, 10.077f, 3.077f, 10.033f, 3.023f, 9.978f)
                curveTo(2.914f, 9.867f, 2.765f, 9.707f, 2.596f, 9.507f)
                curveTo(2.262f, 9.111f, 1.838f, 8.542f, 1.512f, 7.87f)
                curveTo(1.188f, 7.201f, 0.939f, 6.388f, 1.013f, 5.522f)
                curveTo(1.089f, 4.635f, 1.499f, 3.765f, 2.362f, 3.003f)
                curveTo(3.211f, 2.253f, 4.065f, 1.952f, 4.883f, 2.006f)
                curveTo(5.683f, 2.059f, 6.355f, 2.447f, 6.87f, 2.884f)
                curveTo(7.299f, 3.248f, 7.647f, 3.669f, 7.905f, 4.03f)
                curveTo(8.163f, 3.669f, 8.511f, 3.248f, 8.939f, 2.884f)
                curveTo(9.455f, 2.447f, 10.126f, 2.059f, 10.927f, 2.006f)
                curveTo(11.745f, 1.952f, 12.598f, 2.253f, 13.447f, 3.003f)
                curveTo(14.277f, 3.736f, 14.739f, 4.512f, 14.916f, 5.293f)
                curveTo(15.092f, 6.071f, 14.97f, 6.786f, 14.749f, 7.374f)
                curveTo(14.529f, 7.957f, 14.204f, 8.431f, 13.943f, 8.754f)
                curveTo(13.811f, 8.917f, 13.692f, 9.046f, 13.604f, 9.136f)
                curveTo(13.559f, 9.181f, 13.523f, 9.216f, 13.496f, 9.242f)
                curveTo(13.486f, 9.251f, 13.476f, 9.26f, 13.469f, 9.267f)
                lineTo(8.264f, 14.63f)
                curveTo(8.067f, 14.833f, 7.742f, 14.833f, 7.546f, 14.63f)
                lineTo(3.192f, 10.144f)
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
