package net.primal.android.explore.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import net.primal.android.R
import net.primal.android.core.compose.IconText
import net.primal.android.core.compose.ListLoading
import net.primal.android.core.compose.ListNoContent
import net.primal.android.core.compose.PrimalDivider
import net.primal.android.core.compose.icons.PrimalIcons
import net.primal.android.core.compose.icons.primaliconpack.AdvancedSearch
import net.primal.android.core.compose.icons.primaliconpack.ArrowBack
import net.primal.android.core.compose.preview.PrimalPreview
import net.primal.android.explore.home.ExploreHomeContract.UiEvent
import net.primal.android.theme.AppTheme
import net.primal.android.theme.domain.PrimalTheme

@Composable
fun ExploreHomeScreen(
    viewModel: ExploreHomeViewModel,
    onHashtagClick: (String) -> Unit,
    onSearchClick: () -> Unit,
    onTuneClick: () -> Unit,
    onClose: () -> Unit,
) {
    val uiState = viewModel.state.collectAsState()

    ExploreHomeScreen(
        state = uiState.value,
        onHashtagClick = onHashtagClick,
        onSearchClick = onSearchClick,
        eventPublisher = { viewModel.setEvent(it) },
        onTuneClick = onTuneClick,
        onClose = onClose,
    )
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
private fun ExploreHomeScreen(
    state: ExploreHomeContract.UiState,
    onHashtagClick: (String) -> Unit,
    onSearchClick: () -> Unit,
    eventPublisher: (UiEvent) -> Unit,
    onTuneClick: () -> Unit,
    onClose: () -> Unit,
) {
    val listState = rememberLazyListState()

    Scaffold(
        topBar = {
            ExploreTopAppBar(
                onClose = onClose,
                onSearchClick = onSearchClick,
                onActionIconClick = onTuneClick,
                actionIcon = PrimalIcons.AdvancedSearch,
            )
        },
        content = { paddingValues ->
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = paddingValues,
                state = listState,
            ) {
                items(
                    items = state.hashtags,
                    key = { it.first().name },
                ) {
                    FlowRow(
                        modifier = Modifier
                            .background(color = AppTheme.colorScheme.surfaceVariant)
                            .wrapContentWidth()
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalArrangement = Arrangement.Top,
                    ) {
                        it.forEach {
                            SuggestionChip(
                                modifier = Modifier
                                    .height(56.dp)
                                    .padding(all = 8.dp),
                                onClick = { onHashtagClick("#${it.name}") },
                                shape = AppTheme.shapes.extraLarge,
                                border = SuggestionChipDefaults.suggestionChipBorder(
                                    enabled = true,
                                    borderColor = AppTheme.extraColorScheme.surfaceVariantAlt1,
                                ),
                                colors = SuggestionChipDefaults.suggestionChipColors(
                                    containerColor = AppTheme.extraColorScheme.surfaceVariantAlt1,
                                    labelColor = AppTheme.colorScheme.onSurface,
                                ),
                                label = {
                                    Text(
                                        modifier = Modifier.padding(all = 4.dp),
                                        text = it.name,
                                        style = AppTheme.typography.bodyLarge,
                                        fontWeight = FontWeight.Medium,
                                    )
                                },
                            )
                        }
                    }
                }

                if (state.hashtags.isEmpty()) {
                    if (state.refreshing) {
                        item(contentType = "LoadingRefresh") {
                            ListLoading(
                                modifier = Modifier.fillParentMaxSize(),
                            )
                        }
                    } else {
                        item(contentType = "NoContent") {
                            ListNoContent(
                                modifier = Modifier.fillParentMaxSize(),
                                noContentText = stringResource(
                                    id = R.string.explore_trending_hashtags_no_content,
                                ),
                                refreshButtonVisible = true,
                                onRefresh = { eventPublisher(UiEvent.RefreshTrendingHashtags) },
                            )
                        }
                    }
                }
            }
        },
    )
}

@ExperimentalMaterial3Api
@Composable
fun ExploreTopAppBar(
    modifier: Modifier = Modifier,
    onSearchClick: () -> Unit,
    onActionIconClick: () -> Unit,
    onClose: () -> Unit,
    actionIcon: ImageVector,
) {
    Column(
        modifier = modifier.wrapContentHeight(),
    ) {
        TopAppBar(
            title = {
                SearchBar(
                    onClick = onSearchClick,
                )
            },
            navigationIcon = {
                IconButton(
                    onClick = onClose,
                ) {
                    Icon(
                        imageVector = PrimalIcons.ArrowBack,
                        contentDescription = null,
                    )
                }
            },
            actions = {
                IconButton(
                    onClick = onActionIconClick,
                ) {
                    Icon(
                        imageVector = actionIcon,
                        contentDescription = null,
                    )
                }
            },
            colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                containerColor = AppTheme.colorScheme.surface,
                scrolledContainerColor = AppTheme.colorScheme.surface,
            ),
        )
        PrimalDivider()
    }
}

@Composable
fun SearchBar(onClick: () -> Unit, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .height(34.dp)
            .fillMaxWidth()
            .clip(AppTheme.shapes.extraLarge)
            .clickable { onClick() }
            .background(
                color = AppTheme.extraColorScheme.surfaceVariantAlt1,
                shape = AppTheme.shapes.extraLarge,
            ),
        contentAlignment = Alignment.CenterStart,
    ) {
        IconText(
            modifier = Modifier.padding(horizontal = 8.dp),
            leadingIcon = Icons.Default.Search,
            leadingIconTintColor = AppTheme.extraColorScheme.onSurfaceVariantAlt3,
            text = stringResource(id = R.string.explore_search_nostr).lowercase(),
            color = AppTheme.extraColorScheme.onSurfaceVariantAlt3,
            style = AppTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview
@Composable
fun PreviewExploreTopAppBar() {
    PrimalPreview(primalTheme = PrimalTheme.Sunset) {
        Surface {
            ExploreTopAppBar(
                onSearchClick = {},
                onActionIconClick = {},
                onClose = {},
                actionIcon = Icons.Filled.Tune,
            )
        }
    }
}
