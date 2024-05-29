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

public val PrimalIcons.Bookmarks: ImageVector
    get() {
        if (_bookmarks != null) {
            return _bookmarks!!
        }
        _bookmarks = Builder(name = "Bookmarks", defaultWidth = 24.0.dp, defaultHeight = 24.0.dp,
                viewportWidth = 24.0f, viewportHeight = 24.0f).apply {
            path(fill = SolidColor(Color(0xFFAAAAAA)), stroke = null, strokeLineWidth = 0.0f,
                    strokeLineCap = Butt, strokeLineJoin = Miter, strokeLineMiter = 4.0f,
                    pathFillType = EvenOdd) {
                moveTo(3.75f, 3.1207f)
                curveTo(3.75f, 1.5259f, 4.9904f, 0.25f, 6.5f, 0.25f)
                horizontalLineTo(17.5f)
                curveTo(19.0096f, 0.25f, 20.25f, 1.5259f, 20.25f, 3.1207f)
                verticalLineTo(22.958f)
                curveTo(20.25f, 23.6156f, 19.5534f, 23.9603f, 19.0713f, 23.6063f)
                lineTo(12.2921f, 18.6285f)
                curveTo(12.1177f, 18.5004f, 11.8823f, 18.5004f, 11.7079f, 18.6285f)
                lineTo(4.9287f, 23.6063f)
                curveTo(4.4466f, 23.9603f, 3.75f, 23.6156f, 3.75f, 22.958f)
                verticalLineTo(3.1207f)
                close()
                moveTo(6.5f, 1.8305f)
                curveTo(5.8004f, 1.8305f, 5.25f, 2.4175f, 5.25f, 3.1207f)
                verticalLineTo(21.4441f)
                lineTo(10.8504f, 17.3318f)
                curveTo(11.5403f, 16.8253f, 12.4597f, 16.8253f, 13.1496f, 17.3318f)
                lineTo(18.75f, 21.4441f)
                verticalLineTo(3.1207f)
                curveTo(18.75f, 2.4175f, 18.1996f, 1.8305f, 17.5f, 1.8305f)
                horizontalLineTo(6.5f)
                close()
            }
        }
        .build()
        return _bookmarks!!
    }

private var _bookmarks: ImageVector? = null
