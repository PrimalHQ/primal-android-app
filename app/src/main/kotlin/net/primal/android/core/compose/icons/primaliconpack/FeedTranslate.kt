package net.primal.android.core.compose.icons.primaliconpack

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import net.primal.android.core.compose.icons.PrimalIcons

val PrimalIcons.FeedTranslate: ImageVector
    get() {
        if (_FeedTranslate != null) {
            return _FeedTranslate!!
        }
        _FeedTranslate = ImageVector.Builder(
            name = "FeedTranslate",
            defaultWidth = 16.dp,
            defaultHeight = 16.dp,
            viewportWidth = 16f,
            viewportHeight = 16f,
        ).apply {
            path(
                stroke = SolidColor(Color(0xFF666666)),
                strokeLineWidth = 1.1f,
                strokeLineCap = StrokeCap.Round,
            ) {
                // Outer circle of the globe.
                moveTo(8f, 1.25f)
                arcTo(
                    horizontalEllipseRadius = 6.75f,
                    verticalEllipseRadius = 6.75f,
                    theta = 0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    x1 = 8f,
                    y1 = 14.75f,
                )
                arcTo(
                    horizontalEllipseRadius = 6.75f,
                    verticalEllipseRadius = 6.75f,
                    theta = 0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    x1 = 8f,
                    y1 = 1.25f,
                )
                // Meridian ellipse.
                moveTo(8f, 1.25f)
                arcTo(
                    horizontalEllipseRadius = 3f,
                    verticalEllipseRadius = 6.75f,
                    theta = 0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    x1 = 8f,
                    y1 = 14.75f,
                )
                arcTo(
                    horizontalEllipseRadius = 3f,
                    verticalEllipseRadius = 6.75f,
                    theta = 0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    x1 = 8f,
                    y1 = 1.25f,
                )
                // Equator.
                moveTo(1.25f, 8f)
                lineTo(14.75f, 8f)
            }
        }.build()

        return _FeedTranslate!!
    }

@Suppress("ObjectPropertyName")
private var _FeedTranslate: ImageVector? = null

@Preview(showBackground = true)
@Composable
private fun FeedTranslatePreview() {
    Box(modifier = Modifier.padding(12.dp)) {
        Image(imageVector = PrimalIcons.FeedTranslate, contentDescription = null)
    }
}
