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
import kotlin.Suppress
import net.primal.android.core.compose.icons.PrimalIcons

val PrimalIcons.FeedBookmark: ImageVector
    get() {
        if (_FeedBookmark != null) {
            return _FeedBookmark!!
        }
        _FeedBookmark = ImageVector.Builder(
            name = "FeedBookmark",
            defaultWidth = 14.dp,
            defaultHeight = 20.dp,
            viewportWidth = 14f,
            viewportHeight = 20f
        ).apply {
            path(
                fill = SolidColor(Color(0xFF666666)),
                pathFillType = PathFillType.EvenOdd
            ) {
                moveTo(11.529f, 2.147f)
                horizontalLineTo(2.471f)
                curveTo(2.016f, 2.147f, 1.647f, 2.516f, 1.647f, 2.971f)
                verticalLineTo(17.086f)
                lineTo(5.931f, 14.061f)
                curveTo(6.572f, 13.609f, 7.428f, 13.609f, 8.069f, 14.061f)
                lineTo(12.353f, 17.086f)
                verticalLineTo(2.971f)
                curveTo(12.353f, 2.516f, 11.984f, 2.147f, 11.529f, 2.147f)
                close()
                moveTo(2.471f, 0.5f)
                curveTo(1.106f, 0.5f, 0f, 1.606f, 0f, 2.971f)
                verticalLineTo(18.675f)
                curveTo(0f, 19.343f, 0.753f, 19.733f, 1.298f, 19.348f)
                lineTo(6.881f, 15.407f)
                curveTo(6.952f, 15.357f, 7.048f, 15.357f, 7.119f, 15.407f)
                lineTo(12.702f, 19.348f)
                curveTo(13.247f, 19.733f, 14f, 19.343f, 14f, 18.675f)
                verticalLineTo(2.971f)
                curveTo(14f, 1.606f, 12.894f, 0.5f, 11.529f, 0.5f)
                horizontalLineTo(2.471f)
                close()
            }
        }.build()

        return _FeedBookmark!!
    }

@Suppress("ObjectPropertyName")
private var _FeedBookmark: ImageVector? = null

@Preview(showBackground = true)
@Composable
private fun FeedBookmarkPreview() {
    Box(modifier = Modifier.padding(12.dp)) {
        Image(imageVector = PrimalIcons.FeedBookmark, contentDescription = null)
    }
}
