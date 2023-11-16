package net.primal.android.auth.logout

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import net.primal.android.R
import net.primal.android.theme.AppTheme

@Composable
fun LogoutScreen(viewModel: LogoutViewModel, onClose: () -> Unit) {
    LogoutScreen(
        onClose = onClose,
        onLogoutRequested = {
            viewModel.setEvent(LogoutContract.UiEvent.LogoutConfirmed)
        },
    )
}

@Composable
fun LogoutScreen(onClose: () -> Unit, onLogoutRequested: () -> Unit) {
    AlertDialog(
        containerColor = AppTheme.colorScheme.surfaceVariant,
        onDismissRequest = onClose,
        title = {
            Text(
                text = stringResource(id = R.string.logout_title),
                style = AppTheme.typography.titleLarge,
            )
        },
        text = {
            Text(
                text = stringResource(id = R.string.logout_description),
                style = AppTheme.typography.bodyLarge,
            )
        },
        dismissButton = {
            TextButton(onClick = onClose) {
                Text(text = stringResource(id = R.string.logout_cancel_button))
            }
        },
        confirmButton = {
            TextButton(onClick = onLogoutRequested) {
                Text(
                    text = stringResource(id = R.string.logout_button),
                )
            }
        },
    )
}
