package net.primal.android.core.compose.icons.primaliconpack

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp
import net.primal.android.core.compose.icons.PrimalIcons

public val PrimalIcons.Expand: ImageVector
    get() {
        if (_Expand != null) {
            return _Expand!!
        }
        _Expand = ImageVector.Builder(
            name = "Expand",
            defaultWidth = 16.dp,
            defaultHeight = 16.dp,
            viewportWidth = 16f,
            viewportHeight = 16f
        ).apply {
            path(fill = SolidColor(Color(0xFFAAAAAA))) {
                moveTo(0.686f, 7.543f)
                curveTo(1.064f, 7.543f, 1.371f, 7.85f, 1.371f, 8.229f)
                verticalLineTo(13.659f)
                lineTo(5.687f, 9.344f)
                curveTo(5.954f, 9.076f, 6.388f, 9.076f, 6.656f, 9.344f)
                curveTo(6.924f, 9.612f, 6.924f, 10.046f, 6.656f, 10.313f)
                lineTo(2.341f, 14.629f)
                horizontalLineTo(7.771f)
                curveTo(8.15f, 14.629f, 8.457f, 14.936f, 8.457f, 15.314f)
                curveTo(8.457f, 15.693f, 8.15f, 16f, 7.771f, 16f)
                horizontalLineTo(0.686f)
                curveTo(0.307f, 16f, 0f, 15.693f, 0f, 15.314f)
                verticalLineTo(8.229f)
                curveTo(0f, 7.85f, 0.307f, 7.543f, 0.686f, 7.543f)
                close()
            }
            path(fill = SolidColor(Color(0xFFAAAAAA))) {
                moveTo(15.314f, 0f)
                curveTo(15.693f, 0f, 16f, 0.307f, 16f, 0.686f)
                verticalLineTo(7.771f)
                curveTo(16f, 8.15f, 15.693f, 8.457f, 15.314f, 8.457f)
                curveTo(14.936f, 8.457f, 14.629f, 8.15f, 14.629f, 7.771f)
                verticalLineTo(2.341f)
                lineTo(10.313f, 6.656f)
                curveTo(10.046f, 6.924f, 9.612f, 6.924f, 9.344f, 6.656f)
                curveTo(9.076f, 6.388f, 9.076f, 5.954f, 9.344f, 5.687f)
                lineTo(13.659f, 1.371f)
                horizontalLineTo(8.229f)
                curveTo(7.85f, 1.371f, 7.543f, 1.064f, 7.543f, 0.686f)
                curveTo(7.543f, 0.307f, 7.85f, 0f, 8.229f, 0f)
                horizontalLineTo(15.314f)
                close()
            }
        }.build()

        return _Expand!!
    }

@Suppress("ObjectPropertyName")
private var _Expand: ImageVector? = null
