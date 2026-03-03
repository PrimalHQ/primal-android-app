package net.primal.android.editor.ui.poll

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import net.primal.android.R
import net.primal.android.core.compose.icons.PrimalIcons
import net.primal.android.core.compose.icons.primaliconpack.DrawerProfileFilled
import net.primal.android.core.compose.icons.primaliconpack.LightningBoltFilled
import net.primal.android.editor.NoteEditorContract.PollType
import net.primal.android.theme.AppTheme

@Composable
fun PollTypeDialog(
    selectedType: PollType,
    onTypeSelected: (PollType) -> Unit,
    onDismiss: () -> Unit,
) {
    Dialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .clip(AppTheme.shapes.extraLarge)
                .background(AppTheme.extraColorScheme.surfaceVariantAlt1)
                .padding(24.dp),
        ) {
            Text(
                text = stringResource(id = R.string.poll_editor_select_poll_type),
                style = AppTheme.typography.bodyLarge,
                color = AppTheme.colorScheme.onSurface,
            )

            Spacer(modifier = Modifier.height(16.dp))

            PollTypeOption(
                icon = {
                    Icon(
                        PrimalIcons.DrawerProfileFilled,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                        tint = AppTheme.colorScheme.onSurface,
                    )
                },
                title = stringResource(id = R.string.poll_editor_user_poll),
                description = stringResource(id = R.string.poll_editor_user_poll_description),
                isSelected = selectedType == PollType.UserPoll,
                onClick = { onTypeSelected(PollType.UserPoll) },
            )

            Spacer(modifier = Modifier.height(8.dp))

            PollTypeOption(
                icon = {
                    Icon(
                        PrimalIcons.LightningBoltFilled,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                        tint = AppTheme.colorScheme.onSurface,
                    )
                },
                title = stringResource(id = R.string.poll_editor_zap_poll),
                description = stringResource(id = R.string.poll_editor_zap_poll_description),
                isSelected = selectedType == PollType.ZapPoll,
                onClick = { onTypeSelected(PollType.ZapPoll) },
            )
        }
    }
}

@Composable
private fun PollTypeOption(
    icon: @Composable () -> Unit,
    title: String,
    description: String,
    isSelected: Boolean,
    onClick: () -> Unit,
) {
    val borderColor = if (isSelected) AppTheme.colorScheme.primary else AppTheme.colorScheme.outline
    val backgroundColor = if (isSelected) AppTheme.extraColorScheme.surfaceVariantAlt2 else Color.Transparent
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(AppTheme.shapes.medium)
            .background(backgroundColor)
            .border(width = 1.dp, color = borderColor, shape = AppTheme.shapes.medium)
            .clickable(onClick = onClick)
            .padding(12.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            icon()
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = title,
                style = AppTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                color = AppTheme.colorScheme.onSurface,
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = description,
            style = AppTheme.typography.bodySmall,
            color = AppTheme.extraColorScheme.onSurfaceVariantAlt3,
            lineHeight = 16.sp,
        )
    }
}
