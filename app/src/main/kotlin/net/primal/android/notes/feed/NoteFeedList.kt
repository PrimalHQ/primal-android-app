package net.primal.android.notes.feed

import android.content.Context
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import java.util.*
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import net.primal.android.R
import net.primal.android.core.compose.AvatarThumbnailsRow
import net.primal.android.core.compose.foundation.rememberLazyListStatePagingWorkaround
import net.primal.android.core.compose.isNotEmpty
import net.primal.android.core.compose.pulltorefresh.PrimalIndicator
import net.primal.android.core.compose.runtime.DisposableLifecycleObserverEffect
import net.primal.android.drawer.FloatingNewDataHostTopPadding
import net.primal.android.notes.feed.NoteFeedContract.UiEvent
import net.primal.android.notes.feed.model.FeedPostUi
import net.primal.android.notes.feed.model.FeedPostsSyncStats
import net.primal.android.notes.feed.model.ZappingState
import net.primal.android.notes.feed.note.ConfirmFirstBookmarkAlertDialog
import net.primal.android.notes.feed.note.events.NoteCallbacks
import net.primal.android.profile.report.OnReportContentClick
import net.primal.android.theme.AppTheme

@Composable
fun NoteFeedList(
    feedSpec: String,
    noteCallbacks: NoteCallbacks,
    onGoToWallet: () -> Unit,
    contentPadding: PaddingValues = PaddingValues(0.dp),
    newNotesNoticeAlpha: Float = 1.00f,
    previewMode: Boolean = false,
    isFeedSpecActive: Boolean = true,
    header: @Composable (LazyItemScope.() -> Unit)? = null,
    stickyHeader: @Composable (LazyItemScope.() -> Unit)? = null,
) {
    val viewModel = hiltViewModel<NoteFeedViewModel, NoteFeedViewModel.Factory>(
        key = if (!previewMode) feedSpec else UUID.randomUUID().toString(),
        creationCallback = { factory -> factory.create(feedSpec = feedSpec) },
    )
    val uiState = viewModel.state.collectAsState()

    var started by remember(viewModel) { mutableStateOf(false) }
    DisposableLifecycleObserverEffect(viewModel) {
        when (it) {
            Lifecycle.Event.ON_START -> started = true
            Lifecycle.Event.ON_STOP -> started = false
            else -> Unit
        }
    }

    val isPolling by remember(started, isFeedSpecActive) { mutableStateOf(started && isFeedSpecActive) }
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
        contentPadding = contentPadding,
        header = header,
        stickyHeader = stickyHeader,
        eventPublisher = viewModel::setEvent,
    )
}

@Composable
private fun NoteFeedList(
    state: NoteFeedContract.UiState,
    noteCallbacks: NoteCallbacks,
    onGoToWallet: () -> Unit,
    newNotesNoticeAlpha: Float = 1.00f,
    contentPadding: PaddingValues = PaddingValues(0.dp),
    header: @Composable (LazyItemScope.() -> Unit)? = null,
    stickyHeader: @Composable (LazyItemScope.() -> Unit)? = null,
    eventPublisher: (UiEvent) -> Unit,
) {
    val pagingItems = state.notes.collectAsLazyPagingItems()
    val feedListState = pagingItems.rememberLazyListStatePagingWorkaround()
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    LaunchedEffect(state.error) {
        if (state.error != null) {
            scope.launch {
                Toast.makeText(context, state.error.toErrorMessage(context), Toast.LENGTH_LONG).show()
            }
        }
    }

    LaunchedEffect(feedListState, pagingItems) {
        withContext(Dispatchers.IO) {
            snapshotFlow { feedListState.firstVisibleItemIndex to pagingItems.itemCount }
                .distinctUntilChanged()
                .filter { (_, size) -> size > 0 }
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

    if (state.confirmBookmarkingNoteId != null) {
        ConfirmFirstBookmarkAlertDialog(
            onBookmarkConfirmed = {
                eventPublisher(
                    UiEvent.BookmarkAction(
                        noteId = state.confirmBookmarkingNoteId,
                        forceUpdate = true,
                    ),
                )
            },
            onClose = {
                eventPublisher(UiEvent.DismissBookmarkConfirmation)
            },
        )
    }

    Box {
        NoteFeedList(
            pagingItems = pagingItems,
            feedListState = feedListState,
            zappingState = state.zappingState,
            noteCallbacks = noteCallbacks,
            onZapClick = { post, zapAmount, zapDescription ->
                eventPublisher(
                    UiEvent.ZapAction(
                        postId = post.postId,
                        postAuthorId = post.authorId,
                        zapAmount = zapAmount,
                        zapDescription = zapDescription,
                    ),
                )
            },
            onPostLikeClick = {
                eventPublisher(
                    UiEvent.PostLikeAction(
                        postId = it.postId,
                        postAuthorId = it.authorId,
                    ),
                )
            },
            onRepostClick = {
                eventPublisher(
                    UiEvent.RepostAction(
                        postId = it.postId,
                        postAuthorId = it.authorId,
                        postNostrEvent = it.rawNostrEventJson,
                    ),
                )
            },
            onGoToWallet = onGoToWallet,
            paddingValues = contentPadding,
            onScrolledToTop = { eventPublisher(UiEvent.FeedScrolledToTop) },
            onMuteClick = { eventPublisher(UiEvent.MuteAction(it)) },
            onBookmarkClick = { eventPublisher(UiEvent.BookmarkAction(noteId = it)) },
            onReportContentClick = { type, profileId, noteId ->
                eventPublisher(
                    UiEvent.ReportAbuse(
                        reportType = type,
                        profileId = profileId,
                        noteId = noteId,
                    ),
                )
            },
            header = header,
            stickyHeader = stickyHeader,
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
                var doneDelaying by remember { mutableStateOf(false) }
                LaunchedEffect(true) {
                    delay(0.21.seconds)
                    doneDelaying = true
                }
                if (doneDelaying) {
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

    if (isMediatorRefreshing == false && pullToRefreshing) {
        LaunchedEffect(true) {
            uiScope.launch {
                feedListState.scrollToItem(index = 0)
                onScrolledToTop?.invoke()
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
                    .padding(paddingValues),
                isRefreshing = pullToRefreshing,
                state = pullToRefreshState,
            )
        },
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
    }
}

private fun NoteFeedContract.UiState.FeedError.toErrorMessage(context: Context): String {
    return when (this) {
        is NoteFeedContract.UiState.FeedError.InvalidZapRequest -> context.getString(
            R.string.post_action_invalid_zap_request,
        )

        is NoteFeedContract.UiState.FeedError.MissingLightningAddress -> context.getString(
            R.string.post_action_missing_lightning_address,
        )

        is NoteFeedContract.UiState.FeedError.FailedToPublishZapEvent -> context.getString(
            R.string.post_action_zap_failed,
        )

        is NoteFeedContract.UiState.FeedError.FailedToPublishLikeEvent -> context.getString(
            R.string.post_action_like_failed,
        )

        is NoteFeedContract.UiState.FeedError.FailedToPublishRepostEvent -> context.getString(
            R.string.post_action_repost_failed,
        )

        is NoteFeedContract.UiState.FeedError.MissingRelaysConfiguration -> context.getString(
            R.string.app_missing_relays_config,
        )

        is NoteFeedContract.UiState.FeedError.FailedToMuteUser -> context.getString(
            R.string.app_error_muting_user,
        )
    }
}
