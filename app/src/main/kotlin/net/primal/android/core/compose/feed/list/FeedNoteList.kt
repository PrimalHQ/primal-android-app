package net.primal.android.core.compose.feed.list

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.pulltorefresh.PullToRefreshContainer
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.paging.compose.LazyPagingItems
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import net.primal.android.R
import net.primal.android.core.compose.feed.model.FeedPostUi
import net.primal.android.core.compose.feed.model.ZappingState
import net.primal.android.core.compose.feed.note.events.InvoicePayClickEvent
import net.primal.android.core.compose.feed.note.events.MediaClickEvent
import net.primal.android.core.compose.pulltorefresh.LaunchedPullToRefreshEndingEffect
import net.primal.android.core.compose.pulltorefresh.PrimalPullToRefreshIndicator
import net.primal.android.profile.report.OnReportContentClick
import net.primal.android.theme.AppTheme

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun FeedNoteList(
    feedListState: LazyListState,
    pagingItems: LazyPagingItems<FeedPostUi>,
    zappingState: ZappingState,
    onPostClick: (String) -> Unit,
    onProfileClick: (String) -> Unit,
    onPostLikeClick: (FeedPostUi) -> Unit,
    onZapClick: (FeedPostUi, ULong?, String?) -> Unit,
    onRepostClick: (FeedPostUi) -> Unit,
    onPostReplyClick: (String) -> Unit,
    onPostQuoteClick: (FeedPostUi) -> Unit,
    onHashtagClick: (String) -> Unit,
    onMediaClick: (MediaClickEvent) -> Unit,
    onPayInvoiceClick: ((InvoicePayClickEvent) -> Unit)? = null,
    onGoToWallet: () -> Unit,
    onBookmarkClick: (noteId: String) -> Unit,
    paddingValues: PaddingValues = PaddingValues(0.dp),
    onScrolledToTop: (() -> Unit)? = null,
    onMuteClick: ((String) -> Unit)? = null,
    onReportContentClick: OnReportContentClick,
    noContentText: String = stringResource(id = R.string.feed_no_content),
) {
    LaunchedEffect(feedListState, onScrolledToTop) {
        withContext(Dispatchers.IO) {
            snapshotFlow { feedListState.firstVisibleItemIndex == 0 }
                .distinctUntilChanged()
                .filter { it }
                .collect {
                    onScrolledToTop?.invoke()
                }
        }
    }

    val uiScope = rememberCoroutineScope()
    val pullToRefreshState = rememberPullToRefreshState()

    var pullToRefreshing by remember { mutableStateOf(false) }

    LaunchedEffect(pullToRefreshState.isRefreshing) {
        if (pullToRefreshState.isRefreshing) {
            pagingItems.refresh()
            pullToRefreshing = true
        }
    }

    if (pullToRefreshing) {
        LaunchedPullToRefreshEndingEffect(
            mediatorLoadStates = pagingItems.loadState.mediator,
            onRefreshEnd = {
                pullToRefreshState.endRefresh()
                uiScope.launch {
                    feedListState.scrollToItem(index = 0)
                    onScrolledToTop?.invoke()
                }
            },
        )
    }

    Box(
        modifier = Modifier.nestedScroll(pullToRefreshState.nestedScrollConnection),
    ) {
        FeedLazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = paddingValues,
            pagingItems = pagingItems,
            listState = feedListState,
            zappingState = zappingState,
            onPostClick = onPostClick,
            onProfileClick = onProfileClick,
            onPostLikeClick = onPostLikeClick,
            onZapClick = onZapClick,
            onRepostClick = onRepostClick,
            onPostReplyClick = onPostReplyClick,
            onPostQuoteClick = onPostQuoteClick,
            onHashtagClick = onHashtagClick,
            onMediaClick = onMediaClick,
            onPayInvoiceClick = onPayInvoiceClick,
            onGoToWallet = onGoToWallet,
            onMuteClick = onMuteClick,
            onReportContentClick = onReportContentClick,
            onBookmarkClick = onBookmarkClick,
            noContentText = noContentText,
        )

        PullToRefreshContainer(
            modifier = Modifier
                .padding(paddingValues)
                .align(Alignment.TopCenter),
            state = pullToRefreshState,
            contentColor = AppTheme.colorScheme.primary,
            indicator = { PrimalPullToRefreshIndicator(state = pullToRefreshState) },
        )
    }
}
