package net.primal.android.core.compose

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import net.primal.android.theme.AppTheme

@Composable
fun ConfirmActionAlertDialog(
    confirmText: String,
    dismissText: String,
    onDismissRequest: () -> Unit,
    onConfirmation: () -> Unit,
    dialogTitle: String? = null,
    dialogText: String? = null,
) {
    AlertDialog(
        containerColor = AppTheme.colorScheme.surfaceVariant,
        title = {
            if (dialogTitle != null) {
                Text(text = dialogTitle)
            }
        },
        text = {
            if (dialogText != null) {
                Text(text = dialogText)
            }
        },
        onDismissRequest = {
            onDismissRequest()
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirmation() },
            ) {
                Text(text = confirmText)
            }
        },
        dismissButton = {
            TextButton(
                onClick = { onDismissRequest() },
            ) {
                Text(text = dismissText)
            }
        },
    )
}
