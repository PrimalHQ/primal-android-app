package net.primal.android.core.compose.nostrconnect

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import net.primal.android.core.ext.selectableItem
import net.primal.android.theme.AppTheme

private val DISABLED_ICON_TINT = Color(0xFF808080)

@Composable
fun PermissionsListItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    ListItem(
        modifier = modifier.selectableItem(
            selected = isSelected,
            onClick = onClick,
        ),
        colors = ListItemDefaults.colors(containerColor = AppTheme.extraColorScheme.surfaceVariantAlt1),
        leadingContent = {
            Icon(
                modifier = Modifier.size(28.dp),
                imageVector = icon,
                contentDescription = null,
                tint = if (isSelected) AppTheme.colorScheme.onPrimary else DISABLED_ICON_TINT,
            )
        },
        headlineContent = {
            Text(
                text = title,
                fontWeight = FontWeight.SemiBold,
                color = AppTheme.colorScheme.onSurface,
                style = AppTheme.typography.bodyLarge,
            )
        },
        supportingContent = {
            Text(
                modifier = Modifier.padding(top = 3.dp),
                text = subtitle,
                color = AppTheme.extraColorScheme.onSurfaceVariantAlt3,
                style = AppTheme.typography.bodyMedium,
            )
        },
    )
}
