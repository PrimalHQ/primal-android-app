package net.primal.android.articles.feed

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemContentType
import androidx.paging.compose.itemKey
import fr.acinq.lightning.utils.UUID
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.launch
import net.primal.android.BuildConfig
import net.primal.android.R
import net.primal.android.articles.feed.ui.FeedArticleListItem
import net.primal.android.articles.feed.ui.FeedArticleUi
import net.primal.android.core.compose.ListLoading
import net.primal.android.core.compose.ListLoadingError
import net.primal.android.core.compose.ListNoContent
import net.primal.android.core.compose.ListPlaceholderLoading
import net.primal.android.core.compose.PrimalDivider
import net.primal.android.core.compose.foundation.rememberLazyListStatePagingWorkaround
import net.primal.android.core.compose.isEmpty
import net.primal.android.core.compose.isNotEmpty
import net.primal.android.core.compose.pulltorefresh.PrimalIndicator
import timber.log.Timber

@Composable
fun ArticleFeedList(
    feedSpec: String,
    contentPadding: PaddingValues = PaddingValues(0.dp),
    onArticleClick: (naddr: String) -> Unit,
    previewMode: Boolean = false,
    header: @Composable (LazyItemScope.() -> Unit)? = null,
    stickyHeader: @Composable (LazyItemScope.() -> Unit)? = null,
) {
    val viewModel = hiltViewModel<ArticleFeedViewModel, ArticleFeedViewModel.Factory>(
        key = if (!previewMode) feedSpec else UUID.randomUUID().toString(),
        creationCallback = { factory -> factory.create(spec = feedSpec) },
    )
    val uiState = viewModel.state.collectAsState()

    ArticleFeedList(
        state = uiState.value,
        contentPadding = contentPadding,
        onArticleClick = onArticleClick,
        header = header,
        stickyHeader = stickyHeader,
    )
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class, FlowPreview::class)
@Composable
private fun ArticleFeedList(
    state: ArticleFeedContract.UiState,
    contentPadding: PaddingValues = PaddingValues(0.dp),
    onArticleClick: (naddr: String) -> Unit,
    header: @Composable (LazyItemScope.() -> Unit)? = null,
    stickyHeader: @Composable (LazyItemScope.() -> Unit)? = null,
) {
    val uiScope = rememberCoroutineScope()
    val pagingItems = state.articles.collectAsLazyPagingItems()
    val feedListState = pagingItems.rememberLazyListStatePagingWorkaround()

    val pullToRefreshState = rememberPullToRefreshState()
    var pullToRefreshing by remember { mutableStateOf(false) }

    var isMediatorRefreshing by remember { mutableStateOf<Boolean?>(null) }
    LaunchedEffect(pagingItems) {
        snapshotFlow { pagingItems.loadState }
            .mapNotNull { it.mediator }
            .debounce(0.21.seconds)
            .collect { loadState ->
                val isRefreshing = loadState.refresh == LoadState.Loading || loadState.prepend == LoadState.Loading
                if (!isRefreshing) {
                    isMediatorRefreshing = false
                }
            }
    }

    if (isMediatorRefreshing == false && pullToRefreshing) {
        LaunchedEffect(true) {
            uiScope.launch {
                feedListState.scrollToItem(index = 0)
                pullToRefreshing = false
            }
        }
    }

    PullToRefreshBox(
        isRefreshing = pullToRefreshing,
        onRefresh = {
            pagingItems.refresh()
            pullToRefreshing = true
            isMediatorRefreshing = true
        },
        state = pullToRefreshState,
        indicator = {
            PrimalIndicator(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(contentPadding),
                isRefreshing = pullToRefreshing,
                state = pullToRefreshState,
            )
        },
    ) {
        ArticleFeedLazyColumn(
            pagingItems = pagingItems,
            listState = feedListState,
            onArticleClick = onArticleClick,
            contentPadding = contentPadding,
            header = header,
            stickyHeader = stickyHeader,
        )
    }
}

@ExperimentalFoundationApi
@Composable
private fun ArticleFeedLazyColumn(
    pagingItems: LazyPagingItems<FeedArticleUi>,
    listState: LazyListState,
    onArticleClick: (naddr: String) -> Unit,
    contentPadding: PaddingValues = PaddingValues(all = 0.dp),
    header: @Composable (LazyItemScope.() -> Unit)? = null,
    stickyHeader: @Composable (LazyItemScope.() -> Unit)? = null,
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .animateContentSize(),
        state = listState,
        contentPadding = contentPadding,
    ) {
        handleMediatorPrependState(pagingItems)

        if (stickyHeader != null) {
            stickyHeader {
                stickyHeader()
            }
        }

        if (header != null) {
            item {
                header()
            }
        }

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
                ListPlaceholderLoading(
                    modifier = Modifier.fillMaxSize(),
                    itemPadding = PaddingValues(top = 8.dp),
                    lightAnimationResId = R.raw.primal_loader_reads_light_v2,
                    darkAnimationResId = R.raw.primal_loader_reads_dark_v2,
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
