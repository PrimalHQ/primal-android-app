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
import java.time.Instant
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds
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

    var lastScannedQrCodeData by remember { mutableStateOf<ScanResult?>(null) }

    BoxWithConstraints(
        modifier = modifier,
    ) {
        CameraQrCodeDetector(
            cameraVisible = cameraVisible,
            modifier = Modifier.fillMaxSize(),
            torchEnabled = false,
            onQrCodeDetected = { result ->
                val isRepeatingResult = result.equalValues(lastScannedQrCodeData?.result)
                val lastSuccessfulScanResultExpired = lastScannedQrCodeData?.timestamp.isOlderThan(2.seconds)
                if (!isRepeatingResult || lastSuccessfulScanResultExpired) {
                    onQrCodeDetected(result)
                    lastScannedQrCodeData = ScanResult(
                        result = result,
                        timestamp = Instant.now(),
                    )
                }
            },
        )

        overlayContent?.invoke(this)
    }
}

private data class ScanResult(
    val result: QrCodeResult,
    val timestamp: Instant,
)

private fun Instant?.isOlderThan(duration: Duration): Boolean {
    if (this == null) return true
    return this < Instant.now().minusSeconds(duration.inWholeSeconds)
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
