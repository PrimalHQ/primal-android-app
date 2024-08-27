package net.primal.android.feeds.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import net.primal.android.core.compose.PrimalDivider
import net.primal.android.feeds.ui.model.FeedUi
import net.primal.android.theme.AppTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeedList(
    title: String,
    feeds: List<FeedUi>,
    activeFeed: FeedUi?,
    onFeedClick: (FeedUi) -> Unit,
    onAddFeedClick: () -> Unit,
    modifier: Modifier = Modifier,
    showAddFeed: Boolean = false,
) {
    Column(modifier = modifier) {
        CenterAlignedTopAppBar(
            colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                containerColor = AppTheme.extraColorScheme.surfaceVariantAlt2,
            ),
            title = {
                Text(text = title)
            },
        )
        LazyColumn(
            modifier = modifier.weight(1f),
        ) {
            items(
                items = feeds,
                key = { it.directive },
            ) {
                FeedListItem(
                    data = it,
                    selected = it.directive == activeFeed?.directive,
                    onFeedClick = onFeedClick,
                )
            }
        }
        if (showAddFeed) {
            PrimalDivider()
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
            ) {
                TextButton(onClick = onAddFeedClick) {
                    Text(text = "Add feed")
                }
            }
        }
    }
}
