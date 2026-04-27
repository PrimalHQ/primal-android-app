package net.primal.android.main.explore.filter

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import net.primal.android.theme.AppTheme

@Composable
fun ExploreFilterListOverlayContent(
    activeFilter: ExploreFilter,
    onFilterClick: (ExploreFilter) -> Unit,
    onDismiss: () -> Unit,
) {
    BackHandler { onDismiss() }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(color = AppTheme.extraColorScheme.surfaceVariantAlt2)
            .padding(top = 16.dp)
            .navigationBarsPadding(),
        contentPadding = PaddingValues(vertical = 1.dp),
    ) {
        items(items = ExploreFilter.entries, key = { it.name }) { filter ->
            val interactionSource = remember { MutableInteractionSource() }
            ExploreFilterListItem(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .clip(AppTheme.shapes.large)
                    .clickable(
                        interactionSource = interactionSource,
                        indication = LocalIndication.current,
                        onClick = { onFilterClick(filter) },
                    ),
                filter = filter,
                selected = filter == activeFilter,
            )
        }
    }
}

@Composable
private fun ExploreFilterListItem(
    modifier: Modifier,
    filter: ExploreFilter,
    selected: Boolean,
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
                text = stringResource(id = filter.titleRes),
                textAlign = TextAlign.Start,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        },
        supportingContent = {
            Text(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 6.dp),
                style = AppTheme.typography.bodySmall.copy(fontSize = 15.sp),
                color = AppTheme.extraColorScheme.onSurfaceVariantAlt3,
                text = stringResource(id = filter.subtitleRes),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        },
        trailingContent = {
            if (selected) {
                Icon(
                    modifier = Modifier.size(20.dp),
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    tint = AppTheme.colorScheme.onSurface,
                )
            }
        },
    )
}
