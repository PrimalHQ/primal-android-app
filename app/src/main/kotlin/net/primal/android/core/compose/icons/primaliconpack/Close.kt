package net.primal.android.core.compose.icons.primaliconpack

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val Close: ImageVector
    get() {
        if (_Close != null) {
            return _Close!!
        }
        _Close = ImageVector.Builder(
            name = "Close",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(fill = SolidColor(Color(0xFFAAAAAA))) {
                moveTo(4.982f, 3.837f)
                curveTo(4.666f, 3.521f, 4.153f, 3.521f, 3.837f, 3.837f)
                curveTo(3.521f, 4.154f, 3.521f, 4.666f, 3.837f, 4.983f)
                lineTo(10.854f, 12f)
                lineTo(3.837f, 19.017f)
                curveTo(3.521f, 19.334f, 3.521f, 19.847f, 3.837f, 20.163f)
                curveTo(4.153f, 20.479f, 4.666f, 20.479f, 4.982f, 20.163f)
                lineTo(12f, 13.146f)
                lineTo(19.017f, 20.163f)
                curveTo(19.333f, 20.479f, 19.846f, 20.479f, 20.162f, 20.163f)
                curveTo(20.479f, 19.847f, 20.479f, 19.334f, 20.162f, 19.017f)
                lineTo(13.145f, 12f)
                lineTo(20.162f, 4.983f)
                curveTo(20.479f, 4.666f, 20.479f, 4.154f, 20.162f, 3.837f)
                curveTo(19.846f, 3.521f, 19.333f, 3.521f, 19.017f, 3.837f)
                lineTo(12f, 10.855f)
                lineTo(4.982f, 3.837f)
                close()
            }
        }.build()

        return _Close!!
    }

@Suppress("ObjectPropertyName")
private var _Close: ImageVector? = null
