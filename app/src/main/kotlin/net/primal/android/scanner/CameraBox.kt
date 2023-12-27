package net.primal.android.scanner

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.BoxWithConstraintsScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.tooling.preview.Preview
import net.primal.android.scanner.analysis.QrCodeResult
import net.primal.android.theme.PrimalTheme
import net.primal.android.theme.domain.PrimalTheme

@Composable
fun CameraBox(
    cameraVisible: Boolean,
    modifier: Modifier = Modifier,
    onQrCodeDetected: (QrCodeResult) -> Unit,
    overlayContent: (@Composable BoxWithConstraintsScope.() -> Unit)? = null,
) {
    KeepScreenOn()

    var lastScannedQrCodeData by remember { mutableStateOf<QrCodeResult?>(null) }

    BoxWithConstraints(
        modifier = modifier,
    ) {
        CameraQrCodeDetector(
            cameraVisible = cameraVisible,
            modifier = Modifier.fillMaxSize(),
            torchEnabled = false,
            onQrCodeDetected = { result ->
                if (!result.equalValues(lastScannedQrCodeData)) {
                    onQrCodeDetected(result)
                    lastScannedQrCodeData = result
                }
            },
        )

        overlayContent?.invoke(this)
    }
}

@Composable
private fun KeepScreenOn() {
    val currentView = LocalView.current
    DisposableEffect(Unit) {
        currentView.keepScreenOn = true
        onDispose {
            currentView.keepScreenOn = false
        }
    }
}

@Preview(showBackground = true)
@Composable
fun CameraContentPreview() {
    PrimalTheme(primalTheme = PrimalTheme.Sunset) {
        CameraBox(
            cameraVisible = false,
            onQrCodeDetected = {},
            overlayContent = {},
        )
    }
}
