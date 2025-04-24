package net.primal.android.core.compose.icons.primaliconpack

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp
import net.primal.android.core.compose.icons.PrimalIcons

val PrimalIcons.ContextCopyRawData: ImageVector
    get() {
        if (_ContextCopyRawData != null) {
            return _ContextCopyRawData!!
        }
        _ContextCopyRawData = ImageVector.Builder(
            name = "ContextCopyRawData",
            defaultWidth = 20.dp,
            defaultHeight = 20.dp,
            viewportWidth = 20f,
            viewportHeight = 20f
        ).apply {
            path(fill = SolidColor(Color(0xFFFFFFFF))) {
                moveTo(1f, 8.964f)
                horizontalLineTo(2.917f)
                verticalLineTo(4.857f)
                curveTo(2.917f, 2.873f, 4.587f, 1.25f, 6.667f, 1.25f)
                horizontalLineTo(7f)
                curveTo(7.423f, 1.25f, 7.75f, 1.578f, 7.75f, 1.964f)
                curveTo(7.75f, 2.35f, 7.423f, 2.679f, 7f, 2.679f)
                horizontalLineTo(6.667f)
                curveTo(5.433f, 2.679f, 4.417f, 3.645f, 4.417f, 4.857f)
                verticalLineTo(15.143f)
                curveTo(4.417f, 16.354f, 5.433f, 17.321f, 6.667f, 17.321f)
                horizontalLineTo(7f)
                curveTo(7.423f, 17.321f, 7.75f, 17.65f, 7.75f, 18.036f)
                curveTo(7.75f, 18.422f, 7.423f, 18.75f, 7f, 18.75f)
                horizontalLineTo(6.667f)
                curveTo(4.587f, 18.75f, 2.917f, 17.126f, 2.917f, 15.143f)
                verticalLineTo(10.393f)
                horizontalLineTo(1f)
                curveTo(0.577f, 10.393f, 0.25f, 10.065f, 0.25f, 9.679f)
                curveTo(0.25f, 9.293f, 0.577f, 8.964f, 1f, 8.964f)
                close()
            }
            path(fill = SolidColor(Color(0xFFFFFFFF))) {
                moveTo(17.083f, 4.857f)
                verticalLineTo(8.964f)
                horizontalLineTo(19f)
                curveTo(19.423f, 8.964f, 19.75f, 9.293f, 19.75f, 9.679f)
                curveTo(19.75f, 10.065f, 19.423f, 10.393f, 19f, 10.393f)
                horizontalLineTo(17.083f)
                verticalLineTo(15.143f)
                curveTo(17.083f, 17.126f, 15.413f, 18.75f, 13.333f, 18.75f)
                horizontalLineTo(13f)
                curveTo(12.577f, 18.75f, 12.25f, 18.422f, 12.25f, 18.036f)
                curveTo(12.25f, 17.65f, 12.577f, 17.321f, 13f, 17.321f)
                horizontalLineTo(13.333f)
                curveTo(14.567f, 17.321f, 15.583f, 16.354f, 15.583f, 15.143f)
                verticalLineTo(4.857f)
                curveTo(15.583f, 3.645f, 14.567f, 2.679f, 13.333f, 2.679f)
                horizontalLineTo(13f)
                curveTo(12.577f, 2.679f, 12.25f, 2.35f, 12.25f, 1.964f)
                curveTo(12.25f, 1.578f, 12.577f, 1.25f, 13f, 1.25f)
                horizontalLineTo(13.333f)
                curveTo(15.413f, 1.25f, 17.083f, 2.873f, 17.083f, 4.857f)
                close()
            }
        }.build()

        return _ContextCopyRawData!!
    }

@Suppress("ObjectPropertyName")
private var _ContextCopyRawData: ImageVector? = null
