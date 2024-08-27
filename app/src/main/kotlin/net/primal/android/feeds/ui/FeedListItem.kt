package net.primal.android.feeds.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import net.primal.android.feeds.ui.model.FeedUi
import net.primal.android.theme.AppTheme

@Composable
fun FeedListItem(
    data: FeedUi,
    selected: Boolean,
    onFeedClick: ((FeedUi) -> Unit)? = null,
) {
    ListItem(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clip(AppTheme.shapes.large)
            .clickable { onFeedClick?.invoke(data) },
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
                text = data.name,
                textAlign = if (data.description.isNotEmpty()) TextAlign.Start else TextAlign.Center,
            )
        },
        supportingContent = {
            if (data.description.isNotEmpty()) {
                Text(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 6.dp),
                    style = AppTheme.typography.bodySmall.copy(fontSize = 15.sp),
                    color = AppTheme.extraColorScheme.onSurfaceVariantAlt3,
                    text = data.description,
                )
            }
        },
    )
}
