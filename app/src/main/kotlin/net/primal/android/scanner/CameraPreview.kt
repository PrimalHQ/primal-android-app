package net.primal.android.scanner

import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.resolutionselector.ResolutionSelector
import androidx.camera.core.resolutionselector.ResolutionStrategy
import androidx.camera.view.LifecycleCameraController
import androidx.camera.view.PreviewView
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.compose.LocalLifecycleOwner
import java.util.concurrent.Executors
import net.primal.android.LocalQrCodeDecoder
import net.primal.android.scanner.analysis.QrCodeAnalyzer
import net.primal.android.scanner.domain.QrCodeResult

@Composable
internal fun CameraPreview(
    modifier: Modifier = Modifier,
    torchEnabled: Boolean = false,
    onQrCodeDetected: (QrCodeResult) -> Unit,
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val decoder = LocalQrCodeDecoder.current

    val cameraController = remember(decoder) {
        val qrCodeAnalyzer = QrCodeAnalyzer(decoder = decoder) { onQrCodeDetected(it) }

        LifecycleCameraController(context).apply {
            this.cameraSelector = CameraSelector.Builder()
                .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                .build()
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

    LaunchedEffect(cameraController, lifecycleOwner) {
        cameraController.unbind()
        cameraController.bindToLifecycle(lifecycleOwner)
    }

    cameraController.enableTorch(torchEnabled)

    AndroidView(
        modifier = modifier,
        factory = {
            PreviewView(context).apply {
                controller = cameraController
            }
        },
    )
}
