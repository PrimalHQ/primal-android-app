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

val PrimalIcons.FeedBookmarkFilled: ImageVector
    get() {
        if (_FeedBookmarkFilled != null) {
            return _FeedBookmarkFilled!!
        }
        _FeedBookmarkFilled = ImageVector.Builder(
            name = "FeedBookmarkFilled",
            defaultWidth = 16.dp,
            defaultHeight = 16.dp,
            viewportWidth = 16f,
            viewportHeight = 16f
        ).apply {
            path(
                fill = SolidColor(Color(0xFF0090F8)),
                pathFillType = PathFillType.EvenOdd
            ) {
                moveTo(4.706f, 1.092f)
                curveTo(3.714f, 1.092f, 2.909f, 1.896f, 2.909f, 2.889f)
                verticalLineTo(14.31f)
                curveTo(2.909f, 14.796f, 3.457f, 15.079f, 3.853f, 14.799f)
                lineTo(7.914f, 11.933f)
                curveTo(7.965f, 11.897f, 8.035f, 11.897f, 8.086f, 11.933f)
                lineTo(12.147f, 14.799f)
                curveTo(12.543f, 15.079f, 13.091f, 14.796f, 13.091f, 14.31f)
                verticalLineTo(2.889f)
                curveTo(13.091f, 1.896f, 12.286f, 1.092f, 11.294f, 1.092f)
                horizontalLineTo(4.706f)
                close()
            }
        }.build()

        return _FeedBookmarkFilled!!
    }

@Suppress("ObjectPropertyName")
private var _FeedBookmarkFilled: ImageVector? = null

@Preview(showBackground = true)
@Composable
private fun FeedBookmarkFilledPreview() {
    Box(modifier = Modifier.padding(12.dp)) {
        Image(imageVector = PrimalIcons.FeedBookmarkFilled, contentDescription = null)
    }
}
