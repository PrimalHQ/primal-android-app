@file:Suppress("MagicNumber")

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
                moveTo(14.7563f, 17.4146f)
                curveTo(13.251f, 18.4163f, 11.4437f, 19.0f, 9.5f, 19.0f)
                curveTo(4.2533f, 19.0f, 0.0f, 14.7467f, 0.0f, 9.5f)
                curveTo(0.0f, 4.2533f, 4.2533f, 0.0f, 9.5f, 0.0f)
                curveTo(14.7467f, 0.0f, 19.0f, 4.2533f, 19.0f, 9.5f)
                curveTo(19.0f, 11.4437f, 18.4163f, 13.251f, 17.4146f, 14.7563f)
                lineTo(22.6709f, 20.0126f)
                curveTo(23.4049f, 20.7467f, 23.4049f, 21.9368f, 22.6709f, 22.6709f)
                curveTo(21.9368f, 23.4049f, 20.7467f, 23.4049f, 20.0126f, 22.6709f)
                lineTo(14.7563f, 17.4146f)
                close()
                moveTo(17.0f, 9.5f)
                curveTo(17.0f, 13.6421f, 13.6421f, 17.0f, 9.5f, 17.0f)
                curveTo(5.3579f, 17.0f, 2.0f, 13.6421f, 2.0f, 9.5f)
                curveTo(2.0f, 5.3579f, 5.3579f, 2.0f, 9.5f, 2.0f)
                curveTo(13.6421f, 2.0f, 17.0f, 5.3579f, 17.0f, 9.5f)
                close()
            }
        }
        .build()
        return _search!!
    }

private var _search: ImageVector? = null
