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
import androidx.compose.ui.graphics.vector.group
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
            defaultWidth = 16.dp,
            defaultHeight = 16.dp,
            viewportWidth = 16f,
            viewportHeight = 16f
        ).apply {
            path(fill = SolidColor(Color(0xFF666666))) {
                moveTo(9.099f, 0.788f)
                curveTo(9.611f, 0.18f, 10.681f, 0.613f, 10.519f, 1.363f)
                lineTo(9.583f, 5.673f)
                horizontalLineTo(13.6f)
                curveTo(13.925f, 5.673f, 14.114f, 6.005f, 13.925f, 6.244f)
                lineTo(6.85f, 15.195f)
                curveTo(6.355f, 15.821f, 5.263f, 15.406f, 5.413f, 14.648f)
                lineTo(6.388f, 9.716f)
                horizontalLineTo(2.4f)
                curveTo(2.07f, 9.716f, 1.882f, 9.373f, 2.082f, 9.136f)
                lineTo(9.099f, 0.788f)
                close()
                moveTo(3.956f, 8.615f)
                horizontalLineTo(7.727f)
                lineTo(6.752f, 13.543f)
                lineTo(12.104f, 6.773f)
                horizontalLineTo(8.219f)
                lineTo(9.163f, 2.42f)
                lineTo(3.956f, 8.615f)
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
