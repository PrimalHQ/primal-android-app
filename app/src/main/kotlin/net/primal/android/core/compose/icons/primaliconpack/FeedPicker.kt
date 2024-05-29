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
        if (_feedPicker != null) {
            return _feedPicker!!
        }
        _feedPicker = Builder(name = "Feed picker", defaultWidth = 24.0.dp, defaultHeight =
                24.0.dp, viewportWidth = 24.0f, viewportHeight = 24.0f).apply {
            path(fill = SolidColor(Color(0xFFAAAAAA)), stroke = null, strokeLineWidth = 0.0f,
                    strokeLineCap = Butt, strokeLineJoin = Miter, strokeLineMiter = 4.0f,
                    pathFillType = NonZero) {
                moveTo(7.25f, 2.0f)
                curveTo(7.25f, 1.5858f, 7.5858f, 1.25f, 8.0f, 1.25f)
                horizontalLineTo(16.0f)
                curveTo(16.4142f, 1.25f, 16.75f, 1.5858f, 16.75f, 2.0f)
                curveTo(16.75f, 2.4142f, 16.4142f, 2.75f, 16.0f, 2.75f)
                horizontalLineTo(8.0f)
                curveTo(7.5858f, 2.75f, 7.25f, 2.4142f, 7.25f, 2.0f)
                close()
            }
            path(fill = SolidColor(Color(0xFFAAAAAA)), stroke = null, strokeLineWidth = 0.0f,
                    strokeLineCap = Butt, strokeLineJoin = Miter, strokeLineMiter = 4.0f,
                    pathFillType = NonZero) {
                moveTo(4.25f, 7.0f)
                curveTo(4.25f, 6.5858f, 4.5858f, 6.25f, 5.0f, 6.25f)
                horizontalLineTo(19.0f)
                curveTo(19.4142f, 6.25f, 19.75f, 6.5858f, 19.75f, 7.0f)
                curveTo(19.75f, 7.4142f, 19.4142f, 7.75f, 19.0f, 7.75f)
                horizontalLineTo(5.0f)
                curveTo(4.5858f, 7.75f, 4.25f, 7.4142f, 4.25f, 7.0f)
                close()
            }
            path(fill = SolidColor(Color(0xFFAAAAAA)), stroke = null, strokeLineWidth = 0.0f,
                    strokeLineCap = Butt, strokeLineJoin = Miter, strokeLineMiter = 4.0f,
                    pathFillType = EvenOdd) {
                moveTo(0.25f, 14.0f)
                curveTo(0.25f, 12.4813f, 1.4813f, 11.25f, 3.0f, 11.25f)
                horizontalLineTo(21.0f)
                curveTo(22.5187f, 11.25f, 23.75f, 12.4813f, 23.75f, 14.0f)
                verticalLineTo(20.0f)
                curveTo(23.75f, 21.5187f, 22.5187f, 22.75f, 21.0f, 22.75f)
                horizontalLineTo(3.0f)
                curveTo(1.4813f, 22.75f, 0.25f, 21.5187f, 0.25f, 20.0f)
                verticalLineTo(14.0f)
                close()
                moveTo(3.5f, 12.75f)
                curveTo(2.5333f, 12.75f, 1.75f, 13.5333f, 1.75f, 14.5f)
                verticalLineTo(19.5f)
                curveTo(1.75f, 20.4667f, 2.5333f, 21.25f, 3.5f, 21.25f)
                horizontalLineTo(20.5f)
                curveTo(21.4667f, 21.25f, 22.25f, 20.4667f, 22.25f, 19.5f)
                verticalLineTo(14.5f)
                curveTo(22.25f, 13.5333f, 21.4667f, 12.75f, 20.5f, 12.75f)
                horizontalLineTo(3.5f)
                close()
            }
        }
        .build()
        return _feedPicker!!
    }

private var _feedPicker: ImageVector? = null
