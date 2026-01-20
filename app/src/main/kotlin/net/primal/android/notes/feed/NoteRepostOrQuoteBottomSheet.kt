package net.primal.android.notes.feed

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import net.primal.android.R
import net.primal.android.core.compose.IconText
import net.primal.android.core.compose.button.PrimalFilledButton
import net.primal.android.core.compose.icons.PrimalIcons
import net.primal.android.core.compose.icons.primaliconpack.DeleteRepost
import net.primal.android.core.compose.icons.primaliconpack.Quote
import net.primal.android.core.compose.icons.primaliconpack.Repost
import net.primal.android.theme.AppTheme

@ExperimentalMaterial3Api
@Composable
fun NoteRepostOrQuoteBottomSheet(
    isReposted: Boolean,
    onDismiss: () -> Unit,
    onRepostClick: () -> Unit,
    onDeleteRepostClick: () -> Unit,
    onPostQuoteClick: () -> Unit,
) {
    ModalBottomSheet(
        containerColor = AppTheme.extraColorScheme.surfaceVariantAlt2,
        tonalElevation = 0.dp,
        onDismissRequest = onDismiss,
    ) {
        Column(
            modifier = Modifier
                .padding(24.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            if (isReposted) {
                ActionButton(
                    text = stringResource(id = R.string.post_repost_again_button_confirmation),
                    leadingIcon = PrimalIcons.Repost,
                    onClick = {
                        onDismiss()
                        onRepostClick()
                    },
                )
                ActionButton(
                    text = stringResource(id = R.string.post_delete_repost_button_confirmation),
                    contentColor = AppTheme.colorScheme.error,
                    leadingIcon = PrimalIcons.DeleteRepost,
                    onClick = {
                        onDismiss()
                        onDeleteRepostClick()
                    },
                )
            } else {
                ActionButton(
                    text = stringResource(id = R.string.post_repost_button_confirmation),
                    leadingIcon = PrimalIcons.Repost,
                    onClick = {
                        onDismiss()
                        onRepostClick()
                    },
                )
            }

            ActionButton(
                text = stringResource(id = R.string.post_quote_button_confirmation),
                leadingIcon = PrimalIcons.Quote,
                onClick = {
                    onDismiss()
                    onPostQuoteClick()
                },
            )
        }
    }
}

@Composable
private fun ActionButton(
    modifier: Modifier = Modifier,
    text: String,
    leadingIcon: ImageVector,
    contentColor: Color = AppTheme.colorScheme.onSurface,
    onClick: () -> Unit,
) {
    PrimalFilledButton(
        modifier = modifier.fillMaxWidth(),
        height = 56.dp,
        containerColor = AppTheme.extraColorScheme.surfaceVariantAlt1,
        contentColor = contentColor,
        textStyle = AppTheme.typography.bodyLarge.copy(
            fontSize = 18.sp,
        ),
        onClick = onClick,
    ) {
        IconText(
            text = text,
            leadingIcon = leadingIcon,
            leadingIconTintColor = contentColor,
            iconSize = 32.sp,
        )
    }
}
