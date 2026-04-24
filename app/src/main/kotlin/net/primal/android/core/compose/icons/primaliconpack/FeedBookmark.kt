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

val PrimalIcons.FeedBookmark: ImageVector
    get() {
        if (_FeedBookmark != null) {
            return _FeedBookmark!!
        }
        _FeedBookmark = ImageVector.Builder(
            name = "FeedBookmark",
            defaultWidth = 16.dp,
            defaultHeight = 16.dp,
            viewportWidth = 16f,
            viewportHeight = 16f
        ).apply {
            path(
                fill = SolidColor(Color(0xFF666666)),
                pathFillType = PathFillType.EvenOdd
            ) {
                moveTo(4.706f, 2.342f)
                curveTo(4.404f, 2.342f, 4.159f, 2.587f, 4.159f, 2.889f)
                verticalLineTo(13.054f)
                lineTo(7.193f, 10.912f)
                curveTo(7.193f, 10.912f, 7.193f, 10.912f, 7.193f, 10.912f)
                curveTo(7.677f, 10.571f, 8.323f, 10.571f, 8.807f, 10.912f)
                curveTo(8.807f, 10.912f, 8.807f, 10.912f, 8.807f, 10.912f)
                lineTo(11.841f, 13.054f)
                verticalLineTo(2.889f)
                curveTo(11.841f, 2.587f, 11.596f, 2.342f, 11.294f, 2.342f)
                horizontalLineTo(4.706f)
                close()
                moveTo(8.086f, 11.933f)
                curveTo(8.035f, 11.897f, 7.965f, 11.897f, 7.914f, 11.933f)
                lineTo(3.853f, 14.799f)
                curveTo(3.457f, 15.079f, 2.909f, 14.796f, 2.909f, 14.31f)
                verticalLineTo(2.889f)
                curveTo(2.909f, 1.896f, 3.714f, 1.092f, 4.706f, 1.092f)
                horizontalLineTo(11.294f)
                curveTo(12.286f, 1.092f, 13.091f, 1.896f, 13.091f, 2.889f)
                verticalLineTo(14.31f)
                curveTo(13.091f, 14.796f, 12.543f, 15.079f, 12.147f, 14.799f)
                lineTo(8.086f, 11.933f)
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
