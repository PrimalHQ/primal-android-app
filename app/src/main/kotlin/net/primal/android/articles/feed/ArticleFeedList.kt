package net.primal.android.articles.feed

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
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
import net.primal.android.core.compose.PrimalDivider
import net.primal.android.core.compose.foundation.rememberLazyListStatePagingWorkaround
import net.primal.android.core.compose.heightAdjustableLoadingLazyListPlaceholder
import net.primal.android.core.compose.isEmpty
import net.primal.android.core.compose.isNotEmpty
import net.primal.android.core.compose.pulltorefresh.PrimalPullToRefreshBox
import net.primal.android.core.errors.UiError
import net.primal.android.notes.feed.note.ui.ConfirmFirstBookmarkAlertDialog
import net.primal.android.theme.AppTheme
import net.primal.android.thread.articles.ArticleContract
import net.primal.android.thread.articles.ArticleViewModel
import timber.log.Timber

@Composable
fun ArticleFeedList(
    feedSpec: String,
    onArticleClick: (naddr: String) -> Unit,
    modifier: Modifier = Modifier,
    pullToRefreshEnabled: Boolean = true,
    previewMode: Boolean = false,
    noContentText: String = stringResource(id = R.string.article_feed_no_content),
    noContentVerticalArrangement: Arrangement.Vertical = Arrangement.Center,
    noContentPaddingValues: PaddingValues = PaddingValues(all = 0.dp),
    contentPadding: PaddingValues = PaddingValues(0.dp),
    onUiError: ((UiError) -> Unit)? = null,
    header: @Composable (LazyItemScope.() -> Unit)? = null,
    stickyHeader: @Composable (LazyItemScope.() -> Unit)? = null,
) {
    val viewModelKey by remember { mutableStateOf(if (!previewMode) feedSpec else UUID.randomUUID().toString()) }
    val viewModel = hiltViewModel<ArticleFeedViewModel, ArticleFeedViewModel.Factory>(key = viewModelKey) { factory ->
        factory.create(spec = feedSpec)
    }
    val feedState by viewModel.state.collectAsState()

    val articleViewModel = hiltViewModel<ArticleViewModel>()
    val articleState by articleViewModel.state.collectAsState()
    LaunchedEffect(articleViewModel, articleState.error, onUiError) {
        articleState.error?.let { onUiError?.invoke(it) }
        articleViewModel.setEvent(ArticleContract.UiEvent.DismissError)
    }

    ArticleFeedList(
        feedState = feedState,
        articleState = articleState,
        modifier = modifier,
        contentPadding = contentPadding,
        onArticleClick = onArticleClick,
        header = header,
        stickyHeader = stickyHeader,
        pullToRefreshEnabled = pullToRefreshEnabled,
        noContentText = noContentText,
        noContentVerticalArrangement = noContentVerticalArrangement,
        noContentPaddingValues = noContentPaddingValues,
        articleEventPublisher = articleViewModel::setEvent,
    )
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class, FlowPreview::class)
@Composable
private fun ArticleFeedList(
    feedState: ArticleFeedContract.UiState,
    articleState: ArticleContract.UiState,
    modifier: Modifier = Modifier,
    articleEventPublisher: (ArticleContract.UiEvent) -> Unit,
    pullToRefreshEnabled: Boolean = true,
    contentPadding: PaddingValues = PaddingValues(0.dp),
    noContentText: String = stringResource(id = R.string.article_feed_no_content),
    noContentVerticalArrangement: Arrangement.Vertical = Arrangement.Center,
    noContentPaddingValues: PaddingValues = PaddingValues(all = 0.dp),
    onArticleClick: (naddr: String) -> Unit,
    header: @Composable (LazyItemScope.() -> Unit)? = null,
    stickyHeader: @Composable (LazyItemScope.() -> Unit)? = null,
) {
    val uiScope = rememberCoroutineScope()
    val pagingItems = feedState.articles.collectAsLazyPagingItems()
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

    PrimalPullToRefreshBox(
        isRefreshing = pullToRefreshing,
        onRefresh = {
            pagingItems.refresh()
            pullToRefreshing = true
            isMediatorRefreshing = true
        },
        state = pullToRefreshState,
        enabled = pullToRefreshEnabled,
        indicatorPaddingValues = contentPadding,
    ) {
        ArticleFeedLazyColumn(
            modifier = modifier
                .background(color = AppTheme.colorScheme.surfaceVariant)
                .fillMaxSize(),
            articleState = articleState,
            pagingItems = pagingItems,
            listState = feedListState,
            onArticleClick = onArticleClick,
            articleEventPublisher = articleEventPublisher,
            contentPadding = contentPadding,
            header = header,
            stickyHeader = stickyHeader,
            noContentText = noContentText,
            noContentVerticalArrangement = noContentVerticalArrangement,
            noContentPaddingValues = noContentPaddingValues,
        )
    }
}

@ExperimentalMaterial3Api
@ExperimentalFoundationApi
@Composable
private fun ArticleFeedLazyColumn(
    articleState: ArticleContract.UiState,
    pagingItems: LazyPagingItems<FeedArticleUi>,
    listState: LazyListState,
    modifier: Modifier = Modifier,
    onArticleClick: (naddr: String) -> Unit,
    articleEventPublisher: (ArticleContract.UiEvent) -> Unit,
    noContentText: String = stringResource(id = R.string.article_feed_no_content),
    noContentVerticalArrangement: Arrangement.Vertical = Arrangement.Center,
    noContentPaddingValues: PaddingValues = PaddingValues(all = 0.dp),
    contentPadding: PaddingValues = PaddingValues(all = 0.dp),
    header: @Composable (LazyItemScope.() -> Unit)? = null,
    stickyHeader: @Composable (LazyItemScope.() -> Unit)? = null,
) {
    LazyColumn(
        modifier = modifier.animateContentSize(),
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
                    if (articleState.shouldApproveBookmark) {
                        ConfirmFirstBookmarkAlertDialog(
                            onBookmarkConfirmed = {
                                articleEventPublisher(
                                    ArticleContract.UiEvent.BookmarkAction(
                                        forceUpdate = true,
                                        articleATag = item.aTag,
                                    ),
                                )
                            },
                            onClose = {
                                articleEventPublisher(ArticleContract.UiEvent.DismissBookmarkConfirmation)
                            },
                        )
                    }

                    FeedArticleListItem(
                        data = item,
                        modifier = Modifier.padding(all = 16.dp),
                        onClick = onArticleClick,
                        onBookmarkClick = {
                            articleEventPublisher(ArticleContract.UiEvent.BookmarkAction(articleATag = item.aTag))
                        },
                        onMuteUserClick = {
                            articleEventPublisher(ArticleContract.UiEvent.MuteAction(userId = item.authorId))
                        },
                        onReportContentClick = { reportType ->
                            articleEventPublisher(
                                ArticleContract.UiEvent.ReportAbuse(
                                    reportType = reportType,
                                    authorId = item.authorId,
                                    eventId = item.eventId,
                                    articleId = item.articleId,
                                ),
                            )
                        },
                    )
                    PrimalDivider()
                }

                else -> {}
            }
        }

        if (pagingItems.isEmpty()) {
            handleRefreshLoadState(
                pagingItems = pagingItems,
                noContentText = noContentText,
                noContentVerticalArrangement = noContentVerticalArrangement,
                noContentPaddingValues = noContentPaddingValues,
            )
        }

        handleMediatorAppendState(pagingItems)

        if (pagingItems.isNotEmpty()) {
            item(contentType = "Footer") {
                Spacer(modifier = Modifier.height(64.dp))
            }
        }
    }
}

private fun LazyListScope.handleRefreshLoadState(
    pagingItems: LazyPagingItems<FeedArticleUi>,
    noContentText: String,
    noContentVerticalArrangement: Arrangement.Vertical = Arrangement.Center,
    noContentPaddingValues: PaddingValues = PaddingValues(all = 0.dp),
) {
    when (val refreshLoadState = pagingItems.loadState.refresh) {
        LoadState.Loading -> {
            heightAdjustableLoadingLazyListPlaceholder()
        }

        is LoadState.NotLoading -> {
            item(contentType = "NoContent") {
                ListNoContent(
                    modifier = Modifier.fillParentMaxSize(),
                    noContentText = noContentText,
                    onRefresh = { pagingItems.refresh() },
                    verticalArrangement = noContentVerticalArrangement,
                    contentPadding = noContentPaddingValues,
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
                    verticalArrangement = noContentVerticalArrangement,
                    contentPadding = noContentPaddingValues,
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
