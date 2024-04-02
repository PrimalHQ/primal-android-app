package net.primal.android.profile.qr.ui

import android.Manifest
import android.content.pm.PackageManager
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animate
import androidx.compose.animation.core.spring
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import net.primal.android.R
import net.primal.android.core.compose.foundation.KeepScreenOn
import net.primal.android.scanner.CameraQrCodeDetector2
import net.primal.android.scanner.MissingCameraPermissionContent
import net.primal.android.scanner.domain.QrCodeResult
import net.primal.android.scanner.missingCameraPermissionColors
import net.primal.android.theme.AppTheme

@Composable
fun ProfileQrCodeScanner(
    paddingValues: PaddingValues,
    cameraVisible: Boolean,
    onQrCodeDetected: (QrCodeResult) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        val context = LocalContext.current

        var hasCameraPermission by remember {
            val permission = ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA)
            mutableStateOf(permission == PackageManager.PERMISSION_GRANTED)
        }

        if (hasCameraPermission) {
            if (cameraVisible) {
                ProfileQrCodeCameraBox(onQrCodeDetected)

                Spacer(modifier = Modifier.height(32.dp))

                Text(
                    modifier = Modifier.padding(horizontal = 64.dp),
                    text = stringResource(id = R.string.profile_qr_code_scan_qr_code_hint),
                    color = Color.White,
                    textAlign = TextAlign.Center,
                    style = AppTheme.typography.bodyLarge,
                )
            }
        } else {
            MissingCameraPermissionContent(
                modifier = Modifier.fillMaxSize(),
                colors = missingCameraPermissionColors(
                    iconContainerColor = Color.White,
                    buttonContainerColor = profileQrCodeButtonBackgroundColor,
                    buttonContentColor = Color.White,
                    textColor = Color.White,
                ),
                onPermissionChange = { allowed ->
                    hasCameraPermission = allowed
                },
            )
        }
    }
}

@Composable
private fun ProfileQrCodeCameraBox(onQrCodeDetected: (QrCodeResult) -> Unit) {
    var previewSize by remember { mutableStateOf(0.dp) }
    var shape by remember { mutableStateOf(RoundedCornerShape(percent = 70)) }
    LaunchedEffect(Unit) {
        val animationSpec: AnimationSpec<Float> = spring(
            stiffness = Spring.StiffnessLow,
        )

        animate(
            initialValue = 0.0f,
            targetValue = 1.0f,
            animationSpec = animationSpec,
        ) { value, _ ->
            previewSize = 300.dp.times(value)
            shape = RoundedCornerShape(percent = 50 - (value * 40).toInt())
        }
    }

    Box(
        modifier = Modifier
            .clip(shape)
            .size(previewSize)
            .border(width = 4.dp, color = Color.White, shape = shape),
        contentAlignment = Alignment.Center,
    ) {
        KeepScreenOn()
        CameraQrCodeDetector2(onQrCodeDetected = onQrCodeDetected)
    }
}
