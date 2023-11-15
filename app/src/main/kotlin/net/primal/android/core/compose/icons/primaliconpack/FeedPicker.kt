@file:Suppress("MagicNumber")

package net.primal.android.core.compose.icons.primaliconpack

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType.Companion.EvenOdd
import androidx.compose.ui.graphics.PathFillType.Companion.NonZero
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap.Companion.Butt
import androidx.compose.ui.graphics.StrokeJoin.Companion.Miter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.ImageVector.Builder
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp
import net.primal.android.core.compose.icons.PrimalIcons

public val PrimalIcons.FeedPicker: ImageVector
    get() {
        if (_FeedPicker != null) {
            return _FeedPicker!!
        }
        _FeedPicker = Builder(name = "Feed picker", defaultWidth = 24.0.dp, defaultHeight =
                24.0.dp, viewportWidth = 24.0f, viewportHeight = 24.0f).apply {
            path(fill = SolidColor(Color(0xFFAAAAAA)), stroke = null, strokeLineWidth = 0.0f,
                    strokeLineCap = Butt, strokeLineJoin = Miter, strokeLineMiter = 4.0f,
                    pathFillType = NonZero) {
                moveTo(8.0f, 1.0f)
                curveTo(7.4478f, 1.0f, 7.0f, 1.4477f, 7.0f, 2.0f)
                curveTo(7.0f, 2.5522f, 7.4478f, 3.0f, 8.0f, 3.0f)
                horizontalLineTo(16.0f)
                curveTo(16.5522f, 3.0f, 17.0f, 2.5522f, 17.0f, 2.0f)
                curveTo(17.0f, 1.4477f, 16.5522f, 1.0f, 16.0f, 1.0f)
                horizontalLineTo(8.0f)
                close()
            }
            path(fill = SolidColor(Color(0xFFAAAAAA)), stroke = null, strokeLineWidth = 0.0f,
                    strokeLineCap = Butt, strokeLineJoin = Miter, strokeLineMiter = 4.0f,
                    pathFillType = NonZero) {
                moveTo(4.0f, 7.0f)
                curveTo(4.0f, 6.4478f, 4.4478f, 6.0f, 5.0f, 6.0f)
                horizontalLineTo(19.0f)
                curveTo(19.5522f, 6.0f, 20.0f, 6.4478f, 20.0f, 7.0f)
                curveTo(20.0f, 7.5522f, 19.5522f, 8.0f, 19.0f, 8.0f)
                horizontalLineTo(5.0f)
                curveTo(4.4478f, 8.0f, 4.0f, 7.5522f, 4.0f, 7.0f)
                close()
            }
            path(fill = SolidColor(Color(0xFFAAAAAA)), stroke = null, strokeLineWidth = 0.0f,
                    strokeLineCap = Butt, strokeLineJoin = Miter, strokeLineMiter = 4.0f,
                    pathFillType = EvenOdd) {
                moveTo(3.0f, 11.0f)
                curveTo(1.3433f, 11.0f, 0.0f, 12.3433f, 0.0f, 14.0f)
                verticalLineTo(20.0f)
                curveTo(0.0f, 21.6567f, 1.3433f, 23.0f, 3.0f, 23.0f)
                horizontalLineTo(21.0f)
                curveTo(22.6567f, 23.0f, 24.0f, 21.6567f, 24.0f, 20.0f)
                verticalLineTo(14.0f)
                curveTo(24.0f, 12.3433f, 22.6567f, 11.0f, 21.0f, 11.0f)
                horizontalLineTo(3.0f)
                close()
                moveTo(20.5f, 21.0f)
                curveTo(21.3286f, 21.0f, 22.0f, 20.3286f, 22.0f, 19.5f)
                verticalLineTo(14.5f)
                curveTo(22.0f, 13.6714f, 21.3286f, 13.0f, 20.5f, 13.0f)
                horizontalLineTo(3.5f)
                curveTo(2.6714f, 13.0f, 2.0f, 13.6714f, 2.0f, 14.5f)
                verticalLineTo(19.5f)
                curveTo(2.0f, 20.3286f, 2.6714f, 21.0f, 3.5f, 21.0f)
                horizontalLineTo(20.5f)
                close()
            }
        }
        .build()
        return _FeedPicker!!
    }

private var _FeedPicker: ImageVector? = null
