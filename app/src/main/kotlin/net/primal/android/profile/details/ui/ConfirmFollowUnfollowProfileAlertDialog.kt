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
    val messages = when (profileAction) {
        ProfileAction.Follow -> {
            ApprovalMessages(
                title = stringResource(id = R.string.context_confirm_follow_title),
                text = stringResource(id = R.string.context_confirm_follow_text),
                positive = stringResource(id = R.string.context_confirm_follow_positive),
                negative = stringResource(id = R.string.context_confirm_follow_negative),
            )
        }
        ProfileAction.Unfollow -> {
            ApprovalMessages(
                title = stringResource(id = R.string.context_confirm_unfollow_title),
                text = stringResource(id = R.string.context_confirm_unfollow_text),
                positive = stringResource(id = R.string.context_confirm_unfollow_positive),
                negative = stringResource(id = R.string.context_confirm_unfollow_negative),
            )
        }
    }
    AlertDialog(
        containerColor = AppTheme.colorScheme.surfaceVariant,
        onDismissRequest = onClose,
        title = {
            Text(
                text = messages.title,
                style = AppTheme.typography.titleLarge,
            )
        },
        text = {
            Text(
                text = messages.text,
                style = AppTheme.typography.bodyLarge,
            )
        },
        dismissButton = {
            TextButton(onClick = onClose) {
                Text(text = messages.negative)
            }
        },
        confirmButton = {
            TextButton(onClick = onActionConfirmed) {
                Text(
                    text = messages.positive,
                )
            }
        },
    )
}

private data class ApprovalMessages(
    val title: String,
    val text: String,
    val positive: String,
    val negative: String,
)

enum class ProfileAction {
    Follow,
    Unfollow,
}
