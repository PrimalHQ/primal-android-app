package net.primal.android.core.compose.icons.primaliconpack

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp
import net.primal.android.core.compose.icons.PrimalIcons

val PrimalIcons.MediumSecurity: ImageVector
    get() {
        if (_MediumSecurity != null) {
            return _MediumSecurity!!
        }
        _MediumSecurity = ImageVector.Builder(
            name = "MediumSecurity",
            defaultWidth = 20.dp,
            defaultHeight = 24.dp,
            viewportWidth = 20f,
            viewportHeight = 24f
        ).apply {
            path(fill = SolidColor(Color.White)) {
                moveTo(5.814f, 0.957f)
                curveTo(5.772f, 0.345f, 6.323f, -0.145f, 6.921f, 0.04f)
                curveTo(7.819f, 0.317f, 8.981f, 0.812f, 9.523f, 1.609f)
                curveTo(10.476f, 3.008f, 10.477f, 4.408f, 10.477f, 5.807f)
                curveTo(10.477f, 6.499f, 10.243f, 7.762f, 10.007f, 8.862f)
                curveTo(9.881f, 9.448f, 10.334f, 10.005f, 10.946f, 10.005f)
                horizontalLineTo(17.143f)
                curveTo(18.72f, 10.005f, 20f, 11.259f, 20f, 12.804f)
                curveTo(20f, 13.424f, 19.794f, 13.995f, 19.445f, 14.458f)
                curveTo(18.994f, 15.059f, 18.792f, 16.092f, 18.97f, 16.822f)
                curveTo(19.02f, 17.029f, 19.048f, 17.246f, 19.048f, 17.469f)
                curveTo(19.048f, 18.391f, 18.592f, 19.206f, 17.891f, 19.715f)
                curveTo(17.436f, 20.045f, 17.143f, 20.639f, 17.143f, 21.201f)
                curveTo(17.143f, 22.746f, 15.864f, 23.999f, 14.286f, 24f)
                horizontalLineTo(7.619f)
                curveTo(7.619f, 24f, 4.762f, 24f, 2.857f, 22.134f)
                curveTo(2.412f, 21.698f, 2.019f, 21.364f, 1.678f, 21.108f)
                curveTo(0.843f, 20.481f, 0f, 19.432f, 0f, 18.402f)
                verticalLineTo(14.891f)
                curveTo(0f, 14.184f, 0.408f, 13.538f, 1.053f, 13.222f)
                lineTo(2.78f, 12.375f)
                curveTo(3.584f, 11.471f, 5.349f, 9.243f, 5.714f, 6.741f)
                curveTo(6.037f, 5.158f, 5.922f, 2.502f, 5.814f, 0.957f)
                close()
            }
        }.build()

        return _MediumSecurity!!
    }

@Suppress("ObjectPropertyName")
private var _MediumSecurity: ImageVector? = null
