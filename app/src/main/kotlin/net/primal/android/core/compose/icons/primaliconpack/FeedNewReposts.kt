package net.primal.android.core.compose.icons.primaliconpack

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import net.primal.android.core.compose.icons.PrimalIcons

val PrimalIcons.FeedNewReposts: ImageVector
    get() {
        if (_FeedNewReposts != null) {
            return _FeedNewReposts!!
        }
        _FeedNewReposts = ImageVector.Builder(
            name = "FeedNewReposts",
            defaultWidth = 16.dp,
            defaultHeight = 16.dp,
            viewportWidth = 16f,
            viewportHeight = 16f
        ).apply {
            path(fill = SolidColor(Color(0xFF666666))) {
                moveTo(5.366f, 5.816f)
                curveTo(5.592f, 5.998f, 5.927f, 5.836f, 5.927f, 5.545f)
                verticalLineTo(4.186f)
                horizontalLineTo(11.455f)
                curveTo(12.599f, 4.186f, 13.527f, 5.118f, 13.527f, 6.267f)
                verticalLineTo(8.871f)
                curveTo(13.527f, 9.107f, 13.488f, 9.334f, 13.416f, 9.546f)
                curveTo(13.363f, 9.701f, 13.399f, 9.878f, 13.526f, 9.98f)
                lineTo(14.095f, 10.438f)
                curveTo(14.258f, 10.568f, 14.498f, 10.526f, 14.586f, 10.337f)
                curveTo(14.793f, 9.892f, 14.909f, 9.395f, 14.909f, 8.871f)
                verticalLineTo(6.267f)
                curveTo(14.909f, 4.351f, 13.362f, 2.799f, 11.455f, 2.799f)
                horizontalLineTo(5.927f)
                verticalLineTo(1.439f)
                curveTo(5.927f, 1.148f, 5.592f, 0.987f, 5.366f, 1.168f)
                lineTo(2.81f, 3.221f)
                curveTo(2.637f, 3.36f, 2.637f, 3.624f, 2.81f, 3.763f)
                lineTo(5.366f, 5.816f)
                close()
            }
            path(fill = SolidColor(Color(0xFF666666))) {
                moveTo(1.905f, 5.564f)
                curveTo(1.742f, 5.434f, 1.502f, 5.476f, 1.414f, 5.665f)
                curveTo(1.207f, 6.11f, 1.091f, 6.607f, 1.091f, 7.13f)
                verticalLineTo(9.735f)
                curveTo(1.091f, 11.65f, 2.638f, 13.203f, 4.545f, 13.203f)
                horizontalLineTo(10.073f)
                verticalLineTo(14.563f)
                curveTo(10.073f, 14.853f, 10.408f, 15.015f, 10.634f, 14.833f)
                lineTo(13.19f, 12.78f)
                curveTo(13.363f, 12.642f, 13.363f, 12.378f, 13.19f, 12.239f)
                lineTo(10.634f, 10.186f)
                curveTo(10.408f, 10.004f, 10.073f, 10.166f, 10.073f, 10.457f)
                verticalLineTo(11.816f)
                horizontalLineTo(4.545f)
                curveTo(3.401f, 11.816f, 2.473f, 10.884f, 2.473f, 9.735f)
                verticalLineTo(7.13f)
                curveTo(2.473f, 6.894f, 2.512f, 6.667f, 2.584f, 6.456f)
                curveTo(2.637f, 6.301f, 2.601f, 6.124f, 2.474f, 6.022f)
                lineTo(1.905f, 5.564f)
                close()
            }
        }.build()

        return _FeedNewReposts!!
    }

@Suppress("ObjectPropertyName")
private var _FeedNewReposts: ImageVector? = null

@Preview(showBackground = true)
@Composable
private fun FeedNewRepostsPreview() {
    Box(modifier = Modifier.padding(12.dp)) {
        Image(imageVector = PrimalIcons.FeedNewReposts, contentDescription = null)
    }
}
