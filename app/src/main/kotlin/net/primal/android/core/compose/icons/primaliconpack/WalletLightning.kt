package net.primal.android.core.compose.icons.primaliconpack

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType.Companion.EvenOdd
import androidx.compose.ui.graphics.PathFillType.Companion.NonZero
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap.Companion.Butt
import androidx.compose.ui.graphics.StrokeJoin.Companion.Miter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.ImageVector.Builder
import androidx.compose.ui.graphics.vector.group
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp
import net.primal.android.core.compose.icons.PrimalIcons

public val PrimalIcons.WalletLightning: ImageVector
    get() {
        if (_walletLightning != null) {
            return _walletLightning!!
        }
        _walletLightning = Builder(name = "WalletLightning", defaultWidth = 28.0.dp, defaultHeight =
                28.0.dp, viewportWidth = 28.0f, viewportHeight = 28.0f).apply {
            group {
                path(fill = SolidColor(Color(0xFFAAAAAA)), stroke = null, strokeLineWidth = 0.0f,
                        strokeLineCap = Butt, strokeLineJoin = Miter, strokeLineMiter = 4.0f,
                        pathFillType = EvenOdd) {
                    moveTo(21.1124f, 11.3456f)
                    curveTo(21.4271f, 11.3456f, 21.6102f, 11.6885f, 21.4272f, 11.9353f)
                    lineTo(14.573f, 21.1847f)
                    curveTo(14.0935f, 21.8318f, 13.0365f, 21.4029f, 13.1816f, 20.6201f)
                    lineTo(14.1259f, 15.5226f)
                    horizontalLineTo(10.2626f)
                    curveTo(9.9427f, 15.5226f, 9.761f, 15.1696f, 9.9545f, 14.9241f)
                    lineTo(16.7515f, 6.2979f)
                    curveTo(17.2474f, 5.6686f, 18.2849f, 6.1171f, 18.1271f, 6.8925f)
                    lineTo(17.2212f, 11.3456f)
                    horizontalLineTo(21.1124f)
                    close()
                    moveTo(15.5757f, 12.6509f)
                    lineTo(16.3169f, 9.0076f)
                    lineTo(12.2119f, 14.2173f)
                    horizontalLineTo(15.7434f)
                    lineTo(14.9671f, 18.4071f)
                    lineTo(19.2327f, 12.6509f)
                    horizontalLineTo(15.5757f)
                    close()
                    moveTo(7.55f, 9.7779f)
                    curveTo(7.55f, 9.4033f, 7.8536f, 9.0997f, 8.2281f, 9.0997f)
                    horizontalLineTo(11.1344f)
                    curveTo(11.5089f, 9.0997f, 11.8125f, 9.4033f, 11.8125f, 9.7779f)
                    curveTo(11.8125f, 10.1524f, 11.5089f, 10.456f, 11.1344f, 10.456f)
                    horizontalLineTo(8.2281f)
                    curveTo(7.8536f, 10.456f, 7.55f, 10.1524f, 7.55f, 9.7779f)
                    close()
                    moveTo(6.6781f, 12.9747f)
                    curveTo(6.3036f, 12.9747f, 6.0f, 13.2783f, 6.0f, 13.6529f)
                    curveTo(6.0f, 14.0274f, 6.3036f, 14.331f, 6.6781f, 14.331f)
                    horizontalLineTo(8.4219f)
                    curveTo(8.7964f, 14.331f, 9.1f, 14.0274f, 9.1f, 13.6529f)
                    curveTo(9.1f, 13.2783f, 8.7964f, 12.9747f, 8.4219f, 12.9747f)
                    horizontalLineTo(6.6781f)
                    close()
                    moveTo(6.775f, 17.5279f)
                    curveTo(6.775f, 17.1533f, 7.0786f, 16.8497f, 7.4531f, 16.8497f)
                    horizontalLineTo(10.7469f)
                    curveTo(11.1214f, 16.8497f, 11.425f, 17.1533f, 11.425f, 17.5279f)
                    curveTo(11.425f, 17.9024f, 11.1214f, 18.206f, 10.7469f, 18.206f)
                    horizontalLineTo(7.4531f)
                    curveTo(7.0786f, 18.206f, 6.775f, 17.9024f, 6.775f, 17.5279f)
                    close()
                }
                path(fill = SolidColor(Color(0x00000000)), stroke = SolidColor(Color(0xFFAAAAAA)),
                        strokeLineWidth = 1.5f, strokeLineCap = Butt, strokeLineJoin = Miter,
                        strokeLineMiter = 4.0f, pathFillType = NonZero) {
                    moveTo(14.0f, 14.0f)
                    moveToRelative(-13.25f, 0.0f)
                    arcToRelative(13.25f, 13.25f, 0.0f, true, true, 26.5f, 0.0f)
                    arcToRelative(13.25f, 13.25f, 0.0f, true, true, -26.5f, 0.0f)
                }
            }
        }
        .build()
        return _walletLightning!!
    }

private var _walletLightning: ImageVector? = null
