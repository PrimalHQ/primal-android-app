package net.primal.android.wallet.activation.ui

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import net.primal.android.R
import net.primal.android.theme.AppTheme

@Composable
fun WalletActivationErrorHandler(
    error: Throwable?,
    fallbackMessage: String,
    onErrorDismiss: () -> Unit,
) {
    if (error != null) {
        val text = (error.message ?: "").ifEmpty { fallbackMessage }
        AlertDialog(
            containerColor = AppTheme.colorScheme.surfaceVariant,
            onDismissRequest = onErrorDismiss,
            title = {
                Text(
                    text = stringResource(id = R.string.wallet_activation_error_dialog_title),
                    style = AppTheme.typography.titleLarge,
                )
            },
            text = {
                Text(
                    text = text,
                    style = AppTheme.typography.bodyLarge,
                )
            },
            confirmButton = {
                TextButton(onClick = onErrorDismiss) {
                    Text(
                        text = stringResource(id = android.R.string.ok),
                    )
                }
            },
        )
    }
}
