package net.primal.android.scanner

import android.Manifest
import android.content.pm.PackageManager
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraintsScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import net.primal.android.scanner.domain.QrCodeResult

@Composable
fun PrimalCamera(
    cameraVisible: Boolean,
    onQrCodeDetected: (QrCodeResult) -> Unit,
    modifier: Modifier = Modifier,
    missingPermissionColors: MissingCameraPermissionColors = missingCameraPermissionColors(),
    overlayContent: (@Composable BoxWithConstraintsScope.() -> Unit)? = null,
) {
    val context = LocalContext.current

    Box(
        modifier = modifier,
    ) {
        var hasCameraPermission by remember {
            val permission = ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA)
            mutableStateOf(permission == PackageManager.PERMISSION_GRANTED)
        }

        if (hasCameraPermission) {
            CameraBox(
                cameraVisible = cameraVisible,
                modifier = Modifier.fillMaxSize(),
                onQrCodeDetected = onQrCodeDetected,
                overlayContent = overlayContent,
            )
        } else {
            MissingCameraPermissionContent(
                modifier = Modifier.fillMaxSize(),
                colors = missingPermissionColors,
                onPermissionChange = { allowed ->
                    hasCameraPermission = allowed
                },
            )
        }
    }
}
