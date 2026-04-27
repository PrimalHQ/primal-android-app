package net.primal.android.core.compose.picker

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import net.primal.android.theme.AppTheme

@Composable
fun BasePickerListItem(
    title: String,
    selected: Boolean,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    titleAlignment: TextAlign = TextAlign.Start,
    supportingContent: @Composable (() -> Unit)? = subtitle?.let { text ->
        { PickerListItemSupportingText(text = text) }
    },
    trailingContent: @Composable (() -> Unit)? = if (selected) {
        { PickerListItemCheckIcon() }
    } else {
        null
    },
) {
    ListItem(
        modifier = modifier,
        colors = ListItemDefaults.colors(
            containerColor = if (selected) {
                AppTheme.extraColorScheme.surfaceVariantAlt1
            } else {
                AppTheme.extraColorScheme.surfaceVariantAlt2
            },
        ),
        headlineContent = {
            Text(
                modifier = Modifier.fillMaxWidth(),
                style = AppTheme.typography.bodyLarge,
                color = AppTheme.colorScheme.onSurface,
                text = title,
                textAlign = titleAlignment,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        },
        supportingContent = supportingContent,
        trailingContent = trailingContent,
    )
}

@Composable
private fun PickerListItemSupportingText(text: String) {
    Text(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 6.dp),
        style = AppTheme.typography.bodySmall.copy(fontSize = 15.sp),
        color = AppTheme.extraColorScheme.onSurfaceVariantAlt3,
        text = text,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
    )
}

@Composable
fun PickerListItemCheckIcon() {
    Icon(
        modifier = Modifier.size(20.dp),
        imageVector = Icons.Default.Check,
        contentDescription = null,
        tint = AppTheme.colorScheme.onSurface,
    )
}
