package net.primal.android.core.compose.profile.approvals

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import net.primal.android.R
import net.primal.android.theme.AppTheme

@Composable
fun ApproveFollowUnfollowProfileAlertDialog(
    profileAction: ProfileAction,
    onActionApproved: () -> Unit,
    onClose: () -> Unit,
) {
    val messages = when (profileAction) {
        is ProfileAction.Follow -> {
            ApprovalMessages(
                title = stringResource(id = R.string.context_confirm_follow_title),
                text = stringResource(id = R.string.context_confirm_follow_text),
                positive = stringResource(id = R.string.context_confirm_follow_positive),
                negative = stringResource(id = R.string.context_confirm_follow_negative),
            )
        }

        is ProfileAction.Unfollow -> {
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
            TextButton(onClick = onActionApproved) {
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

sealed class ProfileAction(open val profileId: String) {
    data class Follow(override val profileId: String) : ProfileAction(profileId)
    data class Unfollow(override val profileId: String) : ProfileAction(profileId)
}
