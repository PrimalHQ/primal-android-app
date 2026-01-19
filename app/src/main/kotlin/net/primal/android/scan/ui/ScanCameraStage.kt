package net.primal.android.scan.ui

import android.Manifest
import android.content.pm.PackageManager
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import kotlin.time.Duration.Companion.milliseconds
import kotlinx.coroutines.delay
import net.primal.android.R
import net.primal.android.core.compose.button.PrimalLoadingButton
import net.primal.android.core.compose.icons.PrimalIcons
import net.primal.android.core.compose.icons.primaliconpack.Keyboard
import net.primal.android.scan.ScanCodeContract
import net.primal.android.scanner.MissingCameraPermissionContent
import net.primal.android.scanner.ScannerCameraDetector
import net.primal.android.scanner.domain.QrCodeResult
import net.primal.android.scanner.missingCameraPermissionColors
import net.primal.android.theme.AppTheme

private val UseKeyboardButtonContentColor = Color(0xFFAAAAAA)

@Composable
fun ScanCameraStage(
    scanMode: ScanCodeContract.ScanMode,
    modifier: Modifier = Modifier,
    onQrCodeDetected: (QrCodeResult) -> Unit,
    onEnterCodeClick: () -> Unit,
    onCameraPermissionChange: (Boolean) -> Unit,
) {
    val context = LocalContext.current
    var hasCameraPermission by remember {
        val permission = ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA)
        mutableStateOf(permission == PackageManager.PERMISSION_GRANTED)
    }

    LaunchedEffect(hasCameraPermission) {
        onCameraPermissionChange(hasCameraPermission)
    }

    if (hasCameraPermission) {
        ScanCameraAccessGranted(
            scanMode = scanMode,
            modifier = modifier,
            onQrCodeDetected = onQrCodeDetected,
            onEnterCodeClick = onEnterCodeClick,
        )
    } else {
        MissingCameraPermissionContent(
            modifier = modifier
                .fillMaxSize()
                .background(color = AppTheme.colorScheme.background),
            colors = missingCameraPermissionColors(
                textColor = AppTheme.colorScheme.onSurface,
                iconContainerColor = AppTheme.extraColorScheme.surfaceVariantAlt1,
                iconContentColor = AppTheme.colorScheme.onSurface,
            ),
            onPermissionChange = { allowed ->
                hasCameraPermission = allowed
            },
        )
    }
}

@Composable
private fun ScanCameraAccessGranted(
    scanMode: ScanCodeContract.ScanMode,
    modifier: Modifier,
    onQrCodeDetected: (QrCodeResult) -> Unit,
    onEnterCodeClick: () -> Unit,
) {
    var cameraVisible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        delay(100.milliseconds)
        cameraVisible = true
    }

    ScannerCameraDetector(
        cameraVisible = cameraVisible,
        modifier = modifier.fillMaxSize(),
        onQrCodeDetected = onQrCodeDetected,
        overlayContent = {
            val density = LocalDensity.current
            val viewPortSizeDp = maxWidth * 0.7f
            val viewPortSizePx = with(density) { viewPortSizeDp.toPx() }
            val topPaddingForButton = with(density) {
                ((constraints.maxHeight - viewPortSizePx) / 2 + viewPortSizePx).toDp() + 50.dp
            }

            Box(
                modifier = Modifier.fillMaxSize(),
            ) {
                PrimalLoadingButton(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = topPaddingForButton)
                        .height(40.dp),
                    text = stringResource(id = R.string.scan_code_use_keyboard_button),
                    leadingIcon = PrimalIcons.Keyboard,
                    containerColor = Color.Black,
                    contentColor = UseKeyboardButtonContentColor,
                    onClick = onEnterCodeClick,
                    fontWeight = FontWeight.Normal,
                    fontSize = 15.sp,
                )

                Column(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(horizontal = 32.dp)
                        .padding(bottom = 64.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    val (titleRes, subtitleRes) = when (scanMode) {
                        ScanCodeContract.ScanMode.Anything ->
                            R.string.scan_code_scan_anything_title to
                                R.string.scan_code_scan_anything_subtitle

                        ScanCodeContract.ScanMode.RemoteLogin ->
                            R.string.scan_code_remote_login_camera_title to
                                R.string.scan_code_remote_login_camera_subtitle
                    }

                    Text(
                        text = stringResource(id = titleRes),
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        style = AppTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center,
                    )
                    Text(
                        text = stringResource(id = subtitleRes),
                        color = Color.White.copy(alpha = 0.75f),
                        style = AppTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center,
                    )
                }
            }
        },
    )
}
