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
    enabled: Boolean = true,
    onClick: (() -> Unit)? = null,
    colors: ListItemColors = ListItemDefaults.colors(
        containerColor = AppTheme.colorScheme.surfaceVariant,
    ),
) {
    ListItem(
        modifier = modifier.clickable(
            enabled = enabled && onClick != null,
            onClick = { onClick?.invoke() },
        ),
        headlineContent = {
            Text(
                modifier = Modifier.padding(bottom = 4.dp),
                text = headlineText,
                style = AppTheme.typography.bodyLarge,
                color = if (enabled) {
                    AppTheme.colorScheme.onPrimary
                } else {
                    AppTheme.extraColorScheme.onSurfaceVariantAlt3
                },
            )
        },
        supportingContent = if (supportText != null) {
            {
                Text(
                    modifier = Modifier.padding(top = 4.dp),
                    text = supportText,
                    style = AppTheme.typography.bodySmall,
                    color = if (enabled) {
                        AppTheme.extraColorScheme.onSurfaceVariantAlt1
                    } else {
                        AppTheme.extraColorScheme.onSurfaceVariantAlt4
                    },
                )
            }
        } else {
            null
        },
        trailingContent = trailingContent,
        colors = colors,
    )
}
