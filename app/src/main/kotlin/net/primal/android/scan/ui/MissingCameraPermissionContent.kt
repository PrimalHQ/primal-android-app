package net.primal.android.scan.ui

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import net.primal.android.R
import net.primal.android.core.compose.button.PrimalLoadingButton
import net.primal.android.core.compose.icons.PrimalIcons
import net.primal.android.core.compose.icons.primaliconpack.ImportPhotoFromCamera
import net.primal.android.theme.AppTheme

@Composable
fun MissingCameraPermissionContent(modifier: Modifier = Modifier, onPermissionChange: (Boolean) -> Unit) {
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { granted ->
            onPermissionChange(granted)
        },
    )

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceEvenly,
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(fraction = 0.42f)
                    .padding(vertical = 16.dp)
                    .aspectRatio(ratio = 1f)
                    .background(
                        color = AppTheme.extraColorScheme.surfaceVariantAlt1,
                        shape = CircleShape,
                    ),
            ) {
                Icon(
                    modifier = Modifier
                        .fillMaxWidth(fraction = 0.5f)
                        .aspectRatio(ratio = 1f)
                        .align(Alignment.Center),
                    imageVector = PrimalIcons.ImportPhotoFromCamera,
                    contentDescription = null,
                )
            }

            Text(
                modifier = Modifier
                    .fillMaxWidth(fraction = 0.9f)
                    .padding(vertical = 8.dp),
                textAlign = TextAlign.Center,
                style = AppTheme.typography.titleLarge,
                fontWeight = FontWeight.Medium,
                text = stringResource(id = R.string.qrcode_scanner_permission_title),
            )

            Text(
                modifier = Modifier.fillMaxWidth(fraction = 0.9f),
                textAlign = TextAlign.Center,
                style = AppTheme.typography.bodyLarge,
                text = stringResource(id = R.string.qrcode_scanner_permission_rationale),
            )
        }

        PrimalLoadingButton(
            modifier = Modifier
                .padding(vertical = 8.dp)
                .fillMaxWidth(fraction = 0.8f),
            text = stringResource(id = R.string.qrcode_scanner_grant_permission_button),
            onClick = {
                launcher.launch(Manifest.permission.CAMERA)
            },
        )
    }
}
