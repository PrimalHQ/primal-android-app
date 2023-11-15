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

public val PrimalIcons.ContextCopyNoteId: ImageVector
    get() {
        if (_contextCopyNoteId != null) {
            return _contextCopyNoteId!!
        }
        _contextCopyNoteId = Builder(name = "ContextCopyNoteId", defaultWidth = 20.0.dp,
                defaultHeight = 20.0.dp, viewportWidth = 20.0f, viewportHeight = 20.0f).apply {
            path(fill = SolidColor(Color(0xFFffffff)), stroke = null, strokeLineWidth = 0.0f,
                    strokeLineCap = Butt, strokeLineJoin = Miter, strokeLineMiter = 4.0f,
                    pathFillType = NonZero) {
                moveTo(13.0f, 1.5f)
                horizontalLineTo(2.0f)
                curveTo(1.7239f, 1.5f, 1.5f, 1.7239f, 1.5f, 2.0f)
                verticalLineTo(13.0f)
                curveTo(1.5f, 13.2761f, 1.7239f, 13.5f, 2.0f, 13.5f)
                horizontalLineTo(3.25f)
                curveTo(3.6642f, 13.5f, 4.0f, 13.8358f, 4.0f, 14.25f)
                curveTo(4.0f, 14.6642f, 3.6642f, 15.0f, 3.25f, 15.0f)
                horizontalLineTo(2.0f)
                curveTo(0.8954f, 15.0f, 0.0f, 14.1046f, 0.0f, 13.0f)
                verticalLineTo(2.0f)
                curveTo(0.0f, 0.8954f, 0.8954f, 0.0f, 2.0f, 0.0f)
                horizontalLineTo(13.0f)
                curveTo(14.1046f, 0.0f, 15.0f, 0.8954f, 15.0f, 2.0f)
                verticalLineTo(2.25f)
                curveTo(15.0f, 2.6642f, 14.6642f, 3.0f, 14.25f, 3.0f)
                curveTo(13.8358f, 3.0f, 13.5f, 2.6642f, 13.5f, 2.25f)
                verticalLineTo(2.0f)
                curveTo(13.5f, 1.7239f, 13.2761f, 1.5f, 13.0f, 1.5f)
                close()
            }
            path(fill = SolidColor(Color(0xFFffffff)), stroke = null, strokeLineWidth = 0.0f,
                    strokeLineCap = Butt, strokeLineJoin = Miter, strokeLineMiter = 4.0f,
                    pathFillType = EvenOdd) {
                moveTo(13.136f, 18.0729f)
                curveTo(13.0572f, 18.559f, 13.4325f, 19.0f, 13.925f, 19.0f)
                curveTo(14.3171f, 19.0f, 14.6513f, 18.7156f, 14.714f, 18.3285f)
                lineTo(15.2119f, 15.2539f)
                horizontalLineTo(17.2347f)
                curveTo(17.6376f, 15.2539f, 17.9808f, 14.9613f, 18.0446f, 14.5635f)
                curveTo(18.1246f, 14.065f, 17.7395f, 13.6133f, 17.2347f, 13.6133f)
                horizontalLineTo(15.4776f)
                lineTo(16.0001f, 10.3867f)
                horizontalLineTo(18.0376f)
                curveTo(18.4405f, 10.3867f, 18.7838f, 10.0941f, 18.8476f, 9.6963f)
                curveTo(18.9275f, 9.1978f, 18.5425f, 8.7461f, 18.0376f, 8.7461f)
                horizontalLineTo(16.2658f)
                lineTo(16.7223f, 5.9271f)
                curveTo(16.801f, 5.441f, 16.4257f, 5.0f, 15.9333f, 5.0f)
                curveTo(15.5412f, 5.0f, 15.207f, 5.2844f, 15.1443f, 5.6715f)
                lineTo(14.6464f, 8.7461f)
                horizontalLineTo(11.4075f)
                lineTo(11.864f, 5.9271f)
                curveTo(11.9428f, 5.441f, 11.5675f, 5.0f, 11.075f, 5.0f)
                curveTo(10.6829f, 5.0f, 10.3487f, 5.2844f, 10.286f, 5.6715f)
                lineTo(9.7881f, 8.7461f)
                horizontalLineTo(7.7653f)
                curveTo(7.3624f, 8.7461f, 7.0192f, 9.0387f, 6.9554f, 9.4365f)
                curveTo(6.8754f, 9.935f, 7.2605f, 10.3867f, 7.7653f, 10.3867f)
                horizontalLineTo(9.5224f)
                lineTo(8.9999f, 13.6133f)
                horizontalLineTo(6.9624f)
                curveTo(6.5595f, 13.6133f, 6.2162f, 13.9059f, 6.1524f, 14.3037f)
                curveTo(6.0725f, 14.8022f, 6.4575f, 15.2539f, 6.9624f, 15.2539f)
                horizontalLineTo(8.7342f)
                lineTo(8.2777f, 18.0729f)
                curveTo(8.199f, 18.559f, 8.5743f, 19.0f, 9.0667f, 19.0f)
                curveTo(9.4588f, 19.0f, 9.793f, 18.7156f, 9.8557f, 18.3285f)
                lineTo(10.3536f, 15.2539f)
                horizontalLineTo(13.5925f)
                lineTo(13.136f, 18.0729f)
                close()
                moveTo(13.8582f, 13.6133f)
                lineTo(14.3807f, 10.3867f)
                horizontalLineTo(11.1418f)
                lineTo(10.6193f, 13.6133f)
                horizontalLineTo(13.8582f)
                close()
            }
        }
        .build()
        return _contextCopyNoteId!!
    }

private var _contextCopyNoteId: ImageVector? = null
