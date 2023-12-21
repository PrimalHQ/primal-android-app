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

public val PrimalIcons.NavMessages: ImageVector
    get() {
        if (_navMessages != null) {
            return _navMessages!!
        }
        _navMessages = Builder(name = "Navmessages", defaultWidth = 24.0.dp, defaultHeight =
                24.0.dp, viewportWidth = 24.0f, viewportHeight = 24.0f).apply {
            path(fill = SolidColor(Color(0xFFAAAAAA)), stroke = null, strokeLineWidth = 0.0f,
                    strokeLineCap = Butt, strokeLineJoin = Miter, strokeLineMiter = 4.0f,
                    pathFillType = EvenOdd) {
                moveTo(0.0f, 6.0f)
                curveTo(0.0f, 3.7909f, 1.7909f, 2.0f, 4.0f, 2.0f)
                horizontalLineTo(20.0f)
                curveTo(22.2091f, 2.0f, 24.0f, 3.7909f, 24.0f, 6.0f)
                verticalLineTo(18.0f)
                curveTo(24.0f, 20.2091f, 22.2091f, 22.0f, 20.0f, 22.0f)
                horizontalLineTo(4.0f)
                curveTo(1.7909f, 22.0f, 0.0f, 20.2091f, 0.0f, 18.0f)
                verticalLineTo(6.0f)
                close()
                moveTo(4.0f, 4.0f)
                horizontalLineTo(20.0f)
                curveTo(20.468f, 4.0f, 20.8984f, 4.1607f, 21.2391f, 4.43f)
                curveTo(21.4584f, 4.6033f, 21.4094f, 4.9276f, 21.1838f, 5.0927f)
                lineTo(12.5906f, 11.3824f)
                curveTo(12.239f, 11.6398f, 11.7611f, 11.6398f, 11.4094f, 11.3824f)
                lineTo(2.8162f, 5.0927f)
                curveTo(2.5906f, 4.9276f, 2.5416f, 4.6033f, 2.7609f, 4.43f)
                curveTo(3.1016f, 4.1607f, 3.532f, 4.0f, 4.0f, 4.0f)
                close()
                moveTo(2.3965f, 7.2416f)
                curveTo(2.2312f, 7.122f, 2.0f, 7.2401f, 2.0f, 7.4441f)
                verticalLineTo(18.0f)
                curveTo(2.0f, 19.1046f, 2.8954f, 20.0f, 4.0f, 20.0f)
                horizontalLineTo(20.0f)
                curveTo(21.1046f, 20.0f, 22.0f, 19.1046f, 22.0f, 18.0f)
                verticalLineTo(7.4442f)
                curveTo(22.0f, 7.2402f, 21.7688f, 7.122f, 21.6035f, 7.2416f)
                lineTo(13.1719f, 13.338f)
                curveTo(12.4725f, 13.8437f, 11.5276f, 13.8437f, 10.8282f, 13.338f)
                lineTo(2.3965f, 7.2416f)
                close()
            }
        }
        .build()
        return _navMessages!!
    }

private var _navMessages: ImageVector? = null
