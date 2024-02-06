package net.primal.android.core.compose.icons.primaliconpack

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush.Companion.linearGradient
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType.Companion.NonZero
import androidx.compose.ui.graphics.StrokeCap.Companion.Butt
import androidx.compose.ui.graphics.StrokeJoin.Companion.Miter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.ImageVector.Builder
import androidx.compose.ui.graphics.vector.group
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp
import net.primal.android.core.compose.icons.PrimalIcons

public val PrimalIcons.WalletLightningPaymentAlt: ImageVector
    get() {
        if (_walletLnPayment != null) {
            return _walletLnPayment!!
        }
        _walletLnPayment = Builder(name = "WalletLnPayment", defaultWidth = 28.0.dp, defaultHeight =
                28.0.dp, viewportWidth = 28.0f, viewportHeight = 28.0f).apply {
            group {
                path(fill = linearGradient(0.0f to Color(0xFFFF9F2F), 1.0f to Color(0xFFFA3C3C),
                        start = Offset(10.5f,4.0f), end = Offset(16.304f,31.3598f)), stroke = null,
                        strokeLineWidth = 0.0f, strokeLineCap = Butt, strokeLineJoin = Miter,
                        strokeLineMiter = 4.0f, pathFillType = NonZero) {
                    moveTo(27.2998f, 9.6565f)
                    curveTo(27.8684f, 9.6565f, 28.199f, 10.2759f, 27.8685f, 10.7218f)
                    lineTo(15.4868f, 27.4305f)
                    curveTo(14.6205f, 28.5995f, 12.7111f, 27.8246f, 12.9731f, 26.4104f)
                    lineTo(14.6791f, 17.2021f)
                    horizontalLineTo(7.7003f)
                    curveTo(7.1224f, 17.2021f, 6.7941f, 16.5645f, 7.1436f, 16.1209f)
                    lineTo(19.4221f, 0.5381f)
                    curveTo(20.3178f, -0.5987f, 22.1921f, 0.2115f, 21.907f, 1.6122f)
                    lineTo(20.2706f, 9.6565f)
                    horizontalLineTo(27.2998f)
                    close()
                }
                path(fill = linearGradient(0.0f to Color(0xFFFF9F2F), 1.0f to Color(0xFFFA3C3C),
                        start = Offset(10.5f,4.0f), end = Offset(16.304f,31.3598f)), stroke = null,
                        strokeLineWidth = 0.0f, strokeLineCap = Butt, strokeLineJoin = Miter,
                        strokeLineMiter = 4.0f, pathFillType = NonZero) {
                    moveTo(2.8f, 6.825f)
                    curveTo(2.8f, 6.1484f, 3.3484f, 5.6f, 4.025f, 5.6f)
                    horizontalLineTo(9.275f)
                    curveTo(9.9516f, 5.6f, 10.5f, 6.1484f, 10.5f, 6.825f)
                    curveTo(10.5f, 7.5016f, 9.9516f, 8.05f, 9.275f, 8.05f)
                    horizontalLineTo(4.025f)
                    curveTo(3.3484f, 8.05f, 2.8f, 7.5016f, 2.8f, 6.825f)
                    close()
                }
                path(fill = linearGradient(0.0f to Color(0xFFFF9F2F), 1.0f to Color(0xFFFA3C3C),
                        start = Offset(10.5f,4.0f), end = Offset(16.304f,31.3598f)), stroke = null,
                        strokeLineWidth = 0.0f, strokeLineCap = Butt, strokeLineJoin = Miter,
                        strokeLineMiter = 4.0f, pathFillType = NonZero) {
                    moveTo(1.225f, 12.6f)
                    curveTo(0.5484f, 12.6f, 0.0f, 13.1484f, 0.0f, 13.825f)
                    curveTo(0.0f, 14.5016f, 0.5484f, 15.05f, 1.225f, 15.05f)
                    horizontalLineTo(4.375f)
                    curveTo(5.0516f, 15.05f, 5.6f, 14.5016f, 5.6f, 13.825f)
                    curveTo(5.6f, 13.1484f, 5.0516f, 12.6f, 4.375f, 12.6f)
                    horizontalLineTo(1.225f)
                    close()
                }
                path(fill = linearGradient(0.0f to Color(0xFFFF9F2F), 1.0f to Color(0xFFFA3C3C),
                        start = Offset(10.5f,4.0f), end = Offset(16.304f,31.3598f)), stroke = null,
                        strokeLineWidth = 0.0f, strokeLineCap = Butt, strokeLineJoin = Miter,
                        strokeLineMiter = 4.0f, pathFillType = NonZero) {
                    moveTo(1.4f, 20.825f)
                    curveTo(1.4f, 20.1484f, 1.9484f, 19.6f, 2.625f, 19.6f)
                    horizontalLineTo(8.575f)
                    curveTo(9.2516f, 19.6f, 9.8f, 20.1484f, 9.8f, 20.825f)
                    curveTo(9.8f, 21.5016f, 9.2516f, 22.05f, 8.575f, 22.05f)
                    horizontalLineTo(2.625f)
                    curveTo(1.9484f, 22.05f, 1.4f, 21.5016f, 1.4f, 20.825f)
                    close()
                }
            }
        }
        .build()
        return _walletLnPayment!!
    }

private var _walletLnPayment: ImageVector? = null
