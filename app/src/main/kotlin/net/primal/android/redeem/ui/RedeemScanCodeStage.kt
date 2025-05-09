package net.primal.android.redeem.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import net.primal.android.R
import net.primal.android.core.compose.button.PrimalLoadingButton
import net.primal.android.scanner.QrCodeScanner
import net.primal.android.scanner.domain.QrCodeResult
import net.primal.android.theme.AppTheme

@Composable
fun RedeemScanCodeStage(
    modifier: Modifier = Modifier,
    onQrCodeDetected: (QrCodeResult) -> Unit,
    onEnterCodeClick: () -> Unit,
) {
    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween,
    ) {
        QrCodeScanner(
            cameraBoxModifier = Modifier.padding(top = 96.dp),
            cameraVisible = true,
            onQrCodeDetected = onQrCodeDetected,
        ) {
            Text(
                modifier = Modifier.padding(top = 16.dp),
                text = stringResource(id = R.string.redeem_code_scan_hint),
                color = Color.White,
                textAlign = TextAlign.Center,
                style = AppTheme.typography.bodyLarge,
            )
        }

        PrimalLoadingButton(
            text = stringResource(id = R.string.redeem_code_enter_code_button),
            containerColor = Color.Black.copy(alpha = 0.5f),
            disabledContainerColor = Color.Black.copy(alpha = 0.5f),
            contentColor = Color.White,
            onClick = onEnterCodeClick,
        )
    }
}
