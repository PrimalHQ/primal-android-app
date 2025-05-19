package net.primal.android.notes.feed.list

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import java.util.*
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import net.primal.android.R
import net.primal.android.core.compose.AvatarThumbnailsRow
import net.primal.android.core.compose.foundation.rememberLazyListStatePagingWorkaround
import net.primal.android.core.compose.isNotEmpty
import net.primal.android.core.compose.pulltorefresh.PrimalPullToRefreshBox
import net.primal.android.core.compose.runtime.DisposableLifecycleObserverEffect
import net.primal.android.core.errors.UiError
import net.primal.android.drawer.FloatingNewDataHostTopPadding
import net.primal.android.notes.feed.list.NoteFeedContract.UiEvent
import net.primal.android.notes.feed.model.FeedPostUi
import net.primal.android.notes.feed.model.FeedPostsSyncStats
import net.primal.android.notes.feed.note.ui.events.NoteCallbacks
import net.primal.android.theme.AppTheme

@Composable
fun NoteFeedList(
    feedSpec: String,
    noteCallbacks: NoteCallbacks,
    onGoToWallet: () -> Unit,
    contentPadding: PaddingValues = PaddingValues(0.dp),
    newNotesNoticeAlpha: Float = 1.00f,
    allowMutedThreads: Boolean = false,
    showTopZaps: Boolean = false,
    previewMode: Boolean = false,
    pullToRefreshEnabled: Boolean = true,
    pollingEnabled: Boolean = true,
    noContentText: String = stringResource(id = R.string.feed_no_content),
    noContentVerticalArrangement: Arrangement.Vertical = Arrangement.Center,
    noContentPaddingValues: PaddingValues = PaddingValues(all = 0.dp),
    shouldAnimateScrollToTop: Boolean = false,
    onUiError: ((UiError) -> Unit)? = null,
    header: @Composable (LazyItemScope.() -> Unit)? = null,
    stickyHeader: @Composable (LazyItemScope.() -> Unit)? = null,
) {
    val viewModelKey = remember(previewMode, feedSpec) {
        if (!previewMode) {
            "NoteFeedViewModel_$feedSpec"
        } else {
            UUID.randomUUID().toString()
        }
    }

    val viewModel = hiltViewModel<NoteFeedViewModel, NoteFeedViewModel.Factory>(key = viewModelKey) { factory ->
        factory.create(feedSpec = feedSpec, allowMutedThreads = allowMutedThreads)
    }
    val uiState = viewModel.state.collectAsState()

    var started by remember(viewModel) { mutableStateOf(false) }
    DisposableLifecycleObserverEffect(viewModel) {
        when (it) {
            Lifecycle.Event.ON_START -> started = true
            Lifecycle.Event.ON_STOP -> started = false
            else -> Unit
        }
    }

    val isPolling by remember(started, pollingEnabled) { mutableStateOf(started && pollingEnabled) }
    LaunchedEffect(isPolling) {
        if (isPolling) {
            viewModel.setEvent(UiEvent.StartPolling)
        } else {
            viewModel.setEvent(UiEvent.StopPolling)
        }
    }

    NoteFeedList(
        state = uiState.value,
        noteCallbacks = noteCallbacks,
        onGoToWallet = onGoToWallet,
        newNotesNoticeAlpha = newNotesNoticeAlpha,
        showTopZaps = showTopZaps,
        contentPadding = contentPadding,
        onUiError = onUiError,
        header = header,
        stickyHeader = stickyHeader,
        eventPublisher = viewModel::setEvent,
        pullToRefreshEnabled = pullToRefreshEnabled,
        noContentText = noContentText,
        noContentVerticalArrangement = noContentVerticalArrangement,
        noContentPaddingValues = noContentPaddingValues,
        shouldAnimateScrollToTop = shouldAnimateScrollToTop,
    )
}

@Composable
private fun NoteFeedList(
    state: NoteFeedContract.UiState,
    noteCallbacks: NoteCallbacks,
    onGoToWallet: () -> Unit,
    newNotesNoticeAlpha: Float = 1.00f,
    showTopZaps: Boolean = false,
    pullToRefreshEnabled: Boolean = true,
    contentPadding: PaddingValues = PaddingValues(0.dp),
    onUiError: ((UiError) -> Unit)? = null,
    noContentText: String = stringResource(id = R.string.feed_no_content),
    noContentVerticalArrangement: Arrangement.Vertical = Arrangement.Center,
    noContentPaddingValues: PaddingValues = PaddingValues(all = 0.dp),
    shouldAnimateScrollToTop: Boolean = false,
    header: @Composable (LazyItemScope.() -> Unit)? = null,
    stickyHeader: @Composable (LazyItemScope.() -> Unit)? = null,
    eventPublisher: (UiEvent) -> Unit,
) {
    val pagingItems = state.notes.collectAsLazyPagingItems()
    val listState = pagingItems.rememberLazyListStatePagingWorkaround()

    LaunchedEffect(shouldAnimateScrollToTop, state.shouldAnimateScrollToTop) {
        if (shouldAnimateScrollToTop || state.shouldAnimateScrollToTop == true) {
            snapshotFlow {
                pagingItems.itemCount > 0 && listState.layoutInfo.totalItemsCount > 0
            }
                .filter { it }
                .first()

            listState.animateScrollToItem(index = 0)
        }
    }

    LaunchedEffect(listState, pagingItems) {
        withContext(Dispatchers.IO) {
            snapshotFlow { listState.firstVisibleItemIndex to pagingItems.itemCount }
                .distinctUntilChanged()
                .filter { (index, size) -> size > 0 && index < size }
                .collect { (index, _) ->
                    val firstVisibleNote = pagingItems.peek(index)
                    if (firstVisibleNote != null) {
                        eventPublisher(
                            UiEvent.UpdateCurrentTopVisibleNote(
                                noteId = firstVisibleNote.postId,
                                repostId = firstVisibleNote.repostId,
                            ),
                        )
                    }
                }
        }
    }

    Box {
        NoteFeedList(
            pagingItems = pagingItems,
            pullToRefreshEnabled = pullToRefreshEnabled,
            feedListState = listState,
            showPaywall = state.paywall,
            showTopZaps = showTopZaps,
            noteCallbacks = noteCallbacks,
            onGoToWallet = onGoToWallet,
            paddingValues = contentPadding,
            onScrolledToTop = { eventPublisher(UiEvent.FeedScrolledToTop) },
            onUiError = onUiError,
            header = header,
            stickyHeader = stickyHeader,
            noContentText = noContentText,
            noContentVerticalArrangement = noContentVerticalArrangement,
            noContentPaddingValues = noContentPaddingValues,
        )

        AnimatedVisibility(
            visible = newNotesNoticeAlpha >= 0.5f,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier
                .padding(contentPadding)
                .padding(top = FloatingNewDataHostTopPadding)
                .wrapContentHeight()
                .wrapContentWidth()
                .align(Alignment.TopCenter)
                .graphicsLayer { this.alpha = newNotesNoticeAlpha },
        ) {
            if (state.syncStats.latestNoteIds.isNotEmpty() && pagingItems.isNotEmpty()) {
                var buttonVisible by remember { mutableStateOf(false) }
                LaunchedEffect(true) {
                    delay(10.milliseconds)
                    buttonVisible = true
                }
                if (buttonVisible) {
                    NewPostsButton(
                        syncStats = state.syncStats,
                        onClick = {
                            eventPublisher(UiEvent.ShowLatestNotes)
                        },
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class, FlowPreview::class)
@Composable
fun NoteFeedList(
    feedListState: LazyListState,
    pagingItems: LazyPagingItems<FeedPostUi>,
    showPaywall: Boolean,
    noteCallbacks: NoteCallbacks,
    onGoToWallet: () -> Unit,
    showTopZaps: Boolean = false,
    pullToRefreshEnabled: Boolean = true,
    paddingValues: PaddingValues = PaddingValues(0.dp),
    noContentVerticalArrangement: Arrangement.Vertical = Arrangement.Center,
    noContentPaddingValues: PaddingValues = PaddingValues(all = 0.dp),
    onScrolledToTop: (() -> Unit)? = null,
    onUiError: ((UiError) -> Unit)? = null,
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

    if (isMediatorRefreshing == false && pullToRefreshing) {
        LaunchedEffect(true) {
            uiScope.launch {
                feedListState.scrollToItem(index = 0)
                onScrolledToTop?.invoke()
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
        enabled = pullToRefreshEnabled,
        state = pullToRefreshState,
        indicatorPaddingValues = paddingValues,
    ) {
        NoteFeedLazyColumn(
            modifier = Modifier
                .background(color = AppTheme.colorScheme.surfaceVariant)
                .fillMaxSize(),
            contentPadding = paddingValues,
            pagingItems = pagingItems,
            listState = feedListState,
            showPaywall = showPaywall,
            noteCallbacks = noteCallbacks,
            onGoToWallet = onGoToWallet,
            showTopZaps = showTopZaps,
            noContentText = noContentText,
            header = header,
            stickyHeader = stickyHeader,
            onUiError = onUiError,
            noContentVerticalArrangement = noContentVerticalArrangement,
            noContentPaddingValues = noContentPaddingValues,
        )
    }
}

@Composable
private fun NewPostsButton(syncStats: FeedPostsSyncStats, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .height(40.dp)
            .background(
                color = AppTheme.colorScheme.primary,
                shape = AppTheme.shapes.extraLarge,
            )
            .padding(horizontal = 2.dp)
            .clickable { onClick() },
        verticalAlignment = Alignment.CenterVertically,
    ) {
        AvatarThumbnailsRow(
            modifier = Modifier.padding(start = 6.dp),
            avatarCdnImages = syncStats.latestAvatarCdnImages,
            onClick = { onClick() },
        )

        Text(
            modifier = Modifier
                .padding(start = 12.dp, end = 16.dp)
                .wrapContentHeight(),
            text = stringResource(id = R.string.feed_new_posts_notice_general),
            style = AppTheme.typography.bodySmall,
            color = Color.White,
        )
    }
}
