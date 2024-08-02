package net.primal.android.scanner

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import net.primal.android.R
import net.primal.android.core.compose.button.PrimalLoadingButton
import net.primal.android.core.compose.icons.PrimalIcons
import net.primal.android.core.compose.icons.primaliconpack.ImportPhotoFromCamera
import net.primal.android.core.compose.preview.PrimalPreview
import net.primal.android.theme.AppTheme
import net.primal.android.theme.domain.PrimalTheme

@Composable
fun MissingCameraPermissionContent(
    modifier: Modifier = Modifier,
    colors: MissingCameraPermissionColors = missingCameraPermissionColors(),
    onPermissionChange: (Boolean) -> Unit,
) {
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { granted ->
            onPermissionChange(granted)
        },
    )

    Column(
        modifier = modifier.padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(fraction = 0.42f)
                .padding(vertical = 16.dp)
                .aspectRatio(ratio = 1f)
                .background(color = colors.iconContainerColor, shape = CircleShape),
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
            color = colors.textColor,
            style = AppTheme.typography.titleLarge,
            fontWeight = FontWeight.Medium,
            text = stringResource(id = R.string.qrcode_scanner_permission_title),
        )

        Text(
            modifier = Modifier.fillMaxWidth(fraction = 0.9f),
            textAlign = TextAlign.Center,
            style = AppTheme.typography.bodyLarge,
            color = colors.textColor,
            text = stringResource(id = R.string.qrcode_scanner_permission_rationale),
        )

        Spacer(modifier = Modifier.height(32.dp))

        PrimalLoadingButton(
            modifier = Modifier
                .padding(vertical = 8.dp)
                .fillMaxWidth(fraction = 0.8f),
            text = stringResource(id = R.string.qrcode_scanner_grant_permission_button),
            containerColor = colors.buttonContainerColor,
            contentColor = colors.buttonContentColor,
            onClick = { launcher.launch(Manifest.permission.CAMERA) },
        )
    }
}

@Stable
data class MissingCameraPermissionColors(
    val textColor: Color,
    val iconContainerColor: Color,
    val buttonContainerColor: Color,
    val buttonContentColor: Color,
)

@Composable
fun missingCameraPermissionColors(
    textColor: Color = Color.Unspecified,
    iconContainerColor: Color = AppTheme.extraColorScheme.surfaceVariantAlt1,
    buttonContainerColor: Color = AppTheme.colorScheme.primary,
    buttonContentColor: Color = Color.White,
) = MissingCameraPermissionColors(
    textColor = textColor,
    iconContainerColor = iconContainerColor,
    buttonContainerColor = buttonContainerColor,
    buttonContentColor = buttonContentColor,
)

@Preview(showBackground = true)
@Composable
fun MissingCameraPermissionContentPreview() {
    PrimalPreview(primalTheme = PrimalTheme.Sunset) {
        MissingCameraPermissionContent(onPermissionChange = {})
    }
}
