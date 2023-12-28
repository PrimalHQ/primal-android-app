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

public val PrimalIcons.WalletError: ImageVector
    get() {
        if (_primalwalleterror != null) {
            return _primalwalleterror!!
        }
        _primalwalleterror = Builder(name = "Primalwalleterror", defaultWidth = 160.0.dp,
                defaultHeight = 160.0.dp, viewportWidth = 160.0f, viewportHeight = 160.0f).apply {
            path(fill = SolidColor(Color(0xFFffffff)), stroke = SolidColor(Color(0xFFFA3C3C)),
                    fillAlpha = 0.7f, strokeLineWidth = 10.0f, strokeLineCap = Butt, strokeLineJoin
                    = Miter, strokeLineMiter = 4.0f, pathFillType = NonZero) {
                moveTo(80.0f, 80.0f)
                moveToRelative(-75.0f, 0.0f)
                arcToRelative(75.0f, 75.0f, 0.0f, true, true, 150.0f, 0.0f)
                arcToRelative(75.0f, 75.0f, 0.0f, true, true, -150.0f, 0.0f)
            }
            path(fill = SolidColor(Color(0xFFFA3C3C)), stroke = SolidColor(Color(0xFFFA3C3C)),
                    strokeLineWidth = 1.5f, strokeLineCap = Butt, strokeLineJoin = Miter,
                    strokeLineMiter = 4.0f, pathFillType = NonZero) {
                moveTo(50.599f, 57.115f)
                lineTo(73.485f, 80.0f)
                lineTo(50.599f, 102.885f)
                lineTo(50.599f, 102.885f)
                curveTo(48.8f, 104.685f, 48.8f, 107.601f, 50.599f, 109.401f)
                lineTo(50.599f, 109.401f)
                curveTo(52.398f, 111.2f, 55.315f, 111.2f, 57.114f, 109.401f)
                lineTo(56.584f, 108.87f)
                lineTo(57.115f, 109.401f)
                lineTo(80.0f, 86.515f)
                lineTo(102.885f, 109.4f)
                curveTo(102.885f, 109.4f, 102.885f, 109.4f, 102.885f, 109.4f)
                curveTo(104.684f, 111.2f, 107.601f, 111.2f, 109.4f, 109.4f)
                curveTo(111.2f, 107.601f, 111.2f, 104.684f, 109.4f, 102.885f)
                lineTo(86.515f, 80.0f)
                lineTo(109.4f, 57.115f)
                lineTo(108.87f, 56.584f)
                lineTo(109.4f, 57.115f)
                curveTo(111.199f, 55.316f, 111.199f, 52.399f, 109.4f, 50.6f)
                lineTo(109.4f, 50.6f)
                curveTo(107.601f, 48.801f, 104.684f, 48.801f, 102.885f, 50.6f)
                lineTo(102.885f, 50.6f)
                lineTo(80.0f, 73.485f)
                lineTo(57.114f, 50.6f)
                curveTo(57.114f, 50.6f, 57.114f, 50.6f, 57.114f, 50.6f)
                curveTo(55.315f, 48.8f, 52.398f, 48.8f, 50.599f, 50.6f)
                curveTo(50.599f, 50.6f, 50.599f, 50.6f, 50.599f, 50.6f)
                curveTo(50.599f, 50.6f, 50.599f, 50.6f, 50.599f, 50.6f)
                curveTo(48.8f, 52.399f, 48.8f, 55.316f, 50.599f, 57.115f)
                curveTo(50.599f, 57.115f, 50.599f, 57.115f, 50.599f, 57.115f)
                close()
            }
        }
        .build()
        return _primalwalleterror!!
    }

private var _primalwalleterror: ImageVector? = null
