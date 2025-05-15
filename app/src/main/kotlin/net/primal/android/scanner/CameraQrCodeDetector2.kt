package net.primal.android.scanner

import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.resolutionselector.ResolutionSelector
import androidx.camera.core.resolutionselector.ResolutionStrategy
import androidx.camera.view.LifecycleCameraController
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.compose.LocalLifecycleOwner
import java.util.concurrent.Executors
import kotlin.time.Duration.Companion.seconds
import net.primal.android.LocalQrCodeDecoder
import net.primal.android.core.utils.isOlderThan
import net.primal.android.scanner.analysis.QrCodeAnalyzer
import net.primal.android.scanner.domain.QrCodeResult

@Composable
fun CameraQrCodeDetector2(torchEnabled: Boolean = false, onQrCodeDetected: (QrCodeResult) -> Unit) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val cameraSelector = remember {
        CameraSelector.Builder()
            .requireLensFacing(CameraSelector.LENS_FACING_BACK)
            .build()
    }

    var lastScannedQrCodeData by remember { mutableStateOf<QrCodeResult?>(null) }

    val decoder = LocalQrCodeDecoder.current
    val cameraController = remember {
        val qrCodeAnalyzer = QrCodeAnalyzer(decoder = decoder) { result ->
            val isRepeatingResult = result.equalValues(lastScannedQrCodeData)
            val lastSuccessfulScanResultExpired = lastScannedQrCodeData?.timestamp.isOlderThan(2.seconds)
            if (!isRepeatingResult || lastSuccessfulScanResultExpired) {
                onQrCodeDetected(result)
                lastScannedQrCodeData = result
            }
        }

        LifecycleCameraController(context).apply {
            this.cameraSelector = cameraSelector
            imageAnalysisBackpressureStrategy = ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST
            setImageAnalysisResolutionSelector(
                ResolutionSelector.Builder()
                    .setResolutionStrategy(
                        ResolutionStrategy(
                            QrCodeAnalyzer.AnalysisTargetSize,
                            ResolutionStrategy.FALLBACK_RULE_CLOSEST_HIGHER_THEN_LOWER,
                        ),
                    )
                    .build(),
            )
            setImageAnalysisAnalyzer(Executors.newSingleThreadExecutor(), qrCodeAnalyzer)
        }
    }

    LaunchedEffect(lifecycleOwner, cameraController) {
        cameraController.unbind()
        cameraController.bindToLifecycle(lifecycleOwner)
    }

    cameraController.enableTorch(torchEnabled)

    AndroidView(
        modifier = Modifier.fillMaxSize(),
        factory = {
            PreviewView(context).apply {
                controller = cameraController
            }
        },
    )
}
