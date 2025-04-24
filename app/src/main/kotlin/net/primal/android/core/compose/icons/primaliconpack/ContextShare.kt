package net.primal.android.core.compose.icons.primaliconpack

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp
import net.primal.android.core.compose.icons.PrimalIcons

val PrimalIcons.ContextShare: ImageVector
    get() {
        if (_ContextShare != null) {
            return _ContextShare!!
        }
        _ContextShare = ImageVector.Builder(
            name = "ContextShare",
            defaultWidth = 20.dp,
            defaultHeight = 20.dp,
            viewportWidth = 20f,
            viewportHeight = 20f
        ).apply {
            path(fill = SolidColor(Color(0xFFFFFFFF))) {
                moveTo(10f, 14.717f)
                curveTo(10.46f, 14.717f, 10.833f, 14.329f, 10.833f, 13.851f)
                verticalLineTo(3.741f)
                lineTo(14.291f, 6.713f)
                curveTo(14.64f, 7.024f, 15.167f, 6.982f, 15.466f, 6.619f)
                curveTo(15.766f, 6.256f, 15.725f, 5.709f, 15.376f, 5.398f)
                lineTo(10.554f, 1.211f)
                curveTo(10.238f, 0.93f, 9.762f, 0.93f, 9.446f, 1.211f)
                lineTo(4.624f, 5.398f)
                curveTo(4.275f, 5.709f, 4.234f, 6.256f, 4.534f, 6.619f)
                curveTo(4.833f, 6.982f, 5.36f, 7.024f, 5.709f, 6.713f)
                lineTo(9.167f, 3.741f)
                verticalLineTo(13.851f)
                curveTo(9.167f, 14.329f, 9.54f, 14.717f, 10f, 14.717f)
                close()
            }
            path(fill = SolidColor(Color(0xFFFFFFFF))) {
                moveTo(0f, 11.833f)
                curveTo(0f, 11.373f, 0.373f, 11f, 0.833f, 11f)
                curveTo(1.294f, 11f, 1.667f, 11.373f, 1.667f, 11.833f)
                verticalLineTo(17.5f)
                curveTo(1.667f, 17.96f, 2.04f, 18.333f, 2.5f, 18.333f)
                horizontalLineTo(17.5f)
                curveTo(17.96f, 18.333f, 18.333f, 17.96f, 18.333f, 17.5f)
                verticalLineTo(11.833f)
                curveTo(18.333f, 11.373f, 18.706f, 11f, 19.167f, 11f)
                curveTo(19.627f, 11f, 20f, 11.373f, 20f, 11.833f)
                verticalLineTo(18.333f)
                curveTo(20f, 19.254f, 19.254f, 20f, 18.333f, 20f)
                horizontalLineTo(1.667f)
                curveTo(0.746f, 20f, 0f, 19.254f, 0f, 18.333f)
                verticalLineTo(11.833f)
                close()
            }
        }.build()

        return _ContextShare!!
    }

@Suppress("ObjectPropertyName")
private var _ContextShare: ImageVector? = null
