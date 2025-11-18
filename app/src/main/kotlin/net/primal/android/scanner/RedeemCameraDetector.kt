package net.primal.android.scanner

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.BoxWithConstraintsScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.ClipOp
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import net.primal.android.core.compose.foundation.KeepScreenOn
import net.primal.android.scanner.domain.QrCodeResult
import net.primal.android.theme.AppTheme

@Composable
fun RedeemCameraDetector(
    cameraVisible: Boolean,
    modifier: Modifier = Modifier,
    torchEnabled: Boolean = false,
    onQrCodeDetected: (QrCodeResult) -> Unit,
    overlayContent: (@Composable BoxWithConstraintsScope.() -> Unit)? = null,
) {
    KeepScreenOn()

    val cameraAlpha: Float by animateFloatAsState(
        targetValue = if (cameraVisible) 1.0f else 0.0f,
        label = "cameraAlpha",
    )

    BoxWithConstraints(
        modifier = modifier,
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(color = AppTheme.colorScheme.surface.copy(alpha = 1 - cameraAlpha)),
        ) {
            if (cameraVisible) {
                CameraPreview(
                    modifier = Modifier.fillMaxSize(),
                    torchEnabled = torchEnabled,
                    onQrCodeDetected = onQrCodeDetected,
                )
            }
        }

        if (cameraVisible) {
            RedeemCameraOverlay(
                modifier = Modifier.fillMaxSize(),
                viewPortSize = this.maxWidth.times(other = 0.7f),
            )
            overlayContent?.invoke(this)
        }
    }
}

@Composable
private fun RedeemCameraOverlay(modifier: Modifier, viewPortSize: Dp) {
    val cornerRadius = 6.dp
    val bracketLength = 32.dp
    val bracketWidth = 2.dp
    val bracketOffset = 4.dp
    val bracketCurveRadius = 6.dp

    val scrimColor = AppTheme.colorScheme.scrim.copy(alpha = 0.4f)

    Box(
        modifier = modifier.drawWithContent {
            drawContent()

            val viewPortSizePx = viewPortSize.toPx()
            val cornerRadiusPx = cornerRadius.toPx()
            val bracketLengthPx = bracketLength.toPx()
            val bracketWidthPx = bracketWidth.toPx()
            val bracketOffsetPx = bracketOffset.toPx()
            val bracketCurveRadiusPx = bracketCurveRadius.toPx()

            val horizontalMargin = (size.width - viewPortSizePx) / 2f
            val topMargin = (size.height - viewPortSizePx) / 2f

            val viewPortRect = Rect(
                left = horizontalMargin,
                top = topMargin,
                right = horizontalMargin + viewPortSizePx,
                bottom = topMargin + viewPortSizePx,
            )

            val viewPortPath = Path().apply {
                addRoundRect(RoundRect(viewPortRect, CornerRadius(cornerRadiusPx)))
            }

            clipPath(viewPortPath, clipOp = ClipOp.Difference) {
                drawRect(
                    color = scrimColor,
                    blendMode = BlendMode.SrcOver,
                )
            }

            val bracketColor = Color.White
            val stroke = Stroke(
                width = bracketWidthPx,
                cap = StrokeCap.Round,
                join = StrokeJoin.Round,
                pathEffect = PathEffect.cornerPathEffect(bracketCurveRadiusPx),
            )

            val tlPath = Path().apply {
                val corner = viewPortRect.topLeft - Offset(bracketOffsetPx, bracketOffsetPx)
                moveTo(corner.x, corner.y + bracketLengthPx)
                lineTo(corner.x, corner.y)
                lineTo(corner.x + bracketLengthPx, corner.y)
            }
            drawPath(path = tlPath, color = bracketColor, style = stroke)

            val trPath = Path().apply {
                val corner = viewPortRect.topRight + Offset(bracketOffsetPx, -bracketOffsetPx)
                moveTo(corner.x - bracketLengthPx, corner.y)
                lineTo(corner.x, corner.y)
                lineTo(corner.x, corner.y + bracketLengthPx)
            }
            drawPath(path = trPath, color = bracketColor, style = stroke)

            val blPath = Path().apply {
                val corner = viewPortRect.bottomLeft + Offset(-bracketOffsetPx, bracketOffsetPx)
                moveTo(corner.x, corner.y - bracketLengthPx)
                lineTo(corner.x, corner.y)
                lineTo(corner.x + bracketLengthPx, corner.y)
            }
            drawPath(path = blPath, color = bracketColor, style = stroke)

            val brPath = Path().apply {
                val corner = viewPortRect.bottomRight + Offset(bracketOffsetPx, bracketOffsetPx)
                moveTo(corner.x, corner.y - bracketLengthPx)
                lineTo(corner.x, corner.y)
                lineTo(corner.x - bracketLengthPx, corner.y)
            }
            drawPath(path = brPath, color = bracketColor, style = stroke)
        },
    )
}
