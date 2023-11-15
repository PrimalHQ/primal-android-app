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

public val PrimalIcons.Report: ImageVector
    get() {
        if (_report != null) {
            return _report!!
        }
        _report = Builder(name = "Report", defaultWidth = 20.0.dp, defaultHeight = 20.0.dp,
                viewportWidth = 20.0f, viewportHeight = 20.0f).apply {
            path(fill = SolidColor(Color(0xFFFA3C3C)), stroke = null, strokeLineWidth = 0.0f,
                    strokeLineCap = Butt, strokeLineJoin = Miter, strokeLineMiter = 4.0f,
                    pathFillType = NonZero) {
                moveTo(11.1111f, 14.4444f)
                curveTo(11.1111f, 15.0581f, 10.6137f, 15.5556f, 10.0f, 15.5556f)
                curveTo(9.3863f, 15.5556f, 8.8889f, 15.0581f, 8.8889f, 14.4444f)
                curveTo(8.8889f, 13.8308f, 9.3863f, 13.3333f, 10.0f, 13.3333f)
                curveTo(10.6137f, 13.3333f, 11.1111f, 13.8308f, 11.1111f, 14.4444f)
                close()
            }
            path(fill = SolidColor(Color(0xFFFA3C3C)), stroke = null, strokeLineWidth = 0.0f,
                    strokeLineCap = Butt, strokeLineJoin = Miter, strokeLineMiter = 4.0f,
                    pathFillType = NonZero) {
                moveTo(10.0f, 4.4444f)
                curveTo(9.3863f, 4.4444f, 8.8889f, 4.9419f, 8.8889f, 5.5556f)
                verticalLineTo(11.1111f)
                curveTo(8.8889f, 11.7248f, 9.3863f, 12.2222f, 10.0f, 12.2222f)
                curveTo(10.6137f, 12.2222f, 11.1111f, 11.7248f, 11.1111f, 11.1111f)
                verticalLineTo(5.5556f)
                curveTo(11.1111f, 4.9419f, 10.6137f, 4.4444f, 10.0f, 4.4444f)
                close()
            }
            path(fill = SolidColor(Color(0xFFFA3C3C)), stroke = null, strokeLineWidth = 0.0f,
                    strokeLineCap = Butt, strokeLineJoin = Miter, strokeLineMiter = 4.0f,
                    pathFillType = EvenOdd) {
                moveTo(10.0f, 20.0f)
                curveTo(15.5228f, 20.0f, 20.0f, 15.5228f, 20.0f, 10.0f)
                curveTo(20.0f, 4.4771f, 15.5228f, 0.0f, 10.0f, 0.0f)
                curveTo(4.4771f, 0.0f, 0.0f, 4.4771f, 0.0f, 10.0f)
                curveTo(0.0f, 15.5228f, 4.4771f, 20.0f, 10.0f, 20.0f)
                close()
                moveTo(10.0f, 18.3333f)
                curveTo(14.6024f, 18.3333f, 18.3333f, 14.6024f, 18.3333f, 10.0f)
                curveTo(18.3333f, 5.3976f, 14.6024f, 1.6667f, 10.0f, 1.6667f)
                curveTo(5.3976f, 1.6667f, 1.6667f, 5.3976f, 1.6667f, 10.0f)
                curveTo(1.6667f, 14.6024f, 5.3976f, 18.3333f, 10.0f, 18.3333f)
                close()
            }
        }
        .build()
        return _report!!
    }

private var _report: ImageVector? = null
