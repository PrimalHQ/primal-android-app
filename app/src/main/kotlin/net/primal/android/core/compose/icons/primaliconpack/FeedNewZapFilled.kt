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

val PrimalIcons.FeedNewZapFilled: ImageVector
    get() {
        if (_FeedNewZapFilled != null) {
            return _FeedNewZapFilled!!
        }
        _FeedNewZapFilled = ImageVector.Builder(
            name = "FeedNewZapFilled",
            defaultWidth = 18.dp,
            defaultHeight = 22.dp,
            viewportWidth = 18f,
            viewportHeight = 22f
        ).apply {
            path(
                fill = SolidColor(Color(0xFFFFA02F)),
                pathFillType = PathFillType.EvenOdd
            ) {
                moveTo(17.147f, 8.424f)
                curveTo(17.406f, 8.074f, 17.147f, 7.587f, 16.7f, 7.587f)
                horizontalLineTo(11.177f)
                lineTo(12.463f, 1.267f)
                curveTo(12.687f, 0.166f, 11.214f, -0.47f, 10.51f, 0.423f)
                lineTo(0.863f, 12.666f)
                curveTo(0.588f, 13.015f, 0.846f, 13.516f, 1.3f, 13.516f)
                horizontalLineTo(6.784f)
                lineTo(5.443f, 20.751f)
                curveTo(5.237f, 21.862f, 6.738f, 22.471f, 7.418f, 21.552f)
                lineTo(17.147f, 8.424f)
                close()
            }
        }.build()

        return _FeedNewZapFilled!!
    }

@Suppress("ObjectPropertyName")
private var _FeedNewZapFilled: ImageVector? = null

@Preview(showBackground = true)
@Composable
private fun FeedNewZapFilledPreview() {
    Box(modifier = Modifier.padding(12.dp)) {
        Image(imageVector = PrimalIcons.FeedNewZapFilled, contentDescription = null)
    }
}
