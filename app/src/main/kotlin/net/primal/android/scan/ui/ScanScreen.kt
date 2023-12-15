package net.primal.android.scan.ui

import android.Manifest
import android.content.pm.PackageManager
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST
import androidx.camera.view.CameraController
import androidx.camera.view.LifecycleCameraController
import androidx.camera.view.PreviewView
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import java.util.concurrent.Executors
import net.primal.android.R
import net.primal.android.scan.ScanContract
import net.primal.android.scan.ScanViewModel
import net.primal.android.scan.analysis.QrCodeAnalyzer
import net.primal.android.scan.analysis.QrCodeResult
import net.primal.android.theme.AppTheme
import net.primal.android.theme.PrimalTheme
import timber.log.Timber

@Composable
fun ScanScreen(
    viewModel: ScanViewModel,
    onClose: () -> Unit,
    onScanningCompleted: () -> Unit,
) {
    LaunchedEffect(viewModel) {
        viewModel.effect.collect {
            when (it) {
                ScanContract.SideEffect.ScanningCompleted -> onScanningCompleted()
            }
        }
    }

    val uiState = viewModel.state.collectAsState()

    ScanScreen(
        state = uiState.value,
        eventPublisher = {
            viewModel.setEvent(it)
        },
        onClose = onClose,
    )
}

@Composable
fun ScanScreen(
    state: ScanContract.UiState,
    eventPublisher: (ScanContract.UiEvent) -> Unit,
    onClose: () -> Unit,
) {
    val context = LocalContext.current

    Surface {
        var hasCameraPermission by remember {
            val permission = ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA)
            mutableStateOf(permission == PackageManager.PERMISSION_GRANTED)
        }

        if (hasCameraPermission) {
            PrimalTheme(primalTheme = net.primal.android.theme.domain.PrimalTheme.Sunset) {
                CameraContent(
                    modifier = Modifier.fillMaxSize(),
                    state = state,
                    eventPublisher = eventPublisher,
                    onClose = onClose,
                    onScanningCompleted = {},
                )
            }
        } else {
            MissingCameraPermissionContent(
                modifier = Modifier.fillMaxSize(),
                onPermissionChange = { allowed ->
                    hasCameraPermission = allowed
                },
            )
        }
    }
}

@Composable
private fun CameraContent(
    state: ScanContract.UiState,
    eventPublisher: (ScanContract.UiEvent) -> Unit,
    modifier: Modifier = Modifier,
    onScanningCompleted: (QrCodeResult) -> Unit,
    onClose: () -> Unit,
) {
    KeepScreenOn()

    BoxWithConstraints(
        modifier = modifier,
    ) {
        CameraQrCodeDetector { result ->
            eventPublisher(ScanContract.UiEvent.ProcessScannedData(result = result))
            Timber.e("Scanned = $result")
        }

        CameraOverlayContent(
            viewPortSize = maxWidth.times(0.7f),
            footerContent = {
                ViewPortFooter(
                    modifier = Modifier
                        .navigationBarsPadding()
                        .fillMaxWidth()
                        .padding(vertical = 16.dp)
                        .align(Alignment.Center),
                    onCancelClick = onClose,
                )
            },
        )
    }
}

@Composable
private fun CameraQrCodeDetector(torchEnabled: Boolean = false, onQrCodeDetected: (QrCodeResult) -> Unit) {
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
            imageAnalysisBackpressureStrategy = STRATEGY_KEEP_ONLY_LATEST
            imageAnalysisTargetSize = CameraController.OutputSize(QrCodeAnalyzer.AnalysisTargetSize)
            setImageAnalysisAnalyzer(Executors.newSingleThreadExecutor(), qrCodeAnalyzer)
        }
    }

    LaunchedEffect(cameraSelector, cameraController, previewView) {
        cameraController.unbind()
        cameraController.bindToLifecycle(lifecycleOwner)
        previewView.controller = cameraController
    }

    cameraController.enableTorch(torchEnabled)

    AndroidView({ previewView }, modifier = Modifier.fillMaxSize())
}

@Composable
private fun ViewPortFooter(modifier: Modifier, onCancelClick: () -> Unit) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.SpaceEvenly,
    ) {
        ScannerFab(
            modifier = Modifier.weight(1f),
            imageVector = Icons.Default.Close,
            label = stringResource(id = R.string.qrcode_scanner_cancel_button),
            onClick = onCancelClick,
        )
    }
}

@Composable
private fun CameraOverlayContent(
    viewPortSize: Dp,
    outsideColor: Color = AppTheme.colorScheme.scrim.copy(alpha = 0.6f),
    headerContent: @Composable BoxScope.() -> Unit = {},
    footerContent: @Composable BoxScope.() -> Unit = {},
    successContent: @Composable BoxScope.() -> Unit = {},
) {
    Column {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(0.45f, fill = true)
                .background(color = outsideColor),
        ) {
            headerContent()
        }

        Row(
            horizontalArrangement = Arrangement.SpaceAround,
        ) {
            Box(
                modifier = Modifier
                    .weight(1f, fill = true)
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
                    .weight(1f, fill = true)
                    .height(viewPortSize)
                    .background(color = outsideColor),
            )
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(0.55f, fill = true)
                .background(color = outsideColor),
        ) {
            footerContent()
        }
    }
}

@Composable
private fun ScannerFab(
    imageVector: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    label: String? = null,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        FloatingActionButton(
            modifier = Modifier
                .padding(all = 8.dp)
                .border(width = 1.dp, color = Color.White, shape = CircleShape),
            containerColor = Color.Transparent,
            contentColor = Color.White,
            shape = CircleShape,
            onClick = onClick,
        ) {
            Icon(
                imageVector = imageVector,
                contentDescription = null,
            )
        }

        Text(
            modifier = Modifier
                .padding(all = 8.dp)
                .alpha(if (label != null) 1.0f else 0.0f),
            text = label?.uppercase() ?: "",
            style = AppTheme.typography.labelMedium,
            color = Color.White,
        )
    }
}

@Composable
fun KeepScreenOn() {
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
fun MissingCameraPermissionContentPreview() {
    PrimalTheme(primalTheme = net.primal.android.theme.domain.PrimalTheme.Sunset) {
        MissingCameraPermissionContent(onPermissionChange = {})
    }
}

@Preview(showBackground = true)
@Composable
fun CameraContentPreview() {
    PrimalTheme(primalTheme = net.primal.android.theme.domain.PrimalTheme.Sunset) {
        CameraContent(
            state = ScanContract.UiState(),
            eventPublisher = {},
            onClose = {},
            onScanningCompleted = {},
        )
    }
}
