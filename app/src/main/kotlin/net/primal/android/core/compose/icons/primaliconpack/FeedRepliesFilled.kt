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

public val PrimalIcons.FeedRepliesFilled: ImageVector
    get() {
        if (_feedRepliesFilled != null) {
            return _feedRepliesFilled!!
        }
        _feedRepliesFilled = Builder(name = "Feedrepliesfilled", defaultWidth = 18.0.dp,
                defaultHeight = 18.0.dp, viewportWidth = 18.0f, viewportHeight = 18.0f).apply {
            path(fill = SolidColor(Color(0xFFCCCCCC)), stroke = null, strokeLineWidth = 0.0f,
                    strokeLineCap = Butt, strokeLineJoin = Miter, strokeLineMiter = 4.0f,
                    pathFillType = EvenOdd) {
                moveTo(4.8828f, 17.8624f)
                curveTo(4.6309f, 18.1563f, 4.2f, 17.9482f, 4.2f, 17.5326f)
                lineTo(4.2f, 13.0611f)
                curveTo(2.4327f, 13.0611f, 1.0f, 11.3904f, 1.0f, 9.3294f)
                verticalLineTo(4.7318f)
                curveTo(1.0f, 2.6708f, 2.4327f, 1.0f, 4.2f, 1.0f)
                horizontalLineTo(14.8f)
                curveTo(16.5673f, 1.0f, 18.0f, 2.6708f, 18.0f, 4.7318f)
                verticalLineTo(9.3294f)
                curveTo(18.0f, 11.3904f, 16.5673f, 13.0611f, 14.8f, 13.0611f)
                horizontalLineTo(9.0f)
                lineTo(4.8828f, 17.8624f)
                close()
            }
        }
        .build()
        return _feedRepliesFilled!!
    }

private var _feedRepliesFilled: ImageVector? = null
