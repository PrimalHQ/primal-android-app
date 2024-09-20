package net.primal.android.core.compose.icons.primaliconpack

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import net.primal.android.core.compose.icons.PrimalIcons

val PrimalIcons.FeedNewRepostsFilled: ImageVector
    get() {
        if (_FeedNewRepostsFilled != null) {
            return _FeedNewRepostsFilled!!
        }
        _FeedNewRepostsFilled = ImageVector.Builder(
            name = "FeedNewRepostsFilled",
            defaultWidth = 22.dp,
            defaultHeight = 22.dp,
            viewportWidth = 22f,
            viewportHeight = 22f
        ).apply {
            path(fill = SolidColor(Color(0xFF52CE0A))) {
                moveTo(7.378f, 7.996f)
                curveTo(7.689f, 8.246f, 8.15f, 8.023f, 8.15f, 7.623f)
                verticalLineTo(5.754f)
                horizontalLineTo(15.75f)
                curveTo(17.324f, 5.754f, 18.6f, 7.035f, 18.6f, 8.616f)
                verticalLineTo(12.197f)
                curveTo(18.6f, 12.521f, 18.546f, 12.833f, 18.447f, 13.124f)
                curveTo(18.374f, 13.337f, 18.423f, 13.581f, 18.598f, 13.722f)
                lineTo(19.381f, 14.35f)
                curveTo(19.604f, 14.53f, 19.935f, 14.472f, 20.056f, 14.212f)
                curveTo(20.341f, 13.6f, 20.5f, 12.917f, 20.5f, 12.197f)
                verticalLineTo(8.616f)
                curveTo(20.5f, 5.982f, 18.373f, 3.847f, 15.75f, 3.847f)
                horizontalLineTo(8.15f)
                verticalLineTo(1.978f)
                curveTo(8.15f, 1.578f, 7.689f, 1.356f, 7.378f, 1.605f)
                lineTo(3.864f, 4.428f)
                curveTo(3.626f, 4.619f, 3.626f, 4.982f, 3.864f, 5.173f)
                lineTo(7.378f, 7.996f)
                close()
            }
            path(fill = SolidColor(Color(0xFF52CE0A))) {
                moveTo(2.619f, 7.65f)
                curveTo(2.396f, 7.47f, 2.065f, 7.528f, 1.944f, 7.788f)
                curveTo(1.659f, 8.4f, 1.5f, 9.083f, 1.5f, 9.803f)
                verticalLineTo(13.384f)
                curveTo(1.5f, 16.018f, 3.627f, 18.153f, 6.25f, 18.153f)
                horizontalLineTo(13.85f)
                verticalLineTo(20.022f)
                curveTo(13.85f, 20.422f, 14.311f, 20.644f, 14.622f, 20.395f)
                lineTo(18.136f, 17.572f)
                curveTo(18.374f, 17.381f, 18.374f, 17.018f, 18.136f, 16.827f)
                lineTo(14.622f, 14.004f)
                curveTo(14.311f, 13.754f, 13.85f, 13.977f, 13.85f, 14.377f)
                verticalLineTo(16.246f)
                horizontalLineTo(6.25f)
                curveTo(4.676f, 16.246f, 3.4f, 14.965f, 3.4f, 13.384f)
                verticalLineTo(9.803f)
                curveTo(3.4f, 9.478f, 3.454f, 9.166f, 3.553f, 8.876f)
                curveTo(3.626f, 8.663f, 3.577f, 8.419f, 3.402f, 8.278f)
                lineTo(2.619f, 7.65f)
                close()
            }
        }.build()

        return _FeedNewRepostsFilled!!
    }

@Suppress("ObjectPropertyName")
private var _FeedNewRepostsFilled: ImageVector? = null

@Preview(showBackground = true)
@Composable
private fun FeedNewRepostsFilledPreview() {
    Box(modifier = Modifier.padding(12.dp)) {
        Image(imageVector = PrimalIcons.FeedNewRepostsFilled, contentDescription = null)
    }
}
