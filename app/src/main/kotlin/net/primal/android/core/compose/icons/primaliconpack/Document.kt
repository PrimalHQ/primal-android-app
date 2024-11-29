package net.primal.android.core.compose.icons.primaliconpack

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp
import net.primal.android.core.compose.icons.PrimalIcons

val PrimalIcons.Document: ImageVector
    get() {
        if (_Document != null) {
            return _Document!!
        }
        _Document = ImageVector.Builder(
            name = "Document",
            defaultWidth = 20.dp,
            defaultHeight = 23.dp,
            viewportWidth = 20f,
            viewportHeight = 23f
        ).apply {
            path(
                stroke = SolidColor(Color(0xFF757575)),
                strokeLineWidth = 1.5f
            ) {
                moveTo(2.75f, 4f)
                curveTo(2.75f, 2.757f, 3.757f, 1.75f, 5f, 1.75f)
                horizontalLineTo(11.757f)
                curveTo(12.354f, 1.75f, 12.926f, 1.987f, 13.348f, 2.409f)
                lineTo(13.879f, 1.879f)
                lineTo(13.348f, 2.409f)
                lineTo(15.47f, 4.53f)
                lineTo(17.591f, 6.652f)
                curveTo(18.013f, 7.074f, 18.25f, 7.646f, 18.25f, 8.243f)
                verticalLineTo(18f)
                curveTo(18.25f, 19.243f, 17.243f, 20.25f, 16f, 20.25f)
                horizontalLineTo(5f)
                curveTo(3.757f, 20.25f, 2.75f, 19.243f, 2.75f, 18f)
                verticalLineTo(4f)
                close()
            }
            path(
                stroke = SolidColor(Color(0xFF757575)),
                strokeLineWidth = 1.5f
            ) {
                moveTo(11.5f, 2f)
                verticalLineTo(7.5f)
                curveTo(11.5f, 8.052f, 11.948f, 8.5f, 12.5f, 8.5f)
                horizontalLineTo(18f)
            }
        }.build()

        return _Document!!
    }

@Suppress("ObjectPropertyName")
private var _Document: ImageVector? = null
