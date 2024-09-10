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

public val PrimalIcons.AdvancedSearch: ImageVector
    get() {
        if (_icAsearch != null) {
            return _icAsearch!!
        }
        _icAsearch = Builder(name = "IcAsearch", defaultWidth = 20.0.dp, defaultHeight = 20.0.dp,
                viewportWidth = 20.0f, viewportHeight = 20.0f).apply {
            path(fill = SolidColor(Color(0xFFAAAAAA)), stroke = null, strokeLineWidth = 0.0f,
                    strokeLineCap = Butt, strokeLineJoin = Miter, strokeLineMiter = 4.0f,
                    pathFillType = NonZero) {
                moveTo(7.625f, 4.5f)
                lineTo(19.375f, 4.5f)
                arcTo(0.625f, 0.625f, 0.0f, false, true, 20.0f, 5.125f)
                lineTo(20.0f, 5.125f)
                arcTo(0.625f, 0.625f, 0.0f, false, true, 19.375f, 5.75f)
                lineTo(7.625f, 5.75f)
                arcTo(0.625f, 0.625f, 0.0f, false, true, 7.0f, 5.125f)
                lineTo(7.0f, 5.125f)
                arcTo(0.625f, 0.625f, 0.0f, false, true, 7.625f, 4.5f)
                close()
            }
            path(fill = SolidColor(Color(0x00000000)), stroke = SolidColor(Color(0xFFAAAAAA)),
                    strokeLineWidth = 1.25f, strokeLineCap = Butt, strokeLineJoin = Miter,
                    strokeLineMiter = 4.0f, pathFillType = NonZero) {
                moveTo(4.0f, 5.0f)
                moveToRelative(-3.375f, 0.0f)
                arcToRelative(3.375f, 3.375f, 0.0f, true, true, 6.75f, 0.0f)
                arcToRelative(3.375f, 3.375f, 0.0f, true, true, -6.75f, 0.0f)
            }
            path(fill = SolidColor(Color(0xFFAAAAAA)), stroke = null, strokeLineWidth = 0.0f,
                    strokeLineCap = Butt, strokeLineJoin = Miter, strokeLineMiter = 4.0f,
                    pathFillType = NonZero) {
                moveTo(1.625f, 14.5f)
                lineTo(12.375f, 14.5f)
                arcTo(0.625f, 0.625f, 0.0f, false, true, 13.0f, 15.125f)
                lineTo(13.0f, 15.125f)
                arcTo(0.625f, 0.625f, 0.0f, false, true, 12.375f, 15.75f)
                lineTo(1.625f, 15.75f)
                arcTo(0.625f, 0.625f, 0.0f, false, true, 1.0f, 15.125f)
                lineTo(1.0f, 15.125f)
                arcTo(0.625f, 0.625f, 0.0f, false, true, 1.625f, 14.5f)
                close()
            }
            path(fill = SolidColor(Color(0x00000000)), stroke = SolidColor(Color(0xFFAAAAAA)),
                    strokeLineWidth = 1.25f, strokeLineCap = Butt, strokeLineJoin = Miter,
                    strokeLineMiter = 4.0f, pathFillType = NonZero) {
                moveTo(16.0f, 15.0f)
                moveToRelative(-3.375f, 0.0f)
                arcToRelative(3.375f, 3.375f, 0.0f, true, true, 6.75f, 0.0f)
                arcToRelative(3.375f, 3.375f, 0.0f, true, true, -6.75f, 0.0f)
            }
        }
        .build()
        return _icAsearch!!
    }

private var _icAsearch: ImageVector? = null
