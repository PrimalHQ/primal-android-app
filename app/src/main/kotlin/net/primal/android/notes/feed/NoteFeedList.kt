package net.primal.android.notes.feed

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.pulltorefresh.PullToRefreshContainer
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
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import fr.acinq.lightning.utils.UUID
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import net.primal.android.R
import net.primal.android.core.compose.foundation.rememberLazyListStatePagingWorkaround
import net.primal.android.core.compose.pulltorefresh.PrimalPullToRefreshIndicator
import net.primal.android.notes.feed.model.FeedPostUi
import net.primal.android.notes.feed.model.ZappingState
import net.primal.android.notes.feed.note.events.NoteCallbacks
import net.primal.android.profile.report.OnReportContentClick
import net.primal.android.theme.AppTheme

@Composable
fun NoteFeedList(
    feedSpec: String,
    contentPadding: PaddingValues = PaddingValues(0.dp),
    noteCallbacks: NoteCallbacks,
    onGoToWallet: () -> Unit,
    previewMode: Boolean = false,
    header: @Composable (LazyItemScope.() -> Unit)? = null,
    stickyHeader: @Composable (LazyItemScope.() -> Unit)? = null,
) {
    val viewModel = hiltViewModel<NoteFeedViewModel, NoteFeedViewModel.Factory>(
        key = if (!previewMode) feedSpec else UUID.randomUUID().toString(),
        creationCallback = { factory -> factory.create(feedSpec = feedSpec) },
    )
    val uiState = viewModel.state.collectAsState()

    NoteFeedList(
        state = uiState.value,
        noteCallbacks = noteCallbacks,
        contentPadding = contentPadding,
        header = header,
        stickyHeader = stickyHeader,
        onGoToWallet = onGoToWallet,
    )
}

@Composable
private fun NoteFeedList(
    state: NoteFeedContract.UiState,
    noteCallbacks: NoteCallbacks,
    onGoToWallet: () -> Unit,
    contentPadding: PaddingValues = PaddingValues(0.dp),
    header: @Composable (LazyItemScope.() -> Unit)? = null,
    stickyHeader: @Composable (LazyItemScope.() -> Unit)? = null,
) {
    val pagingItems = state.notes.collectAsLazyPagingItems()
    val feedListState = pagingItems.rememberLazyListStatePagingWorkaround()

    NoteFeedList(
        pagingItems = pagingItems,
        feedListState = feedListState,
        // state.zappingState
        zappingState = ZappingState(),
        noteCallbacks = noteCallbacks,
        onZapClick = { post, zapAmount, zapDescription ->
//            eventPublisher(
//                FeedContract.UiEvent.ZapAction(
//                    postId = post.postId,
//                    postAuthorId = post.authorId,
//                    zapAmount = zapAmount,
//                    zapDescription = zapDescription,
//                ),
//            )
        },
        onPostLikeClick = {
//            eventPublisher(
//                FeedContract.UiEvent.PostLikeAction(
//                    postId = it.postId,
//                    postAuthorId = it.authorId,
//                ),
//            )
        },
        onRepostClick = {
//            eventPublisher(
//                FeedContract.UiEvent.RepostAction(
//                    postId = it.postId,
//                    postAuthorId = it.authorId,
//                    postNostrEvent = it.rawNostrEventJson,
//                ),
//            )
        },
        onGoToWallet = onGoToWallet,
        paddingValues = contentPadding,
        onScrolledToTop = {
//            eventPublisher(FeedContract.UiEvent.FeedScrolledToTop)
        },
        onMuteClick = {
//            eventPublisher(FeedContract.UiEvent.MuteAction(it))
        },
        onBookmarkClick = {
//            eventPublisher(FeedContract.UiEvent.BookmarkAction(noteId = it))
        },
        onReportContentClick = { type, profileId, noteId ->
//            eventPublisher(
//                FeedContract.UiEvent.ReportAbuse(
//                    reportType = type,
//                    profileId = profileId,
//                    noteId = noteId,
//                ),
//            )
        },
        header = header,
        stickyHeader = stickyHeader,
    )
}

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class, FlowPreview::class)
@Composable
fun NoteFeedList(
    feedListState: LazyListState,
    pagingItems: LazyPagingItems<FeedPostUi>,
    zappingState: ZappingState,
    noteCallbacks: NoteCallbacks,
    onPostLikeClick: (FeedPostUi) -> Unit,
    onZapClick: (FeedPostUi, ULong?, String?) -> Unit,
    onRepostClick: (FeedPostUi) -> Unit,
    onGoToWallet: () -> Unit,
    onBookmarkClick: (noteId: String) -> Unit,
    paddingValues: PaddingValues = PaddingValues(0.dp),
    onScrolledToTop: (() -> Unit)? = null,
    onMuteClick: ((String) -> Unit)? = null,
    onReportContentClick: OnReportContentClick,
    noContentText: String = stringResource(id = R.string.feed_no_content),
    header: @Composable (LazyItemScope.() -> Unit)? = null,
    stickyHeader: @Composable (LazyItemScope.() -> Unit)? = null,
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

    val uiScope = rememberCoroutineScope()
    val pullToRefreshState = rememberPullToRefreshState()
    var pullToRefreshing by remember { mutableStateOf(false) }

    if (pullToRefreshState.isRefreshing) {
        LaunchedEffect(true) {
            pagingItems.refresh()
            pullToRefreshing = true
            isMediatorRefreshing = true
        }
    }

    if (isMediatorRefreshing == false && pullToRefreshing) {
        LaunchedEffect(true) {
            uiScope.launch {
                feedListState.scrollToItem(index = 0)
                onScrolledToTop?.invoke()
                pullToRefreshState.endRefresh()
                pullToRefreshing = false
            }
        }
    }

    Box(
        modifier = Modifier.nestedScroll(pullToRefreshState.nestedScrollConnection),
    ) {
        NoteFeedLazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = paddingValues,
            pagingItems = pagingItems,
            listState = feedListState,
            zappingState = zappingState,
            noteCallbacks = noteCallbacks,
            onPostLikeClick = onPostLikeClick,
            onZapClick = onZapClick,
            onRepostClick = onRepostClick,
            onGoToWallet = onGoToWallet,
            onMuteClick = onMuteClick,
            onReportContentClick = onReportContentClick,
            onBookmarkClick = onBookmarkClick,
            noContentText = noContentText,
            header = header,
            stickyHeader = stickyHeader,
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
