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
            defaultWidth = 20.dp,
            defaultHeight = 20.dp,
            viewportWidth = 20f,
            viewportHeight = 20f
        ).apply {
            group {
                path(
                    fill = SolidColor(Color(0xFF666666)),
                    pathFillType = PathFillType.EvenOdd
                ) {
                    moveTo(15.915f, 6.893f)
                    curveTo(16.128f, 6.606f, 15.915f, 6.208f, 15.55f, 6.208f)
                    horizontalLineTo(11.031f)
                    lineTo(12.083f, 1.036f)
                    curveTo(12.266f, 0.136f, 11.061f, -0.385f, 10.486f, 0.346f)
                    lineTo(2.592f, 10.363f)
                    curveTo(2.368f, 10.649f, 2.579f, 11.059f, 2.95f, 11.059f)
                    horizontalLineTo(7.437f)
                    lineTo(6.34f, 16.978f)
                    curveTo(6.171f, 17.887f, 7.399f, 18.385f, 7.956f, 17.634f)
                    lineTo(15.915f, 6.893f)
                    close()
                    moveTo(9.981f, 3.493f)
                    lineTo(9.12f, 7.724f)
                    horizontalLineTo(13.367f)
                    lineTo(8.413f, 14.408f)
                    lineTo(9.315f, 9.543f)
                    horizontalLineTo(5.214f)
                    lineTo(9.981f, 3.493f)
                    close()
                }
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
