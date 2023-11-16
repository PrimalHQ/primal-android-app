package net.primal.android.core.compose.dropdown

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MenuDefaults
import androidx.compose.material3.MenuItemColors
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import net.primal.android.theme.AppTheme

@Composable
fun DropdownPrimalMenuItem(
    text: String,
    trailingIconVector: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    tint: Color = LocalContentColor.current,
    colors: MenuItemColors = MenuDefaults.itemColors(),
    contentPadding: PaddingValues = PaddingValues(
        horizontal = 16.dp,
        vertical = 0.dp,
    ),
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
) {
    DropdownMenuItem(
        modifier = modifier,
        trailingIcon = {
            Icon(
                imageVector = trailingIconVector,
                tint = tint,
                contentDescription = null,
            )
        },
        text = {
            Text(
                modifier = Modifier
                    .padding(end = 32.dp)
                    .padding(start = 8.dp),
                text = text,
                color = tint,
                style = AppTheme.typography.bodyMedium,
            )
        },
        onClick = onClick,
        enabled = enabled,
        colors = colors,
        contentPadding = contentPadding,
        interactionSource = interactionSource,
    )
}
