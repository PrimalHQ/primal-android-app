package net.primal.android.profile.details.ui

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import net.primal.android.R
import net.primal.android.theme.AppTheme

@Composable
fun ConfirmFollowUnfollowProfileAlertDialog(
    onClose: () -> Unit,
    onActionConfirmed: () -> Unit,
    profileAction: ProfileAction,
) {
    val (title, text, positive, negative) = when (profileAction) {
        ProfileAction.Follow -> {
            arrayOf(
                stringResource(id = R.string.context_confirm_follow_title),
                stringResource(id = R.string.context_confirm_follow_text),
                stringResource(id = R.string.context_confirm_follow_positive),
                stringResource(id = R.string.context_confirm_follow_negative),
            )
        }
        ProfileAction.Unfollow -> {
            arrayOf(
                stringResource(id = R.string.context_confirm_unfollow_title),
                stringResource(id = R.string.context_confirm_unfollow_text),
                stringResource(id = R.string.context_confirm_unfollow_positive),
                stringResource(id = R.string.context_confirm_unfollow_negative),
            )
        }
    }
    AlertDialog(
        containerColor = AppTheme.colorScheme.surfaceVariant,
        onDismissRequest = onClose,
        title = {
            Text(
                text = title,
                style = AppTheme.typography.titleLarge,
            )
        },
        text = {
            Text(
                text = text,
                style = AppTheme.typography.bodyLarge,
            )
        },
        dismissButton = {
            TextButton(onClick = onClose) {
                Text(text = negative)
            }
        },
        confirmButton = {
            TextButton(onClick = onActionConfirmed) {
                Text(
                    text = positive,
                )
            }
        },
    )
}

enum class ProfileAction {
    Follow,
    Unfollow,
}
