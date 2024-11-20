package net.primal.android.core.compose.icons.primaliconpack

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp
import net.primal.android.core.compose.icons.PrimalIcons

val PrimalIcons.DrawerPremium: ImageVector
    get() {
        if (_DrawerPremium != null) {
            return _DrawerPremium!!
        }
        _DrawerPremium = ImageVector.Builder(
            name = "DrawerPremium",
            defaultWidth = 20.dp,
            defaultHeight = 20.dp,
            viewportWidth = 20f,
            viewportHeight = 20f
        ).apply {
            path(fill = SolidColor(Color(0xFFAAAAAA))) {
                moveTo(13.453f, 8.445f)
                curveTo(13.691f, 8.197f, 13.681f, 7.805f, 13.431f, 7.569f)
                curveTo(13.181f, 7.333f, 12.785f, 7.342f, 12.547f, 7.59f)
                lineTo(8.714f, 11.58f)
                lineTo(7.453f, 10.267f)
                curveTo(7.215f, 10.019f, 6.819f, 10.009f, 6.569f, 10.245f)
                curveTo(6.319f, 10.481f, 6.309f, 10.873f, 6.547f, 11.121f)
                lineTo(8.714f, 13.377f)
                lineTo(13.453f, 8.445f)
                close()
            }
            path(
                fill = SolidColor(Color(0xFFAAAAAA)),
                pathFillType = PathFillType.EvenOdd
            ) {
                moveTo(0.123f, 9.705f)
                lineTo(2.321f, 7.513f)
                lineTo(1.835f, 4.452f)
                curveTo(1.799f, 4.224f, 1.956f, 4.01f, 2.187f, 3.973f)
                lineTo(5.254f, 3.488f)
                lineTo(6.663f, 0.731f)
                curveTo(6.769f, 0.524f, 7.026f, 0.441f, 7.235f, 0.548f)
                lineTo(9.999f, 1.952f)
                lineTo(12.766f, 0.546f)
                curveTo(12.975f, 0.44f, 13.231f, 0.523f, 13.337f, 0.73f)
                lineTo(14.747f, 3.488f)
                lineTo(17.813f, 3.972f)
                curveTo(18.044f, 4.009f, 18.202f, 4.223f, 18.165f, 4.452f)
                lineTo(17.678f, 7.513f)
                lineTo(19.877f, 9.705f)
                curveTo(20.041f, 9.868f, 20.041f, 10.132f, 19.877f, 10.296f)
                lineTo(17.679f, 12.488f)
                lineTo(18.165f, 15.548f)
                curveTo(18.201f, 15.777f, 18.044f, 15.991f, 17.813f, 16.028f)
                lineTo(14.745f, 16.512f)
                lineTo(13.336f, 19.27f)
                curveTo(13.23f, 19.477f, 12.973f, 19.56f, 12.764f, 19.454f)
                lineTo(9.999f, 18.049f)
                lineTo(7.237f, 19.453f)
                curveTo(7.027f, 19.559f, 6.771f, 19.477f, 6.665f, 19.269f)
                lineTo(5.256f, 16.512f)
                lineTo(2.187f, 16.028f)
                curveTo(1.956f, 15.991f, 1.799f, 15.777f, 1.835f, 15.548f)
                lineTo(2.322f, 12.488f)
                lineTo(0.123f, 10.296f)
                curveTo(-0.041f, 10.132f, -0.041f, 9.869f, 0.123f, 9.705f)
                close()
                moveTo(16.802f, 5.067f)
                lineTo(16.345f, 7.941f)
                lineTo(18.41f, 10f)
                lineTo(16.345f, 12.06f)
                lineTo(16.802f, 14.932f)
                lineTo(13.919f, 15.387f)
                lineTo(12.596f, 17.976f)
                lineTo(9.999f, 16.657f)
                lineTo(7.405f, 17.975f)
                lineTo(6.083f, 15.387f)
                lineTo(3.199f, 14.932f)
                lineTo(3.655f, 12.06f)
                lineTo(1.589f, 10f)
                lineTo(3.655f, 7.941f)
                lineTo(3.198f, 5.068f)
                lineTo(6.081f, 4.613f)
                lineTo(7.403f, 2.025f)
                lineTo(9.999f, 3.344f)
                lineTo(12.597f, 2.024f)
                lineTo(13.921f, 4.613f)
                lineTo(16.802f, 5.067f)
                close()
            }
        }.build()

        return _DrawerPremium!!
    }

@Suppress("ObjectPropertyName")
private var _DrawerPremium: ImageVector? = null
