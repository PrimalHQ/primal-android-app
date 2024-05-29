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

public val PrimalIcons.Search: ImageVector
    get() {
        if (_search != null) {
            return _search!!
        }
        _search = Builder(name = "Search", defaultWidth = 24.0.dp, defaultHeight = 24.0.dp,
                viewportWidth = 24.0f, viewportHeight = 24.0f).apply {
            path(fill = SolidColor(Color(0xFFAAAAAA)), stroke = null, strokeLineWidth = 0.0f,
                    strokeLineCap = Butt, strokeLineJoin = Miter, strokeLineMiter = 4.0f,
                    pathFillType = EvenOdd) {
                moveTo(0.25f, 9.5f)
                curveTo(0.25f, 4.3914f, 4.3914f, 0.25f, 9.5f, 0.25f)
                curveTo(14.6086f, 0.25f, 18.75f, 4.3914f, 18.75f, 9.5f)
                curveTo(18.75f, 11.3929f, 18.1817f, 13.1523f, 17.2064f, 14.6178f)
                lineTo(17.093f, 14.7883f)
                lineTo(22.4941f, 20.1894f)
                curveTo(23.1305f, 20.8258f, 23.1305f, 21.8577f, 22.4941f, 22.4941f)
                curveTo(21.8577f, 23.1305f, 20.8258f, 23.1305f, 20.1894f, 22.4941f)
                lineTo(14.7883f, 17.093f)
                lineTo(14.6178f, 17.2064f)
                curveTo(13.1523f, 18.1817f, 11.3929f, 18.75f, 9.5f, 18.75f)
                curveTo(4.3914f, 18.75f, 0.25f, 14.6086f, 0.25f, 9.5f)
                close()
                moveTo(9.5f, 1.75f)
                curveTo(5.2198f, 1.75f, 1.75f, 5.2198f, 1.75f, 9.5f)
                curveTo(1.75f, 13.7802f, 5.2198f, 17.25f, 9.5f, 17.25f)
                curveTo(13.7802f, 17.25f, 17.25f, 13.7802f, 17.25f, 9.5f)
                curveTo(17.25f, 5.2198f, 13.7802f, 1.75f, 9.5f, 1.75f)
                close()
            }
        }
        .build()
        return _search!!
    }

private var _search: ImageVector? = null
