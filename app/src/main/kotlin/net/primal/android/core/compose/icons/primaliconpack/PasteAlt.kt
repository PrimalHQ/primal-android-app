import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp
import net.primal.android.core.compose.icons.PrimalIcons

val PrimalIcons.PasteAlt: ImageVector
    get() {
        if (_PasteAlt != null) {
            return _PasteAlt!!
        }
        _PasteAlt = ImageVector.Builder(
            name = "PasteAlt",
            defaultWidth = 13.dp,
            defaultHeight = 16.dp,
            viewportWidth = 13f,
            viewportHeight = 16f
        ).apply {
            path(fill = SolidColor(Color(0xFFAAAAAA))) {
                moveTo(2.5f, 2f)
                curveTo(2.776f, 2f, 3f, 2.224f, 3f, 2.5f)
                curveTo(3f, 2.776f, 2.776f, 3f, 2.5f, 3f)
                horizontalLineTo(2f)
                curveTo(1.482f, 3f, 1.056f, 3.393f, 1.005f, 3.897f)
                lineTo(1f, 4f)
                verticalLineTo(14f)
                curveTo(1f, 14.552f, 1.448f, 15f, 2f, 15f)
                horizontalLineTo(11f)
                curveTo(11.552f, 15f, 12f, 14.552f, 12f, 14f)
                verticalLineTo(4f)
                curveTo(12f, 3.482f, 11.607f, 3.056f, 11.102f, 3.005f)
                lineTo(11f, 3f)
                horizontalLineTo(10.5f)
                curveTo(10.224f, 3f, 10f, 2.776f, 10f, 2.5f)
                curveTo(10f, 2.224f, 10.224f, 2f, 10.5f, 2f)
                horizontalLineTo(11f)
                lineTo(11.204f, 2.011f)
                curveTo(12.213f, 2.113f, 13f, 2.964f, 13f, 4f)
                verticalLineTo(14f)
                curveTo(13f, 15.036f, 12.213f, 15.887f, 11.204f, 15.989f)
                lineTo(11f, 16f)
                horizontalLineTo(2f)
                curveTo(0.964f, 16f, 0.113f, 15.213f, 0.011f, 14.204f)
                lineTo(0f, 14f)
                verticalLineTo(4f)
                curveTo(0f, 2.895f, 0.895f, 2f, 2f, 2f)
                horizontalLineTo(2.5f)
                close()
            }
            path(fill = SolidColor(Color(0xFFAAAAAA))) {
                moveTo(8.5f, 1f)
                curveTo(9.052f, 1f, 9.5f, 1.448f, 9.5f, 2f)
                verticalLineTo(3f)
                curveTo(9.5f, 3.552f, 9.052f, 4f, 8.5f, 4f)
                horizontalLineTo(4.5f)
                curveTo(3.948f, 4f, 3.5f, 3.552f, 3.5f, 3f)
                verticalLineTo(2f)
                curveTo(3.5f, 1.448f, 3.948f, 1f, 4.5f, 1f)
                horizontalLineTo(5f)
                curveTo(5f, 0.448f, 5.448f, 0f, 6f, 0f)
                horizontalLineTo(7f)
                curveTo(7.552f, 0f, 8f, 0.448f, 8f, 1f)
                horizontalLineTo(8.5f)
                close()
            }
        }.build()

        return _PasteAlt!!
    }

@Suppress("ObjectPropertyName")
private var _PasteAlt: ImageVector? = null
