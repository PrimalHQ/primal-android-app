package net.primal.android.core.compose.icons.primaliconpack

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType.Companion.EvenOdd
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap.Companion.Butt
import androidx.compose.ui.graphics.StrokeJoin.Companion.Miter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.ImageVector.Builder
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp
import net.primal.android.core.compose.icons.PrimalIcons

public val PrimalIcons.BookmarksFilled: ImageVector
    get() {
        if (_bookmarksfilled != null) {
            return _bookmarksfilled!!
        }
        _bookmarksfilled = Builder(name = "Bookmarksfilled", defaultWidth = 24.0.dp, defaultHeight =
                24.0.dp, viewportWidth = 24.0f, viewportHeight = 24.0f).apply {
            path(fill = SolidColor(Color(0xFFffffff)), stroke = null, strokeLineWidth = 0.0f,
                    strokeLineCap = Butt, strokeLineJoin = Miter, strokeLineMiter = 4.0f,
                    pathFillType = EvenOdd) {
                moveTo(6.5f, 0.0f)
                curveTo(4.8432f, 0.0f, 3.5f, 1.3972f, 3.5f, 3.1207f)
                verticalLineTo(22.958f)
                curveTo(3.5f, 23.8014f, 4.4143f, 24.2942f, 5.0767f, 23.8078f)
                lineTo(11.8558f, 18.83f)
                curveTo(11.9423f, 18.7665f, 12.0577f, 18.7665f, 12.1442f, 18.83f)
                lineTo(18.9233f, 23.8078f)
                curveTo(19.5857f, 24.2942f, 20.5f, 23.8014f, 20.5f, 22.958f)
                verticalLineTo(3.1207f)
                curveTo(20.5f, 1.3972f, 19.1569f, 0.0f, 17.5f, 0.0f)
                horizontalLineTo(6.5f)
                close()
            }
        }
        .build()
        return _bookmarksfilled!!
    }

private var _bookmarksfilled: ImageVector? = null
