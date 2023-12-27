package net.primal.android.wallet.send.prepare.tabs

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import net.primal.android.R
import net.primal.android.core.compose.PrimalDefaults
import net.primal.android.core.compose.button.PrimalLoadingButton
import net.primal.android.core.compose.icons.PrimalIcons
import net.primal.android.core.compose.icons.primaliconpack.Paste
import net.primal.android.theme.AppTheme
import net.primal.android.theme.PrimalTheme
import net.primal.android.wallet.utils.isLightningAddress
import net.primal.android.wallet.utils.isLightningAddressUri
import net.primal.android.wallet.utils.isLnInvoice
import net.primal.android.wallet.utils.isLnUrl

@Composable
fun SendPaymentTabText(parsing: Boolean, onTextConfirmed: (String) -> Unit) {
    val clipboardManager = LocalClipboardManager.current

    var textState by remember { mutableStateOf("") }

    val isInputValid by remember {
        derivedStateOf { textState.hasLnRecipient() }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(state = rememberScrollState()),
        verticalArrangement = Arrangement.SpaceBetween,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Column {
            OutlinedTextField(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp)
                    .padding(horizontal = 16.dp),
                value = textState,
                onValueChange = { textState = it },
                enabled = !parsing,
                colors = PrimalDefaults.outlinedTextFieldColors(),
                shape = AppTheme.shapes.extraLarge,
            )

            Text(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 72.dp),
                text = stringResource(id = R.string.wallet_send_payment_text_description),
                textAlign = TextAlign.Center,
                style = AppTheme.typography.bodyMedium,
                color = AppTheme.extraColorScheme.onSurfaceVariantAlt2,
            )
        }

        if (isInputValid) {
            PrimalLoadingButton(
                modifier = Modifier
                    .padding(vertical = 16.dp)
                    .fillMaxWidth(fraction = 0.7f),
                onClick = { onTextConfirmed(textState) },
                loading = parsing,
                enabled = !parsing,
                text = stringResource(id = R.string.wallet_send_payment_text_next),
            )
        } else {
            PrimalLoadingButton(
                modifier = Modifier.padding(vertical = 16.dp),
                onClick = {
                    val clipboardText = clipboardManager.getText()?.text.orEmpty().trim()
                    if (clipboardText.hasLnRecipient()) {
                        textState = clipboardText
                    }
                },
                containerColor = AppTheme.extraColorScheme.surfaceVariantAlt1,
                contentColor = AppTheme.extraColorScheme.onSurfaceVariantAlt2,
                fontWeight = FontWeight.Normal,
                fontSize = 16.sp,
                text = stringResource(id = R.string.wallet_send_payment_text_paste_from_keyboard),
                leadingIcon = PrimalIcons.Paste,
            )
        }
    }
}

private fun String.hasLnRecipient(): Boolean {
    return isLnInvoice() || isLnUrl() || isLightningAddress() || isLightningAddressUri()
}

@Preview
@Composable
fun PreviewSendPaymentTabText() {
    PrimalTheme(primalTheme = net.primal.android.theme.domain.PrimalTheme.Sunrise) {
        Surface {
            SendPaymentTabText(
                parsing = false,
                onTextConfirmed = {},
            )
        }
    }
}
