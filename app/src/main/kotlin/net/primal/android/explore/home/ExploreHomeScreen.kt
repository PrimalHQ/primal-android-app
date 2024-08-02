package net.primal.android.explore.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import net.primal.android.LocalContentDisplaySettings
import net.primal.android.R
import net.primal.android.attachments.domain.CdnImage
import net.primal.android.core.compose.AvatarThumbnail
import net.primal.android.core.compose.IconText
import net.primal.android.core.compose.InvisibleAppBarIcon
import net.primal.android.core.compose.ListLoading
import net.primal.android.core.compose.ListNoContent
import net.primal.android.core.compose.PrimalDivider
import net.primal.android.core.compose.PrimalTopLevelDestination
import net.primal.android.core.compose.preview.PrimalPreview
import net.primal.android.drawer.DrawerScreenDestination
import net.primal.android.drawer.PrimalDrawerScaffold
import net.primal.android.explore.home.ExploreHomeContract.UiEvent
import net.primal.android.theme.AppTheme
import net.primal.android.theme.domain.PrimalTheme

@Composable
@Deprecated("Outdated top level screen replaced with Reads.")
fun ExploreHomeScreen(
    viewModel: ExploreHomeViewModel,
    onHashtagClick: (String) -> Unit,
    onSearchClick: () -> Unit,
    onTopLevelDestinationChanged: (PrimalTopLevelDestination) -> Unit,
    onDrawerScreenClick: (DrawerScreenDestination) -> Unit,
    onDrawerQrCodeClick: () -> Unit,
) {
    val uiState = viewModel.state.collectAsState()

    ExploreHomeScreen(
        state = uiState.value,
        onHashtagClick = onHashtagClick,
        onSearchClick = onSearchClick,
        onPrimaryDestinationChanged = onTopLevelDestinationChanged,
        onDrawerDestinationClick = onDrawerScreenClick,
        onDrawerQrCodeClick = onDrawerQrCodeClick,
        eventPublisher = { viewModel.setEvent(it) },
    )
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
@Deprecated("Outdated top level screen replaced with Reads.")
private fun ExploreHomeScreen(
    state: ExploreHomeContract.UiState,
    onHashtagClick: (String) -> Unit,
    onSearchClick: () -> Unit,
    onPrimaryDestinationChanged: (PrimalTopLevelDestination) -> Unit,
    onDrawerDestinationClick: (DrawerScreenDestination) -> Unit,
    onDrawerQrCodeClick: () -> Unit,
    eventPublisher: (UiEvent) -> Unit,
) {
    val uiScope = rememberCoroutineScope()
    val drawerState: DrawerState = rememberDrawerState(DrawerValue.Closed)
    val listState = rememberLazyListState()

    PrimalDrawerScaffold(
        drawerState = drawerState,
        activeDestination = PrimalTopLevelDestination.Reads,
        onActiveDestinationClick = { uiScope.launch { listState.animateScrollToItem(0) } },
        onPrimaryDestinationChanged = onPrimaryDestinationChanged,
        onDrawerDestinationClick = onDrawerDestinationClick,
        onDrawerQrCodeClick = onDrawerQrCodeClick,
        focusModeEnabled = LocalContentDisplaySettings.current.focusModeEnabled && state.hashtags.isNotEmpty(),
        badges = state.badges,
        topBar = {
            ExploreTopAppBar(
                title = {
                    SearchBar(
                        onClick = onSearchClick,
                    )
                },
                avatarCdnImage = state.activeAccountAvatarCdnImage,
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
    title: @Composable () -> Unit,
    onNavigationIconClick: () -> Unit,
    avatarCdnImage: CdnImage? = null,
    actions: (@Composable RowScope.() -> Unit)? = null,
    scrollBehavior: TopAppBarScrollBehavior? = null,
) {
    Column {
        CenterAlignedTopAppBar(
            navigationIcon = {
                Box(
                    modifier = Modifier
                        .padding(horizontal = 8.dp)
                        .clip(CircleShape),
                ) {
                    AvatarThumbnail(
                        avatarCdnImage = avatarCdnImage,
                        modifier = Modifier.size(32.dp),
                        onClick = onNavigationIconClick,
                    )
                }
            },
            title = title,
            actions = actions ?: { InvisibleAppBarIcon() },
            colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                containerColor = AppTheme.colorScheme.surface,
                scrolledContainerColor = AppTheme.colorScheme.surface,
            ),
            scrollBehavior = scrollBehavior,
        )

        PrimalDivider()
    }
}

@Composable
fun SearchBar(onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .height(34.dp)
            .fillMaxWidth()
            .padding(horizontal = 32.dp)
            .clickable { onClick() }
            .background(
                color = AppTheme.extraColorScheme.surfaceVariantAlt1,
                shape = AppTheme.shapes.extraLarge,
            ),
        contentAlignment = Alignment.Center,
    ) {
        IconText(
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
