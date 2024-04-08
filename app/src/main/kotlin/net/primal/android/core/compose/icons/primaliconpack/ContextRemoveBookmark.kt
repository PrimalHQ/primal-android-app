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

public val PrimalIcons.ContextRemoveBookmark: ImageVector
    get() {
        if (_bookmarksfilled != null) {
            return _bookmarksfilled!!
        }
        _bookmarksfilled = Builder(name = "Bookmarksfilled", defaultWidth = 20.0.dp, defaultHeight =
                20.0.dp, viewportWidth = 20.0f, viewportHeight = 20.0f).apply {
            path(fill = SolidColor(Color(0xFFffffff)), stroke = null, strokeLineWidth = 0.0f,
                    strokeLineCap = Butt, strokeLineJoin = Miter, strokeLineMiter = 4.0f,
                    pathFillType = EvenOdd) {
                moveTo(5.4166f, 0.0f)
                curveTo(4.0359f, 0.0f, 2.9166f, 1.1643f, 2.9166f, 2.6006f)
                verticalLineTo(19.1316f)
                curveTo(2.9166f, 19.8345f, 3.6785f, 20.2451f, 4.2305f, 19.8398f)
                lineTo(9.8798f, 15.6917f)
                curveTo(9.9518f, 15.6388f, 10.0481f, 15.6388f, 10.1201f, 15.6917f)
                lineTo(15.7694f, 19.8398f)
                curveTo(16.3214f, 20.2451f, 17.0833f, 19.8345f, 17.0833f, 19.1316f)
                verticalLineTo(2.6006f)
                curveTo(17.0833f, 1.1643f, 15.964f, 0.0f, 14.5833f, 0.0f)
                horizontalLineTo(5.4166f)
                close()
            }
        }
        .build()
        return _bookmarksfilled!!
    }

private var _bookmarksfilled: ImageVector? = null
