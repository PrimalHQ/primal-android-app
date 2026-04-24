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

val PrimalIcons.FeedNewReplyFilled: ImageVector
    get() {
        if (_FeedNewReplyFilled != null) {
            return _FeedNewReplyFilled!!
        }
        _FeedNewReplyFilled = ImageVector.Builder(
            name = "FeedNewReplyFilled",
            defaultWidth = 16.dp,
            defaultHeight = 16.dp,
            viewportWidth = 16f,
            viewportHeight = 16f
        ).apply {
            path(
                fill = SolidColor(Color(0xFFD5D5D5)),
                pathFillType = PathFillType.EvenOdd
            ) {
                moveTo(2.966f, 15.196f)
                curveTo(2.649f, 15.408f, 2.231f, 15.155f, 2.264f, 14.771f)
                lineTo(2.607f, 10.809f)
                curveTo(1.439f, 9.816f, 0.727f, 8.499f, 0.727f, 7.053f)
                curveTo(0.727f, 3.961f, 3.983f, 1.455f, 8f, 1.455f)
                curveTo(12.017f, 1.455f, 15.273f, 3.961f, 15.273f, 7.053f)
                curveTo(15.273f, 10.145f, 12.017f, 12.651f, 8f, 12.651f)
                curveTo(7.621f, 12.651f, 7.249f, 12.629f, 6.885f, 12.586f)
                lineTo(2.966f, 15.196f)
                close()
            }
        }.build()

        return _FeedNewReplyFilled!!
    }

@Suppress("ObjectPropertyName")
private var _FeedNewReplyFilled: ImageVector? = null

@Preview(showBackground = true)
@Composable
private fun FeedNewReplyFilledPreview() {
    Box(modifier = Modifier.padding(12.dp)) {
        Image(imageVector = PrimalIcons.FeedNewReplyFilled, contentDescription = null)
    }
}
