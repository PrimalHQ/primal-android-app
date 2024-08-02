package net.primal.android.articles.feed.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.DrawerState
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemContentType
import androidx.paging.compose.itemKey
import kotlinx.coroutines.launch
import net.primal.android.BuildConfig
import net.primal.android.LocalContentDisplaySettings
import net.primal.android.R
import net.primal.android.articles.feed.ArticleFeedScreenContract
import net.primal.android.articles.feed.ArticleFeedViewModel
import net.primal.android.core.compose.AppBarIcon
import net.primal.android.core.compose.ListLoading
import net.primal.android.core.compose.ListLoadingError
import net.primal.android.core.compose.ListNoContent
import net.primal.android.core.compose.PrimalDivider
import net.primal.android.core.compose.PrimalTopAppBar
import net.primal.android.core.compose.PrimalTopLevelDestination
import net.primal.android.core.compose.feed.model.ZappingState
import net.primal.android.core.compose.foundation.rememberLazyListStatePagingWorkaround
import net.primal.android.core.compose.icons.PrimalIcons
import net.primal.android.core.compose.icons.primaliconpack.AvatarDefault
import net.primal.android.core.compose.icons.primaliconpack.Search
import net.primal.android.core.compose.isEmpty
import net.primal.android.core.compose.isNotEmpty
import net.primal.android.drawer.DrawerScreenDestination
import net.primal.android.drawer.PrimalDrawerScaffold
import timber.log.Timber

@Composable
fun ArticleFeedScreen(
    viewModel: ArticleFeedViewModel,
    onTopLevelDestinationChanged: (PrimalTopLevelDestination) -> Unit,
    onDrawerScreenClick: (DrawerScreenDestination) -> Unit,
    onDrawerQrCodeClick: () -> Unit,
    onSearchClick: () -> Unit,
    onArticleClick: (naddr: String) -> Unit,
) {
    val uiState = viewModel.state.collectAsState()

    ArticleFeedScreen(
        state = uiState.value,
        onTopLevelDestinationChanged = onTopLevelDestinationChanged,
        onDrawerScreenClick = onDrawerScreenClick,
        onDrawerQrCodeClick = onDrawerQrCodeClick,
        onSearchClick = onSearchClick,
        onArticleClick = onArticleClick,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ArticleFeedScreen(
    state: ArticleFeedScreenContract.UiState,
    onTopLevelDestinationChanged: (PrimalTopLevelDestination) -> Unit,
    onDrawerScreenClick: (DrawerScreenDestination) -> Unit,
    onDrawerQrCodeClick: () -> Unit,
    onSearchClick: () -> Unit,
    onArticleClick: (naddr: String) -> Unit,
) {
    val uiScope = rememberCoroutineScope()
    val drawerState: DrawerState = rememberDrawerState(DrawerValue.Closed)
    val snackbarHostState = remember { SnackbarHostState() }

    val pagingItems = state.articles.collectAsLazyPagingItems()
    val feedListState = pagingItems.rememberLazyListStatePagingWorkaround()

    PrimalDrawerScaffold(
        drawerState = drawerState,
        activeDestination = PrimalTopLevelDestination.Reads,
        onActiveDestinationClick = { uiScope.launch { feedListState.animateScrollToItem(index = 0) } },
        onPrimaryDestinationChanged = onTopLevelDestinationChanged,
        onDrawerDestinationClick = onDrawerScreenClick,
        onDrawerQrCodeClick = onDrawerQrCodeClick,
        badges = state.badges,
        focusModeEnabled = LocalContentDisplaySettings.current.focusModeEnabled && pagingItems.isNotEmpty(),
        topBar = {
            PrimalTopAppBar(
                title = "My reads",
                titleTrailingIcon = Icons.Default.ExpandMore,
                avatarCdnImage = state.activeAccountAvatarCdnImage,
                navigationIcon = PrimalIcons.AvatarDefault,
                onNavigationIconClick = {
                    uiScope.launch { drawerState.open() }
                },
                actions = {
                    AppBarIcon(
                        icon = PrimalIcons.Search,
                        onClick = onSearchClick,
                        appBarIconContentDescription = stringResource(id = R.string.accessibility_search),
                    )
                },
                scrollBehavior = it,
            )
        },
        content = { paddingValues ->
            FeedArticleList(
                feedListState = feedListState,
                pagingItems = pagingItems,
                contentPadding = paddingValues,
                zappingState = state.zappingState,
                onArticleClick = onArticleClick,
            )
        },
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        },
    )
}

@Composable
private fun FeedArticleList(
    feedListState: LazyListState,
    pagingItems: LazyPagingItems<FeedArticleUi>,
    zappingState: ZappingState,
    contentPadding: PaddingValues = PaddingValues(0.dp),
    onArticleClick: (naddr: String) -> Unit,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = contentPadding,
        state = feedListState,
    ) {
        handleMediatorPrependState(pagingItems)

        items(
            count = pagingItems.itemCount,
            key = pagingItems.itemKey(key = { it.eventId }),
            contentType = pagingItems.itemContentType(),
        ) { index ->
            val item = pagingItems[index]

            when {
                item != null -> Column {
                    FeedArticleListItem(
                        data = item,
                        modifier = Modifier.padding(all = 16.dp),
                        onClick = onArticleClick,
                    )
                    PrimalDivider()
                }

                else -> {}
            }
        }

        if (pagingItems.isEmpty()) {
            handleRefreshLoadState(pagingItems)
        }

        handleMediatorAppendState(pagingItems)

        if (pagingItems.isNotEmpty()) {
            item(contentType = "Footer") {
                Spacer(modifier = Modifier.height(64.dp))
            }
        }
    }
}

private fun LazyListScope.handleRefreshLoadState(pagingItems: LazyPagingItems<FeedArticleUi>) {
    when (val refreshLoadState = pagingItems.loadState.refresh) {
        LoadState.Loading -> {
            item(contentType = "LoadingRefresh") {
                ListLoading(
                    modifier = Modifier.fillParentMaxSize(),
                )
            }
        }

        is LoadState.NotLoading -> {
            item(contentType = "NoContent") {
                ListNoContent(
                    modifier = Modifier.fillParentMaxSize(),
                    noContentText = stringResource(id = R.string.article_feed_no_content),
                    onRefresh = { pagingItems.refresh() },
                )
            }
        }

        is LoadState.Error -> {
            val error = refreshLoadState.error
            Timber.w(error)
            item(contentType = "RefreshError") {
                ListNoContent(
                    modifier = Modifier.fillParentMaxSize(),
                    noContentText = stringResource(id = R.string.feed_error_loading),
                    onRefresh = { pagingItems.refresh() },
                )
            }
        }
    }
}

private fun LazyListScope.handleMediatorAppendState(pagingItems: LazyPagingItems<FeedArticleUi>) {
    when (val appendMediatorLoadState = pagingItems.loadState.mediator?.append) {
        LoadState.Loading -> item(contentType = "LoadingAppend") {
            ListLoading(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp)
                    .padding(vertical = 8.dp),
            )
        }

        is LoadState.Error -> if (BuildConfig.FEATURE_PRIMAL_CRASH_REPORTER) {
            item(contentType = "AppendError") {
                val error = appendMediatorLoadState.error
                Timber.w(error)
                ListLoadingError(
                    text = stringResource(R.string.app_error_loading_next_page) + "\n${error.message}",
                )
            }
        }

        else -> Unit
    }
}

private fun LazyListScope.handleMediatorPrependState(pagingItems: LazyPagingItems<FeedArticleUi>) {
    when (val prependMediatorLoadState = pagingItems.loadState.mediator?.prepend) {
        is LoadState.Error -> {
            item(contentType = "PrependError") {
                val error = prependMediatorLoadState.error
                ListLoadingError(
                    text = stringResource(R.string.app_error_loading_prev_page) + "\n${error.message}",
                )
            }
        }

        else -> Unit
    }
}
