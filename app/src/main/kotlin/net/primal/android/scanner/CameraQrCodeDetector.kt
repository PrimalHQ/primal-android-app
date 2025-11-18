package net.primal.android.scanner

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import net.primal.android.scanner.domain.QrCodeResult
import net.primal.android.theme.AppTheme

@Composable
fun CameraQrCodeDetector(
    cameraVisible: Boolean,
    modifier: Modifier = Modifier,
    torchEnabled: Boolean = false,
    onQrCodeDetected: (QrCodeResult) -> Unit,
) {
    val cameraAlpha: Float by animateFloatAsState(
        targetValue = if (cameraVisible) 0.0f else 1.0f,
        label = "cameraAlpha",
    )

    BoxWithConstraints(
        modifier = modifier,
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(color = AppTheme.colorScheme.scrim.copy(alpha = cameraAlpha)),
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
            CameraOverlayContent(
                modifier = Modifier.fillMaxSize(),
                outsideColor = AppTheme.colorScheme.scrim.copy(alpha = 0.75f),
                viewPortSize = this.maxWidth.times(other = 0.7f),
            )
        }
    }
}

@Composable
private fun CameraOverlayContent(
    modifier: Modifier,
    viewPortSize: Dp,
    outsideColor: Color = AppTheme.colorScheme.scrim.copy(alpha = 0.75f),
    headerContent: @Composable BoxScope.() -> Unit = {},
    footerContent: @Composable BoxScope.() -> Unit = {},
    successContent: @Composable BoxScope.() -> Unit = {},
) {
    Column(
        modifier = modifier,
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(weight = 0.45f, fill = true)
                .background(color = outsideColor),
        ) {
            headerContent()
        }

        Row(
            horizontalArrangement = Arrangement.SpaceAround,
        ) {
            Box(
                modifier = Modifier
                    .weight(weight = 1f, fill = true)
                    .height(viewPortSize)
                    .background(color = outsideColor),
            )

            Box(
                modifier = Modifier
                    .size(viewPortSize)
                    .border(width = 1.dp, color = AppTheme.colorScheme.outline),
            ) {
                successContent()
            }

            Box(
                modifier = Modifier
                    .weight(weight = 1f, fill = true)
                    .height(viewPortSize)
                    .background(color = outsideColor),
            )
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(weight = 0.55f, fill = true)
                .background(color = outsideColor),
        ) {
            footerContent()
        }
    }
}
