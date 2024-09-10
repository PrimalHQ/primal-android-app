package net.primal.android.notes.feed.note

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import net.primal.android.R
import net.primal.android.theme.AppTheme

@Composable
fun ConfirmFirstBookmarkAlertDialog(onClose: () -> Unit, onBookmarkConfirmed: () -> Unit) {
    AlertDialog(
        containerColor = AppTheme.colorScheme.surfaceVariant,
        onDismissRequest = onClose,
        title = {
            Text(
                text = stringResource(id = R.string.context_confirm_bookmark_title),
                style = AppTheme.typography.titleLarge,
            )
        },
        text = {
            Text(
                text = stringResource(id = R.string.context_confirm_bookmark_text),
                style = AppTheme.typography.bodyLarge,
            )
        },
        dismissButton = {
            TextButton(onClick = onClose) {
                Text(text = stringResource(id = R.string.context_confirm_bookmark_negative))
            }
        },
        confirmButton = {
            TextButton(onClick = onBookmarkConfirmed) {
                Text(
                    text = stringResource(id = R.string.context_confirm_bookmark_positive),
                )
            }
        },
    )
}
