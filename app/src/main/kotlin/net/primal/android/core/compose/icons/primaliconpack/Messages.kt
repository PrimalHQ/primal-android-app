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

public val PrimalIcons.Messages: ImageVector
    get() {
        if (_messages != null) {
            return _messages!!
        }
        _messages = Builder(name = "Messages", defaultWidth = 24.0.dp, defaultHeight = 24.0.dp,
                viewportWidth = 24.0f, viewportHeight = 24.0f).apply {
            path(fill = SolidColor(Color(0xFFAAAAAA)), stroke = null, strokeLineWidth = 0.0f,
                    strokeLineCap = Butt, strokeLineJoin = Miter, strokeLineMiter = 4.0f,
                    pathFillType = EvenOdd) {
                moveTo(0.25f, 6.0f)
                curveTo(0.25f, 3.9289f, 1.9289f, 2.25f, 4.0f, 2.25f)
                horizontalLineTo(20.0f)
                curveTo(22.0711f, 2.25f, 23.75f, 3.9289f, 23.75f, 6.0f)
                verticalLineTo(18.0f)
                curveTo(23.75f, 20.0711f, 22.0711f, 21.75f, 20.0f, 21.75f)
                horizontalLineTo(4.0f)
                curveTo(1.9289f, 21.75f, 0.25f, 20.0711f, 0.25f, 18.0f)
                verticalLineTo(6.0f)
                close()
                moveTo(4.0f, 3.75f)
                curveTo(3.4738f, 3.75f, 2.9891f, 3.9309f, 2.6059f, 4.2339f)
                curveTo(2.4227f, 4.3786f, 2.3519f, 4.5902f, 2.3731f, 4.7907f)
                curveTo(2.3938f, 4.9865f, 2.5007f, 5.1716f, 2.6685f, 5.2944f)
                lineTo(11.2617f, 11.5841f)
                curveTo(11.7013f, 11.9059f, 12.2987f, 11.9059f, 12.7383f, 11.5841f)
                lineTo(21.3315f, 5.2944f)
                curveTo(21.4993f, 5.1716f, 21.6062f, 4.9865f, 21.6269f, 4.7907f)
                curveTo(21.6481f, 4.5902f, 21.5773f, 4.3786f, 21.3941f, 4.2339f)
                curveTo(21.0109f, 3.931f, 20.5262f, 3.75f, 20.0f, 3.75f)
                horizontalLineTo(4.0f)
                close()
                moveTo(2.543f, 7.039f)
                curveTo(2.2123f, 6.7999f, 1.75f, 7.0361f, 1.75f, 7.4441f)
                verticalLineTo(18.0f)
                curveTo(1.75f, 19.2426f, 2.7574f, 20.25f, 4.0f, 20.25f)
                horizontalLineTo(20.0f)
                curveTo(21.2426f, 20.25f, 22.25f, 19.2426f, 22.25f, 18.0f)
                verticalLineTo(7.4442f)
                curveTo(22.25f, 7.0361f, 21.7877f, 6.7999f, 21.457f, 7.039f)
                lineTo(13.0254f, 13.1354f)
                curveTo(12.4134f, 13.5779f, 11.5866f, 13.5779f, 10.9746f, 13.1354f)
                lineTo(2.543f, 7.039f)
                close()
            }
        }
        .build()
        return _messages!!
    }

private var _messages: ImageVector? = null
