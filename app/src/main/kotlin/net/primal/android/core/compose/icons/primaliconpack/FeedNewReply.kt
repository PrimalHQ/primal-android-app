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

val PrimalIcons.FeedNewReply: ImageVector
    get() {
        if (_FeedNewReply != null) {
            return _FeedNewReply!!
        }
        _FeedNewReply = ImageVector.Builder(
            name = "FeedNewReply",
            defaultWidth = 20.dp,
            defaultHeight = 19.dp,
            viewportWidth = 20f,
            viewportHeight = 19f
        ).apply {
            path(
                fill = SolidColor(Color(0xFF666666)),
                pathFillType = PathFillType.EvenOdd
            ) {
                moveTo(4.538f, 12.042f)
                lineTo(4.206f, 15.87f)
                lineTo(8.007f, 13.338f)
                lineTo(8.685f, 13.418f)
                curveTo(9.112f, 13.469f, 9.551f, 13.495f, 10f, 13.495f)
                curveTo(12.389f, 13.495f, 14.482f, 12.748f, 15.937f, 11.628f)
                curveTo(17.39f, 10.509f, 18.125f, 9.102f, 18.125f, 7.697f)
                curveTo(18.125f, 6.293f, 17.39f, 4.886f, 15.937f, 3.767f)
                curveTo(14.482f, 2.647f, 12.389f, 1.9f, 10f, 1.9f)
                curveTo(7.611f, 1.9f, 5.518f, 2.647f, 4.063f, 3.767f)
                curveTo(2.61f, 4.886f, 1.875f, 6.293f, 1.875f, 7.697f)
                curveTo(1.875f, 9.013f, 2.517f, 10.325f, 3.79f, 11.407f)
                lineTo(4.538f, 12.042f)
                close()
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

        return _FeedNewReply!!
    }

@Suppress("ObjectPropertyName")
private var _FeedNewReply: ImageVector? = null

@Preview(showBackground = true)
@Composable
private fun FeedNewReplyPreview() {
    Box(modifier = Modifier.padding(12.dp)) {
        Image(imageVector = PrimalIcons.FeedNewReply, contentDescription = null)
    }
}
