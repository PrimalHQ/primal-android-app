package net.primal.android.explore.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DrawerState
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import net.primal.android.R
import net.primal.android.core.compose.AvatarThumbnailListItemImage
import net.primal.android.core.compose.IconText
import net.primal.android.core.compose.PrimalTopLevelDestination
import net.primal.android.drawer.DrawerScreenDestination
import net.primal.android.drawer.PrimalDrawerScaffold
import net.primal.android.theme.AppTheme
import net.primal.android.theme.PrimalTheme

@Composable
fun ExploreHomeScreen(
    viewModel: ExploreHomeViewModel,
    onHashtagClick: (String) -> Unit,
    onSearchClick: () -> Unit,
    onTopLevelDestinationChanged: (PrimalTopLevelDestination) -> Unit,
    onDrawerScreenClick: (DrawerScreenDestination) -> Unit,
) {
    val uiState = viewModel.state.collectAsState()

    ExploreHomeScreen(
        state = uiState.value,
        onHashtagClick = onHashtagClick,
        onSearchClick = onSearchClick,
        onPrimaryDestinationChanged = onTopLevelDestinationChanged,
        onDrawerDestinationClick = onDrawerScreenClick,
    )
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ExploreHomeScreen(
    state: ExploreHomeContract.UiState,
    onHashtagClick: (String) -> Unit,
    onSearchClick: () -> Unit,
    onPrimaryDestinationChanged: (PrimalTopLevelDestination) -> Unit,
    onDrawerDestinationClick: (DrawerScreenDestination) -> Unit,
) {
    val uiScope = rememberCoroutineScope()
    val drawerState: DrawerState = rememberDrawerState(DrawerValue.Closed)
    val listState = rememberLazyListState()

    PrimalDrawerScaffold(
        drawerState = drawerState,
        activeDestination = PrimalTopLevelDestination.Explore,
        onActiveDestinationClick = { uiScope.launch { listState.animateScrollToItem(0) } },
        onPrimaryDestinationChanged = onPrimaryDestinationChanged,
        onDrawerDestinationClick = onDrawerDestinationClick,
        topBar = {
            ExploreTopAppBar(
                title = { SearchBar(
                    onClick = onSearchClick,
                ) },
                avatarUrl = state.activeAccountAvatarUrl,
                onNavigationIconClick = {
                    uiScope.launch { drawerState.open() }
                },
                scrollBehavior = it,
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
                            .wrapContentWidth()
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.Top,
                    ) {
                        it.forEach {
                            SuggestionChip(
                                modifier = Modifier
                                    .height(56.dp)
                                    .padding(all = 8.dp),
                                onClick = { onHashtagClick("#${it.name}") },
                                shape = AppTheme.shapes.extraLarge,
                                border = SuggestionChipDefaults.suggestionChipBorder(
                                    borderColor = AppTheme.extraColorScheme.surfaceVariantAlt,
                                ),
                                colors = SuggestionChipDefaults.suggestionChipColors(
                                    containerColor = AppTheme.extraColorScheme.surfaceVariantAlt,
                                    labelColor = AppTheme.colorScheme.onSurface,
                                ),
                                label = {
                                    Text(
                                        modifier = Modifier.padding(all = 4.dp),
                                        text = it.name,
                                        style = AppTheme.typography.bodyLarge,
                                        fontWeight = FontWeight.Medium,
                                    )
                                }
                            )
                        }
                    }
                }
            }
        }
    )
}

@ExperimentalMaterial3Api
@Composable
fun ExploreTopAppBar(
    title: @Composable () -> Unit,
    onNavigationIconClick: () -> Unit,
    avatarUrl: String? = null,
    actions: @Composable RowScope.() -> Unit = {},
    scrollBehavior: TopAppBarScrollBehavior? = null,
) {
    CenterAlignedTopAppBar(
        navigationIcon = {
            Box(
                modifier = Modifier
                    .padding(horizontal = 8.dp)
                    .clip(CircleShape)
                    .clickable(onClick = onNavigationIconClick)
            ) {
                AvatarThumbnailListItemImage(
                    source = avatarUrl,
                    modifier = Modifier.size(32.dp),
                )
            }
        },
        title = title,
        actions = actions,
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
            containerColor = AppTheme.colorScheme.surface,
            scrolledContainerColor = AppTheme.colorScheme.surface,
        ),
        scrollBehavior = scrollBehavior,
    )
}

@Composable
fun SearchBar(
    onClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .height(34.dp)
            .fillMaxWidth()
            .padding(horizontal = 32.dp)
            .clickable { onClick() }
            .background(
                color = AppTheme.extraColorScheme.surfaceVariantAlt,
                shape = AppTheme.shapes.extraLarge,
            ),
        contentAlignment = Alignment.Center,
    ) {
        IconText(
            leadingIcon = Icons.Default.Search,
            leadingIconTintColor = AppTheme.extraColorScheme.onSurfaceVariantAlt3,
            text = stringResource(id = R.string.explore_search_nostr),
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
    PrimalTheme {
        Surface {
            ExploreTopAppBar(
                title = {
                    SearchBar(
                        onClick = {},
                    )
                },
                onNavigationIconClick = {},
            )
        }
    }
}
