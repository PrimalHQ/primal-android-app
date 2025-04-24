package net.primal.android.core.compose.icons.primaliconpack

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp
import net.primal.android.core.compose.icons.PrimalIcons

val PrimalIcons.ContextCopyNoteText: ImageVector
    get() {
        if (_ContextCopyNoteText != null) {
            return _ContextCopyNoteText!!
        }
        _ContextCopyNoteText = ImageVector.Builder(
            name = "ContextCopyNoteText",
            defaultWidth = 20.dp,
            defaultHeight = 20.dp,
            viewportWidth = 20f,
            viewportHeight = 20f
        ).apply {
            path(fill = SolidColor(Color(0xFFFFFFFF))) {
                moveTo(2f, 1.692f)
                curveTo(2f, 1.446f, 2.198f, 1.25f, 2.438f, 1.25f)
                horizontalLineTo(17.563f)
                curveTo(17.802f, 1.25f, 18f, 1.446f, 18f, 1.692f)
                verticalLineTo(5.846f)
                curveTo(18f, 6.092f, 17.802f, 6.288f, 17.563f, 6.288f)
                curveTo(17.323f, 6.288f, 17.125f, 6.092f, 17.125f, 5.846f)
                verticalLineTo(4.115f)
                curveTo(17.125f, 3.788f, 16.861f, 3.519f, 16.531f, 3.519f)
                horizontalLineTo(11.719f)
                curveTo(11.389f, 3.519f, 11.125f, 3.788f, 11.125f, 4.115f)
                verticalLineTo(17.269f)
                curveTo(11.125f, 17.597f, 11.389f, 17.865f, 11.719f, 17.865f)
                horizontalLineTo(13.438f)
                curveTo(13.677f, 17.865f, 13.875f, 18.062f, 13.875f, 18.308f)
                curveTo(13.875f, 18.554f, 13.677f, 18.75f, 13.438f, 18.75f)
                horizontalLineTo(6.563f)
                curveTo(6.323f, 18.75f, 6.125f, 18.554f, 6.125f, 18.308f)
                curveTo(6.125f, 18.062f, 6.323f, 17.865f, 6.563f, 17.865f)
                horizontalLineTo(8.281f)
                curveTo(8.611f, 17.865f, 8.875f, 17.597f, 8.875f, 17.269f)
                verticalLineTo(4.115f)
                curveTo(8.875f, 3.788f, 8.611f, 3.519f, 8.281f, 3.519f)
                horizontalLineTo(3.469f)
                curveTo(3.139f, 3.519f, 2.875f, 3.788f, 2.875f, 4.115f)
                verticalLineTo(5.846f)
                curveTo(2.875f, 6.092f, 2.677f, 6.288f, 2.438f, 6.288f)
                curveTo(2.198f, 6.288f, 2f, 6.092f, 2f, 5.846f)
                verticalLineTo(1.692f)
                close()
            }
        }.build()

        return _ContextCopyNoteText!!
    }

@Suppress("ObjectPropertyName")
private var _ContextCopyNoteText: ImageVector? = null
