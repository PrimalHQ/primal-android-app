package net.primal.android.core.compose.icons.primaliconpack

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp
import net.primal.android.core.compose.icons.PrimalIcons

val PrimalIcons.Home: ImageVector
    get() {
        if (_Home != null) {
            return _Home!!
        }
        _Home = ImageVector.Builder(
            name = "Home",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(
                fill = SolidColor(Color(0xFFAAAAAA)),
                pathFillType = PathFillType.EvenOdd
            ) {
                moveTo(11.624f, 3.167f)
                curveTo(11.847f, 3f, 12.153f, 3f, 12.376f, 3.167f)
                lineTo(21.531f, 10.065f)
                curveTo(21.812f, 10.277f, 21.877f, 10.69f, 21.67f, 10.985f)
                curveTo(21.466f, 11.276f, 21.078f, 11.337f, 20.802f, 11.129f)
                lineTo(19.327f, 10.018f)
                lineTo(18.458f, 19.28f)
                verticalLineTo(19.29f)
                curveTo(18.458f, 20.095f, 17.805f, 20.748f, 17f, 20.748f)
                horizontalLineTo(7f)
                curveTo(6.195f, 20.748f, 5.542f, 20.095f, 5.542f, 19.29f)
                verticalLineTo(19.28f)
                lineTo(4.673f, 10.018f)
                lineTo(3.198f, 11.129f)
                curveTo(2.922f, 11.337f, 2.534f, 11.276f, 2.33f, 10.985f)
                curveTo(2.123f, 10.69f, 2.188f, 10.277f, 2.469f, 10.065f)
                lineTo(11.624f, 3.167f)
                close()
                moveTo(12.627f, 4.97f)
                curveTo(12.256f, 4.69f, 11.744f, 4.69f, 11.373f, 4.97f)
                lineTo(5.949f, 9.056f)
                lineTo(6.792f, 18.407f)
                curveTo(6.797f, 18.977f, 7.261f, 19.438f, 7.833f, 19.438f)
                horizontalLineTo(16.167f)
                curveTo(16.739f, 19.438f, 17.203f, 18.977f, 17.208f, 18.407f)
                lineTo(18.051f, 9.057f)
                lineTo(12.627f, 4.97f)
                close()
            }
        }.build()

        return _Home!!
    }

@Suppress("ObjectPropertyName")
private var _Home: ImageVector? = null
