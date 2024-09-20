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

val PrimalIcons.FeedNewZap: ImageVector
    get() {
        if (_FeedNewZap != null) {
            return _FeedNewZap!!
        }
        _FeedNewZap = ImageVector.Builder(
            name = "FeedNewZap",
            defaultWidth = 18.dp,
            defaultHeight = 22.dp,
            viewportWidth = 18f,
            viewportHeight = 22f
        ).apply {
            path(
                fill = SolidColor(Color(0xFF666666)),
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
                moveTo(9.893f, 4.269f)
                lineTo(8.841f, 9.44f)
                horizontalLineTo(14.032f)
                lineTo(7.978f, 17.61f)
                lineTo(9.079f, 11.663f)
                horizontalLineTo(4.067f)
                lineTo(9.893f, 4.269f)
                close()
            }
        }.build()

        return _FeedNewZap!!
    }

@Suppress("ObjectPropertyName")
private var _FeedNewZap: ImageVector? = null

@Preview(showBackground = true)
@Composable
private fun FeedNewZapPreview() {
    Box(modifier = Modifier.padding(12.dp)) {
        Image(imageVector = PrimalIcons.FeedNewZap, contentDescription = null)
    }
}
