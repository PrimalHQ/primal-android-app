@file:Suppress("MagicNumber")

package net.primal.android.core.compose.icons.primaliconpack

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType.Companion.NonZero
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap.Companion.Butt
import androidx.compose.ui.graphics.StrokeJoin.Companion.Miter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.ImageVector.Builder
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp
import net.primal.android.core.compose.icons.PrimalIcons

public val PrimalIcons.ContextCopyNoteText: ImageVector
    get() {
        if (_contextCopyNoteText != null) {
            return _contextCopyNoteText!!
        }
        _contextCopyNoteText = Builder(name = "ContextCopyNoteText", defaultWidth = 20.0.dp,
                defaultHeight = 20.0.dp, viewportWidth = 20.0f, viewportHeight = 20.0f).apply {
            path(fill = SolidColor(Color(0xFFffffff)), stroke = null, strokeLineWidth = 0.0f,
                    strokeLineCap = Butt, strokeLineJoin = Miter, strokeLineMiter = 4.0f,
                    pathFillType = NonZero) {
                moveTo(13.0f, 1.5f)
                horizontalLineTo(2.0f)
                curveTo(1.7239f, 1.5f, 1.5f, 1.7239f, 1.5f, 2.0f)
                verticalLineTo(13.0f)
                curveTo(1.5f, 13.2761f, 1.7239f, 13.5f, 2.0f, 13.5f)
                horizontalLineTo(7.25f)
                curveTo(7.6642f, 13.5f, 8.0f, 13.8358f, 8.0f, 14.25f)
                curveTo(8.0f, 14.6642f, 7.6642f, 15.0f, 7.25f, 15.0f)
                horizontalLineTo(2.0f)
                curveTo(0.8954f, 15.0f, 0.0f, 14.1046f, 0.0f, 13.0f)
                verticalLineTo(2.0f)
                curveTo(0.0f, 0.8954f, 0.8954f, 0.0f, 2.0f, 0.0f)
                horizontalLineTo(13.0f)
                curveTo(14.1046f, 0.0f, 15.0f, 0.8954f, 15.0f, 2.0f)
                verticalLineTo(2.25f)
                curveTo(15.0f, 2.6642f, 14.6642f, 3.0f, 14.25f, 3.0f)
                curveTo(13.8358f, 3.0f, 13.5f, 2.6642f, 13.5f, 2.25f)
                verticalLineTo(2.0f)
                curveTo(13.5f, 1.7239f, 13.2761f, 1.5f, 13.0f, 1.5f)
                close()
            }
            path(fill = SolidColor(Color(0xFFffffff)), stroke = null, strokeLineWidth = 0.0f,
                    strokeLineCap = Butt, strokeLineJoin = Miter, strokeLineMiter = 4.0f,
                    pathFillType = NonZero) {
                moveTo(6.5f, 5.0f)
                curveTo(6.2239f, 5.0f, 6.0f, 5.2239f, 6.0f, 5.5f)
                verticalLineTo(8.5f)
                curveTo(6.0f, 8.7761f, 6.2239f, 9.0f, 6.5f, 9.0f)
                curveTo(6.7761f, 9.0f, 7.0f, 8.7761f, 7.0f, 8.5f)
                verticalLineTo(7.25f)
                curveTo(7.0f, 7.1119f, 7.1119f, 7.0f, 7.25f, 7.0f)
                horizontalLineTo(10.75f)
                curveTo(10.8881f, 7.0f, 11.0f, 7.1119f, 11.0f, 7.25f)
                verticalLineTo(16.75f)
                curveTo(11.0f, 16.8881f, 10.8881f, 17.0f, 10.75f, 17.0f)
                horizontalLineTo(9.5f)
                curveTo(9.2239f, 17.0f, 9.0f, 17.2239f, 9.0f, 17.5f)
                curveTo(9.0f, 17.7761f, 9.2239f, 18.0f, 9.5f, 18.0f)
                horizontalLineTo(14.5f)
                curveTo(14.7761f, 18.0f, 15.0f, 17.7761f, 15.0f, 17.5f)
                curveTo(15.0f, 17.2239f, 14.7761f, 17.0f, 14.5f, 17.0f)
                horizontalLineTo(13.25f)
                curveTo(13.1119f, 17.0f, 13.0f, 16.8881f, 13.0f, 16.75f)
                verticalLineTo(7.25f)
                curveTo(13.0f, 7.1119f, 13.1119f, 7.0f, 13.25f, 7.0f)
                horizontalLineTo(16.75f)
                curveTo(16.8881f, 7.0f, 17.0f, 7.1119f, 17.0f, 7.25f)
                verticalLineTo(8.5f)
                curveTo(17.0f, 8.7761f, 17.2239f, 9.0f, 17.5f, 9.0f)
                curveTo(17.7761f, 9.0f, 18.0f, 8.7761f, 18.0f, 8.5f)
                verticalLineTo(5.5f)
                curveTo(18.0f, 5.2239f, 17.7761f, 5.0f, 17.5f, 5.0f)
                horizontalLineTo(6.5f)
                close()
            }
        }
        .build()
        return _contextCopyNoteText!!
    }

private var _contextCopyNoteText: ImageVector? = null
