package net.primal.android.scanner

import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.view.CameraController
import androidx.camera.view.LifecycleCameraController
import androidx.camera.view.PreviewView
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.compose.LocalLifecycleOwner
import java.util.concurrent.Executors
import net.primal.android.scanner.analysis.QrCodeAnalyzer
import net.primal.android.scanner.domain.QrCodeResult
import net.primal.android.theme.AppTheme

@Composable
fun CameraQrCodeDetector(
    cameraVisible: Boolean,
    modifier: Modifier = Modifier,
    torchEnabled: Boolean = false,
    onQrCodeDetected: (QrCodeResult) -> Unit,
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val previewView = remember { PreviewView(context) }

    val cameraSelector = remember {
        CameraSelector.Builder()
            .requireLensFacing(CameraSelector.LENS_FACING_BACK)
            .build()
    }

    val cameraController = remember {
        val qrCodeAnalyzer = QrCodeAnalyzer {
            onQrCodeDetected(it)
        }

        LifecycleCameraController(context).apply {
            this.cameraSelector = cameraSelector
            imageAnalysisBackpressureStrategy = ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST
            imageAnalysisTargetSize = CameraController.OutputSize(QrCodeAnalyzer.AnalysisTargetSize)
            setImageAnalysisAnalyzer(Executors.newSingleThreadExecutor(), qrCodeAnalyzer)
        }
    }

    LaunchedEffect(cameraController, previewView, lifecycleOwner) {
        cameraController.unbind()
        cameraController.bindToLifecycle(lifecycleOwner)
        previewView.controller = cameraController
    }

    cameraController.enableTorch(torchEnabled)

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
                AndroidView(
                    modifier = Modifier.fillMaxSize(),
                    factory = { previewView },
                )
            }
        }

        if (cameraVisible) {
            CameraOverlayContent(
                modifier = Modifier.fillMaxSize(),
                outsideColor = AppTheme.colorScheme.scrim.copy(alpha = 0.75f),
                viewPortSize = maxWidth.times(other = 0.7f),
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
