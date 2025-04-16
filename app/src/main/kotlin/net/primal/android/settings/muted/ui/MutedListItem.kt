package net.primal.android.settings.muted.ui

import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import net.primal.android.R
import net.primal.android.core.compose.button.PrimalFilledButton
import net.primal.android.theme.AppTheme

@Composable
fun MutedListItem(item: String, onUnmuteClick: (String) -> Unit) {
    ListItem(
        colors = ListItemDefaults.colors(
            containerColor = AppTheme.colorScheme.surfaceVariant,
        ),
        headlineContent = {
            Text(
                text = item,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = AppTheme.typography.headlineLarge,
                fontSize = 14.sp,
            )
        },
        trailingContent = {
            PrimalFilledButton(
                modifier = Modifier
                    .wrapContentWidth()
                    .height(36.dp),
                containerColor = AppTheme.extraColorScheme.surfaceVariantAlt1,
                contentColor = AppTheme.colorScheme.onSurface,
                textStyle = AppTheme.typography.titleMedium.copy(
                    lineHeight = 18.sp,
                ),
                onClick = { onUnmuteClick(item) },
            ) {
                Text(
                    text = stringResource(
                        id = R.string.settings_muted_accounts_unmute_button,
                    ).lowercase(),
                )
            }
        },
    )
}
