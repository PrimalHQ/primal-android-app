package net.primal.android.core.compose.icons.primaliconpack

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp
import net.primal.android.core.compose.icons.PrimalIcons

val PrimalIcons.ContextHideHighlightsOutlined: ImageVector
    get() {
        if (_ContextHideHighlightsOutlined != null) {
            return _ContextHideHighlightsOutlined!!
        }
        _ContextHideHighlightsOutlined = ImageVector.Builder(
            name = "ContextHideHighlightsOutlined",
            defaultWidth = 20.dp,
            defaultHeight = 20.dp,
            viewportWidth = 20f,
            viewportHeight = 20f
        ).apply {
            path(fill = SolidColor(Color(0xFFFFFFFF))) {
                moveTo(0.251f, 1.443f)
                curveTo(-0.084f, 1.113f, -0.084f, 0.578f, 0.251f, 0.248f)
                curveTo(0.586f, -0.083f, 1.128f, -0.083f, 1.463f, 0.248f)
                lineTo(19.749f, 18.276f)
                curveTo(20.084f, 18.606f, 20.084f, 19.141f, 19.749f, 19.471f)
                curveTo(19.414f, 19.801f, 18.872f, 19.801f, 18.537f, 19.471f)
                lineTo(0.251f, 1.443f)
                close()
            }
            path(fill = SolidColor(Color(0xFFFFFFFF))) {
                moveTo(4.643f, 7.81f)
                lineTo(3.387f, 8.671f)
                curveTo(1.915f, 9.681f, 1.516f, 11.573f, 2.307f, 13.039f)
                curveTo(2.465f, 13.332f, 2.67f, 13.608f, 2.923f, 13.856f)
                lineTo(6.884f, 17.738f)
                curveTo(7.137f, 17.987f, 7.419f, 18.188f, 7.717f, 18.343f)
                curveTo(9.213f, 19.118f, 11.143f, 18.727f, 12.174f, 17.284f)
                lineTo(13.033f, 16.081f)
                lineTo(11.951f, 15.015f)
                lineTo(10.953f, 16.413f)
                curveTo(10.256f, 17.389f, 8.804f, 17.52f, 7.934f, 16.667f)
                lineTo(3.973f, 12.785f)
                curveTo(3.129f, 11.957f, 3.243f, 10.589f, 4.235f, 9.908f)
                lineTo(5.731f, 8.882f)
                lineTo(4.643f, 7.81f)
                close()
            }
            path(fill = SolidColor(Color(0xFFFFFFFF))) {
                moveTo(9.26f, 6.461f)
                lineTo(8.172f, 5.388f)
                lineTo(13.5f, 1.732f)
                curveTo(15.142f, 0.605f, 17.373f, 0.797f, 18.79f, 2.186f)
                curveTo(20.207f, 3.575f, 20.403f, 5.761f, 19.253f, 7.371f)
                lineTo(15.506f, 12.618f)
                lineTo(14.424f, 11.552f)
                lineTo(18.033f, 6.499f)
                curveTo(18.75f, 5.494f, 18.632f, 4.132f, 17.74f, 3.257f)
                curveTo(16.837f, 2.372f, 15.403f, 2.246f, 14.349f, 2.969f)
                lineTo(9.26f, 6.461f)
                close()
            }
            path(
                fill = SolidColor(Color(0xFFFFFFFF)),
                pathFillType = PathFillType.EvenOdd
            ) {
                moveTo(5.242f, 18.668f)
                lineTo(5.72f, 18.199f)
                lineTo(2.453f, 14.997f)
                lineTo(0.341f, 17.067f)
                curveTo(-0.387f, 17.78f, 0.128f, 19f, 1.158f, 19f)
                lineTo(4.425f, 19f)
                curveTo(4.731f, 19f, 5.025f, 18.881f, 5.242f, 18.668f)
                close()
            }
        }.build()

        return _ContextHideHighlightsOutlined!!
    }

@Suppress("ObjectPropertyName")
private var _ContextHideHighlightsOutlined: ImageVector? = null
