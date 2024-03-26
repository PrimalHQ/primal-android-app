package net.primal.android.wallet.transactions.send.prepare.tabs

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import kotlin.time.Duration.Companion.milliseconds
import kotlinx.coroutines.delay
import net.primal.android.scanner.PrimalCamera
import net.primal.android.scanner.analysis.QrCodeResult

@Composable
fun SendPaymentTabScan(isClosing: Boolean, onQrCodeDetected: (QrCodeResult) -> Unit) {
    var cameraVisible by remember { mutableStateOf(false) }
    if (!cameraVisible) {
        LaunchedEffect(Unit) {
            delay(100.milliseconds)
            cameraVisible = true
        }
    }
    if (!isClosing) {
        PrimalCamera(
            cameraVisible = cameraVisible,
            onQrCodeDetected = onQrCodeDetected,
        )
    }
}
