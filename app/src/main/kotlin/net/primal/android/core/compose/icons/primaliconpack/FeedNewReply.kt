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

val PrimalIcons.FeedNewReply: ImageVector
    get() {
        if (_FeedNewReply != null) {
            return _FeedNewReply!!
        }
        _FeedNewReply = ImageVector.Builder(
            name = "FeedNewReply",
            defaultWidth = 16.dp,
            defaultHeight = 16.dp,
            viewportWidth = 16f,
            viewportHeight = 16f
        ).apply {
            path(
                fill = SolidColor(Color(0xFF666666)),
                pathFillType = PathFillType.EvenOdd
            ) {
                moveTo(4.028f, 10.213f)
                lineTo(3.786f, 12.997f)
                lineTo(6.551f, 11.155f)
                lineTo(7.044f, 11.214f)
                curveTo(7.354f, 11.25f, 7.674f, 11.27f, 8f, 11.27f)
                curveTo(9.738f, 11.27f, 11.26f, 10.726f, 12.318f, 9.912f)
                curveTo(13.375f, 9.098f, 13.909f, 8.074f, 13.909f, 7.053f)
                curveTo(13.909f, 6.032f, 13.375f, 5.009f, 12.318f, 4.195f)
                curveTo(11.26f, 3.381f, 9.738f, 2.837f, 8f, 2.837f)
                curveTo(6.262f, 2.837f, 4.74f, 3.381f, 3.682f, 4.195f)
                curveTo(2.625f, 5.009f, 2.091f, 6.032f, 2.091f, 7.053f)
                curveTo(2.091f, 8.01f, 2.558f, 8.964f, 3.484f, 9.751f)
                lineTo(4.028f, 10.213f)
                close()
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
