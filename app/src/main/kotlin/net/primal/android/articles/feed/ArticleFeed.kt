package net.primal.android.articles.feed

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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
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
import net.primal.android.BuildConfig
import net.primal.android.R
import net.primal.android.articles.feed.di.ArticleFeedViewModelFactory
import net.primal.android.articles.feed.ui.FeedArticleListItem
import net.primal.android.articles.feed.ui.FeedArticleUi
import net.primal.android.core.compose.ListLoading
import net.primal.android.core.compose.ListLoadingError
import net.primal.android.core.compose.ListNoContent
import net.primal.android.core.compose.PrimalDivider
import net.primal.android.core.compose.foundation.rememberLazyListStatePagingWorkaround
import net.primal.android.core.compose.isEmpty
import net.primal.android.core.compose.isNotEmpty
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
    val viewModel = hiltViewModel<ArticleFeedViewModel, ArticleFeedViewModelFactory>(
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

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ArticleFeedList(
    state: ArticleFeedContract.UiState,
    contentPadding: PaddingValues = PaddingValues(0.dp),
    onArticleClick: (naddr: String) -> Unit,
    header: @Composable (LazyItemScope.() -> Unit)? = null,
    stickyHeader: @Composable (LazyItemScope.() -> Unit)? = null,
) {
    val pagingItems = state.articles.collectAsLazyPagingItems()
    val feedListState = pagingItems.rememberLazyListStatePagingWorkaround()

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = contentPadding,
        state = feedListState,
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