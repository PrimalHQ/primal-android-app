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
        if (_walletError != null) {
            return _walletError!!
        }
        _walletError = Builder(name = "WalletError", defaultWidth = 160.0.dp, defaultHeight =
                160.0.dp, viewportWidth = 160.0f, viewportHeight = 160.0f).apply {
            path(fill = SolidColor(Color(0x00000000)), stroke = SolidColor(Color(0xFFFA3C3C)),
                    strokeLineWidth = 10.0f, strokeLineCap = Butt, strokeLineJoin = Miter,
                    strokeLineMiter = 4.0f, pathFillType = NonZero) {
                moveTo(80.0f, 80.0f)
                moveToRelative(-75.0f, 0.0f)
                arcToRelative(75.0f, 75.0f, 0.0f, true, true, 150.0f, 0.0f)
                arcToRelative(75.0f, 75.0f, 0.0f, true, true, -150.0f, 0.0f)
            }
            path(fill = SolidColor(Color(0xFFFA3C3C)), stroke = SolidColor(Color(0xFFFA3C3C)),
                    strokeLineWidth = 1.5f, strokeLineCap = Butt, strokeLineJoin = Miter,
                    strokeLineMiter = 4.0f, pathFillType = NonZero) {
                moveTo(86.515f, 80.0f)
                lineTo(109.4f, 102.885f)
                curveTo(109.4f, 102.885f, 109.4f, 102.885f, 109.4f, 102.885f)
                curveTo(111.199f, 104.684f, 111.199f, 107.601f, 109.4f, 109.4f)
                curveTo(109.4f, 109.4f, 109.4f, 109.4f, 109.4f, 109.4f)
                moveTo(86.515f, 80.0f)
                lineTo(103.415f, 108.87f)
                curveTo(104.922f, 110.377f, 107.364f, 110.377f, 108.87f, 108.87f)
                lineTo(109.4f, 109.4f)
                moveTo(86.515f, 80.0f)
                lineTo(109.4f, 57.115f)
                lineTo(108.893f, 56.607f)
                lineTo(109.4f, 57.115f)
                curveTo(111.199f, 55.316f, 111.199f, 52.399f, 109.4f, 50.6f)
                lineTo(109.4f, 50.6f)
                curveTo(107.601f, 48.801f, 104.684f, 48.801f, 102.885f, 50.6f)
                lineTo(102.885f, 50.6f)
                lineTo(80.0f, 73.485f)
                lineTo(57.114f, 50.6f)
                curveTo(55.315f, 48.8f, 52.398f, 48.8f, 50.599f, 50.6f)
                moveTo(86.515f, 80.0f)
                lineTo(50.599f, 50.6f)
                moveTo(109.4f, 109.4f)
                curveTo(107.601f, 111.2f, 104.684f, 111.2f, 102.885f, 109.4f)
                curveTo(102.885f, 109.4f, 102.885f, 109.4f, 102.885f, 109.4f)
                moveTo(109.4f, 109.4f)
                lineTo(102.885f, 109.4f)
                moveTo(102.885f, 109.4f)
                lineTo(80.0f, 86.515f)
                lineTo(57.115f, 109.401f)
                lineTo(56.584f, 108.87f)
                lineTo(57.114f, 109.401f)
                curveTo(55.315f, 111.2f, 52.398f, 111.2f, 50.599f, 109.401f)
                lineTo(50.599f, 109.401f)
                curveTo(48.8f, 107.601f, 48.8f, 104.685f, 50.599f, 102.885f)
                lineTo(50.599f, 102.885f)
                lineTo(73.485f, 80.0f)
                lineTo(50.599f, 57.115f)
                curveTo(50.599f, 57.115f, 50.599f, 57.115f, 50.599f, 57.115f)
                curveTo(48.8f, 55.316f, 48.8f, 52.399f, 50.599f, 50.6f)
                curveTo(50.599f, 50.6f, 50.599f, 50.6f, 50.599f, 50.6f)
                moveTo(102.885f, 109.4f)
                lineTo(50.599f, 50.6f)
            }
        }
        .build()
        return _walletError!!
    }

private var _walletError: ImageVector? = null
