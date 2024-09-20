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

val PrimalIcons.FeedNewReposts: ImageVector
    get() {
        if (_FeedNewReposts != null) {
            return _FeedNewReposts!!
        }
        _FeedNewReposts = ImageVector.Builder(
            name = "FeedNewReposts",
            defaultWidth = 20.dp,
            defaultHeight = 20.dp,
            viewportWidth = 20f,
            viewportHeight = 20f
        ).apply {
            path(fill = SolidColor(Color(0xFF666666))) {
                moveTo(6.378f, 6.996f)
                curveTo(6.689f, 7.246f, 7.15f, 7.023f, 7.15f, 6.623f)
                verticalLineTo(4.754f)
                horizontalLineTo(14.75f)
                curveTo(16.324f, 4.754f, 17.6f, 6.035f, 17.6f, 7.616f)
                verticalLineTo(11.197f)
                curveTo(17.6f, 11.521f, 17.546f, 11.833f, 17.447f, 12.124f)
                curveTo(17.374f, 12.337f, 17.423f, 12.581f, 17.598f, 12.722f)
                lineTo(18.381f, 13.35f)
                curveTo(18.604f, 13.53f, 18.935f, 13.472f, 19.056f, 13.212f)
                curveTo(19.341f, 12.6f, 19.5f, 11.917f, 19.5f, 11.197f)
                verticalLineTo(7.616f)
                curveTo(19.5f, 4.982f, 17.373f, 2.847f, 14.75f, 2.847f)
                horizontalLineTo(7.15f)
                verticalLineTo(0.978f)
                curveTo(7.15f, 0.578f, 6.689f, 0.356f, 6.378f, 0.605f)
                lineTo(2.864f, 3.428f)
                curveTo(2.626f, 3.619f, 2.626f, 3.982f, 2.864f, 4.173f)
                lineTo(6.378f, 6.996f)
                close()
            }
            path(fill = SolidColor(Color(0xFF666666))) {
                moveTo(1.619f, 6.65f)
                curveTo(1.396f, 6.47f, 1.065f, 6.528f, 0.944f, 6.788f)
                curveTo(0.659f, 7.4f, 0.5f, 8.083f, 0.5f, 8.803f)
                verticalLineTo(12.384f)
                curveTo(0.5f, 15.018f, 2.627f, 17.153f, 5.25f, 17.153f)
                horizontalLineTo(12.85f)
                verticalLineTo(19.022f)
                curveTo(12.85f, 19.422f, 13.311f, 19.644f, 13.622f, 19.395f)
                lineTo(17.136f, 16.572f)
                curveTo(17.374f, 16.381f, 17.374f, 16.018f, 17.136f, 15.827f)
                lineTo(13.622f, 13.004f)
                curveTo(13.311f, 12.754f, 12.85f, 12.977f, 12.85f, 13.377f)
                verticalLineTo(15.246f)
                horizontalLineTo(5.25f)
                curveTo(3.676f, 15.246f, 2.4f, 13.965f, 2.4f, 12.384f)
                verticalLineTo(8.803f)
                curveTo(2.4f, 8.478f, 2.454f, 8.166f, 2.553f, 7.876f)
                curveTo(2.626f, 7.663f, 2.577f, 7.419f, 2.402f, 7.278f)
                lineTo(1.619f, 6.65f)
                close()
            }
        }.build()

        return _FeedNewReposts!!
    }

@Suppress("ObjectPropertyName")
private var _FeedNewReposts: ImageVector? = null

@Preview(showBackground = true)
@Composable
private fun FeedNewRepostsPreview() {
    Box(modifier = Modifier.padding(12.dp)) {
        Image(imageVector = PrimalIcons.FeedNewReposts, contentDescription = null)
    }
}
