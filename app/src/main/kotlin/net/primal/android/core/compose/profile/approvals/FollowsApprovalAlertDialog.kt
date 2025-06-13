package net.primal.android.core.compose.profile.approvals

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import net.primal.android.R
import net.primal.android.theme.AppTheme
import net.primal.android.user.handler.ProfileFollowsHandler
import net.primal.android.user.handler.ProfileFollowsHandler.Companion.foldActions

@Composable
fun FollowsApprovalAlertDialog(
    followsApproval: FollowsApproval,
    onFollowsActionsApproved: (FollowsApproval) -> Unit,
    onClose: () -> Unit,
) {
    val finalFollowCount = remember(followsApproval) {
        emptySet<String>().foldActions(actions = followsApproval.actions).size
    }

    AlertDialog(
        containerColor = AppTheme.colorScheme.surfaceVariant,
        onDismissRequest = onClose,
        title = {
            Text(
                text = stringResource(id = R.string.context_confirm_follow_all_title),
                style = AppTheme.typography.titleLarge,
            )
        },
        text = {
            Text(
                text = pluralStringResource(
                    id = R.plurals.context_confirm_follow_all_text,
                    finalFollowCount,
                    finalFollowCount,
                ),
                style = AppTheme.typography.bodyLarge,
            )
        },
        dismissButton = {
            TextButton(onClick = onClose) {
                Text(text = stringResource(id = R.string.context_confirm_follow_all_negative))
            }
        },
        confirmButton = {
            TextButton(onClick = { onFollowsActionsApproved(followsApproval) }) {
                Text(text = stringResource(id = R.string.context_confirm_follow_all_positive))
            }
        },
    )
}

data class FollowsApproval(val actions: List<ProfileFollowsHandler.Action>)
