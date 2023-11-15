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

public val PrimalIcons.Verified: ImageVector
    get() {
        if (_verified != null) {
            return _verified!!
        }
        _verified = Builder(name = "Verified", defaultWidth = 13.0.dp, defaultHeight = 12.0.dp,
                viewportWidth = 13.0f, viewportHeight = 12.0f).apply {
            path(fill = SolidColor(Color(0xFFffffff)), stroke = null, strokeLineWidth = 0.0f,
                    strokeLineCap = Butt, strokeLineJoin = Miter, strokeLineMiter = 4.0f,
                    pathFillType = EvenOdd) {
                moveTo(8.5094f, 0.0f)
                lineTo(9.5331f, 1.9497f)
                lineTo(11.759f, 2.2918f)
                lineTo(11.4059f, 4.4533f)
                lineTo(13.0f, 6.0002f)
                lineTo(11.4061f, 7.5473f)
                lineTo(11.7585f, 9.7082f)
                lineTo(9.532f, 10.0503f)
                lineTo(8.5084f, 12.0f)
                lineTo(6.4994f, 11.0066f)
                lineTo(4.4919f, 11.9996f)
                lineTo(3.4689f, 10.0502f)
                lineTo(1.2413f, 9.7082f)
                lineTo(1.5941f, 7.5473f)
                lineTo(0.0f, 6.0004f)
                lineTo(1.5939f, 4.4532f)
                lineTo(1.2415f, 2.2924f)
                lineTo(3.4678f, 1.9498f)
                lineTo(4.4911f, 8.0E-4f)
                lineTo(6.4994f, 0.9939f)
                lineTo(8.5094f, 0.0f)
                close()
                moveTo(4.1167f, 5.7277f)
                lineTo(3.25f, 6.6004f)
                lineTo(5.4167f, 8.7823f)
                lineTo(9.75f, 4.4186f)
                lineTo(8.8833f, 3.5459f)
                lineTo(5.4167f, 7.0368f)
                lineTo(4.1167f, 5.7277f)
                close()
            }
        }
        .build()
        return _verified!!
    }

private var _verified: ImageVector? = null
