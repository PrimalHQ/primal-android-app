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
            defaultWidth = 20.dp,
            defaultHeight = 19.dp,
            viewportWidth = 20f,
            viewportHeight = 19f
        ).apply {
            path(
                fill = SolidColor(Color(0xFFAAAAAA)),
                pathFillType = PathFillType.EvenOdd
            ) {
                moveTo(3.079f, 18.895f)
                curveTo(2.642f, 19.185f, 2.067f, 18.837f, 2.113f, 18.31f)
                lineTo(2.585f, 12.862f)
                curveTo(0.978f, 11.496f, 0f, 9.685f, 0f, 7.697f)
                curveTo(0f, 3.446f, 4.477f, 0f, 10f, 0f)
                curveTo(15.523f, 0f, 20f, 3.446f, 20f, 7.697f)
                curveTo(20f, 11.949f, 15.523f, 15.395f, 10f, 15.395f)
                curveTo(9.479f, 15.395f, 8.967f, 15.364f, 8.467f, 15.305f)
                lineTo(3.079f, 18.895f)
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
