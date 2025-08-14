package net.primal.android.core.compose.icons.primaliconpack

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp
import net.primal.android.core.compose.icons.PrimalIcons

val PrimalIcons.Minimize: ImageVector
    get() {
        if (_Minimize != null) {
            return _Minimize!!
        }
        _Minimize = ImageVector.Builder(
            name = "Minimize",
            defaultWidth = 28.dp,
            defaultHeight = 28.dp,
            viewportWidth = 28f,
            viewportHeight = 28f
        ).apply {
            path(fill = SolidColor(Color.White)) {
                moveTo(20.47f, 10.47f)
                curveTo(20.763f, 10.177f, 21.237f, 10.177f, 21.53f, 10.47f)
                curveTo(21.823f, 10.763f, 21.823f, 11.237f, 21.53f, 11.53f)
                lineTo(14.707f, 18.354f)
                curveTo(14.316f, 18.744f, 13.683f, 18.744f, 13.293f, 18.354f)
                lineTo(6.47f, 11.53f)
                curveTo(6.177f, 11.237f, 6.177f, 10.763f, 6.47f, 10.47f)
                curveTo(6.763f, 10.177f, 7.237f, 10.177f, 7.53f, 10.47f)
                lineTo(14f, 16.94f)
                lineTo(20.47f, 10.47f)
                close()
            }
        }.build()

        return _Minimize!!
    }

@Suppress("ObjectPropertyName")
private var _Minimize: ImageVector? = null
