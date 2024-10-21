package net.primal.android.core.compose.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemColors
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import net.primal.android.theme.AppTheme

@Composable
fun SettingsItem(
    headlineText: String,
    modifier: Modifier = Modifier,
    supportText: String? = null,
    trailingContent: @Composable (() -> Unit)? = null,
    onClick: (() -> Unit)? = null,
    colors: ListItemColors = ListItemDefaults.colors(
        containerColor = AppTheme.colorScheme.surfaceVariant,
    ),
) {
    ListItem(
        modifier = modifier.clickable(
            enabled = onClick != null,
            onClick = { onClick?.invoke() },
        ),
        headlineContent = {
            Text(
                modifier = Modifier.padding(bottom = 4.dp),
                text = headlineText,
                style = AppTheme.typography.bodyLarge,
                color = AppTheme.colorScheme.onPrimary,
            )
        },
        supportingContent = if (supportText != null) {
            {
                Text(
                    modifier = Modifier.padding(top = 4.dp),
                    text = supportText,
                    style = AppTheme.typography.bodySmall,
                    color = AppTheme.extraColorScheme.onSurfaceVariantAlt1,
                )
            }
        } else {
            null
        },
        trailingContent = trailingContent,
        colors = colors,
    )
}
