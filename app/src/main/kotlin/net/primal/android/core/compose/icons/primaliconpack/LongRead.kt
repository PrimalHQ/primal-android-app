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

public val PrimalIcons.LongRead: ImageVector
    get() {
        if (_longread != null) {
            return _longread!!
        }
        _longread = Builder(name = "Longread", defaultWidth = 24.0.dp, defaultHeight = 24.0.dp,
                viewportWidth = 24.0f, viewportHeight = 24.0f).apply {
            path(fill = SolidColor(Color(0xFFAAAAAA)), stroke = null, strokeLineWidth = 0.0f,
                    strokeLineCap = Butt, strokeLineJoin = Miter, strokeLineMiter = 4.0f,
                    pathFillType = NonZero) {
                moveTo(0.75f, 3.0f)
                curveTo(0.75f, 2.5858f, 1.0858f, 2.25f, 1.5f, 2.25f)
                horizontalLineTo(22.5f)
                curveTo(22.9142f, 2.25f, 23.25f, 2.5858f, 23.25f, 3.0f)
                curveTo(23.25f, 3.4142f, 22.9142f, 3.75f, 22.5f, 3.75f)
                horizontalLineTo(1.5f)
                curveTo(1.0858f, 3.75f, 0.75f, 3.4142f, 0.75f, 3.0f)
                close()
            }
            path(fill = SolidColor(Color(0xFFAAAAAA)), stroke = null, strokeLineWidth = 0.0f,
                    strokeLineCap = Butt, strokeLineJoin = Miter, strokeLineMiter = 4.0f,
                    pathFillType = NonZero) {
                moveTo(0.75f, 9.0f)
                curveTo(0.75f, 8.5858f, 1.0858f, 8.25f, 1.5f, 8.25f)
                horizontalLineTo(22.5f)
                curveTo(22.9142f, 8.25f, 23.25f, 8.5858f, 23.25f, 9.0f)
                curveTo(23.25f, 9.4142f, 22.9142f, 9.75f, 22.5f, 9.75f)
                horizontalLineTo(1.5f)
                curveTo(1.0858f, 9.75f, 0.75f, 9.4142f, 0.75f, 9.0f)
                close()
            }
            path(fill = SolidColor(Color(0xFFAAAAAA)), stroke = null, strokeLineWidth = 0.0f,
                    strokeLineCap = Butt, strokeLineJoin = Miter, strokeLineMiter = 4.0f,
                    pathFillType = NonZero) {
                moveTo(0.75f, 15.0f)
                curveTo(0.75f, 14.5858f, 1.0858f, 14.25f, 1.5f, 14.25f)
                horizontalLineTo(22.5f)
                curveTo(22.9142f, 14.25f, 23.25f, 14.5858f, 23.25f, 15.0f)
                curveTo(23.25f, 15.4142f, 22.9142f, 15.75f, 22.5f, 15.75f)
                horizontalLineTo(1.5f)
                curveTo(1.0858f, 15.75f, 0.75f, 15.4142f, 0.75f, 15.0f)
                close()
            }
            path(fill = SolidColor(Color(0xFFAAAAAA)), stroke = null, strokeLineWidth = 0.0f,
                    strokeLineCap = Butt, strokeLineJoin = Miter, strokeLineMiter = 4.0f,
                    pathFillType = NonZero) {
                moveTo(0.75f, 21.0f)
                curveTo(0.75f, 20.5858f, 1.0858f, 20.25f, 1.5f, 20.25f)
                horizontalLineTo(14.5f)
                curveTo(14.9142f, 20.25f, 15.25f, 20.5858f, 15.25f, 21.0f)
                curveTo(15.25f, 21.4142f, 14.9142f, 21.75f, 14.5f, 21.75f)
                horizontalLineTo(1.5f)
                curveTo(1.0858f, 21.75f, 0.75f, 21.4142f, 0.75f, 21.0f)
                close()
            }
        }
        .build()
        return _longread!!
    }

private var _longread: ImageVector? = null
