package net.primal.android.notes.feed.list

import androidx.annotation.PluralsRes
import androidx.annotation.StringRes
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
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
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import net.primal.android.R
import net.primal.android.core.compose.AvatarThumbnailsRow
import net.primal.android.core.compose.foundation.rememberLazyListStatePagingWorkaround
import net.primal.android.core.compose.isNotEmpty
import net.primal.android.core.compose.pulltorefresh.PrimalPullToRefreshBox
import net.primal.android.core.compose.rememberIsItemVisible
import net.primal.android.core.compose.runtime.DisposableLifecycleObserverEffect
import net.primal.android.core.di.rememberMediaCacher
import net.primal.android.core.errors.UiError
import net.primal.android.drawer.FloatingNewDataHostTopPadding
import net.primal.android.events.ui.findNearestOrNull
import net.primal.android.notes.feed.list.NoteFeedContract.UiEvent
import net.primal.android.notes.feed.model.FeedPostUi
import net.primal.android.notes.feed.model.FeedPostsSyncStats
import net.primal.android.notes.feed.model.StreamPillUi
import net.primal.android.notes.feed.model.StreamsSyncStats
import net.primal.android.notes.feed.note.ui.attachment.MaxDisplayImages
import net.primal.android.notes.feed.note.ui.events.NoteCallbacks
import net.primal.android.theme.AppTheme
import net.primal.core.caching.MediaCacher
import net.primal.domain.links.CdnImage
import net.primal.domain.links.EventUriType

@Composable
fun NoteFeedList(
    feedSpec: String,
    noteCallbacks: NoteCallbacks,
    onGoToWallet: () -> Unit,
    contentPadding: PaddingValues = PaddingValues(0.dp),
    newNotesNoticeAlpha: Float = 1.00f,
    allowMutedThreads: Boolean = false,
    showTopZaps: Boolean = false,
    bigPillStreams: List<StreamPillUi> = emptyList(),
    showStreamsInNewPill: Boolean = false,
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
        factory.create(feedSpec = feedSpec, allowMutedThreads = allowMutedThreads, showStreams = showStreamsInNewPill)
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
        bigPillStreams = bigPillStreams,
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
    bigPillStreams: List<StreamPillUi> = emptyList(),
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
    val isStreamPillsRowVisible = listState.rememberIsItemVisible(key = STREAM_PILLS_ROW_KEY, fallback = false)
    val mediaCacher = rememberMediaCacher()

    ScrollToTopHandler(
        shouldAnimateScrollToTop = shouldAnimateScrollToTop,
        stateShouldAnimate = state.shouldAnimateScrollToTop,
        pagingItems = pagingItems,
        listState = listState,
    )

    TopVisibleNoteTracker(
        listState = listState,
        pagingItems = pagingItems,
        eventPublisher = eventPublisher,
    )

    BoxWithConstraints {
        val feedWidthPx = with(LocalDensity.current) { constraints.maxWidth }

        FeedMediaUrlPreLoader(
            pagingItems = pagingItems,
            listState = listState,
            mediaCacher = mediaCacher,
            feedWidthPx = feedWidthPx,
        )

        NoteFeedList(
            pagingItems = pagingItems,
            streamPills = bigPillStreams,
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
            if (state.showSyncStats && pagingItems.isNotEmpty()) {
                var buttonVisible by remember { mutableStateOf(false) }
                LaunchedEffect(true) {
                    delay(10.milliseconds)
                    buttonVisible = true
                }
                if (buttonVisible && (!isStreamPillsRowVisible.value || bigPillStreams.isEmpty())) {
                    NewPostsButton(
                        streamsSyncStats = state.streamsSyncStats,
                        notesSyncStats = state.notesSyncStats,
                        onClick = { eventPublisher(UiEvent.ShowLatestNotes) },
                    )
                }
            }
        }
    }
}

@Composable
private fun ScrollToTopHandler(
    shouldAnimateScrollToTop: Boolean,
    stateShouldAnimate: Boolean?,
    pagingItems: LazyPagingItems<FeedPostUi>,
    listState: LazyListState,
) {
    LaunchedEffect(shouldAnimateScrollToTop, stateShouldAnimate) {
        if (shouldAnimateScrollToTop || stateShouldAnimate == true) {
            snapshotFlow {
                pagingItems.itemCount > 0 && listState.layoutInfo.totalItemsCount > 0
            }.filter { it }.first()

            listState.animateScrollToItem(index = 0)
        }
    }
}

@Composable
private fun TopVisibleNoteTracker(
    listState: LazyListState,
    pagingItems: LazyPagingItems<FeedPostUi>,
    eventPublisher: (UiEvent) -> Unit,
) {
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
}

@OptIn(FlowPreview::class)
@Composable
fun NoteFeedList(
    feedListState: LazyListState,
    pagingItems: LazyPagingItems<FeedPostUi>,
    streamPills: List<StreamPillUi>,
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
                .testTag("noteFeedLazyColumn")
                .background(color = AppTheme.colorScheme.surfaceVariant)
                .fillMaxSize(),
            contentPadding = paddingValues,
            pagingItems = pagingItems,
            streamPills = streamPills,
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
private fun NewPostsButton(
    streamsSyncStats: StreamsSyncStats,
    notesSyncStats: FeedPostsSyncStats,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .height(40.dp)
            .background(
                color = AppTheme.colorScheme.primary,
                shape = AppTheme.shapes.extraLarge,
            )
            .padding(horizontal = 2.dp)
            .padding(start = 4.dp, end = 12.dp)
            .clickable { onClick() },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        when {
            notesSyncStats.latestNotesCount > 1 && streamsSyncStats.streamsCount == 1 -> {
                NewPillPluralIndicator(
                    id = R.plurals.feed_new_posts_notice,
                    count = notesSyncStats.latestNotesCount,
                    avatars = notesSyncStats.latestAvatarCdnImages,
                )

                VerticalDivider(
                    modifier = Modifier.padding(vertical = 8.dp),
                    color = Color.White.copy(alpha = 0.5f),
                )

                NewPillStringIndicator(
                    id = R.string.feed_new_stream,
                    count = 1,
                    avatars = streamsSyncStats.streamAvatarCdnImages,
                )
            }

            notesSyncStats.latestNotesCount > 0 && streamsSyncStats.streamsCount == 0 -> {
                NewPillPluralIndicator(
                    id = R.plurals.feed_new_posts_notice_extended,
                    count = notesSyncStats.latestNotesCount,
                    avatars = notesSyncStats.latestAvatarCdnImages,
                )
            }

            notesSyncStats.latestNotesCount == 0 && streamsSyncStats.streamsCount > 0 -> {
                NewPillPluralIndicator(
                    id = R.plurals.feed_new_lives_notice,
                    count = streamsSyncStats.streamsCount,
                    avatars = streamsSyncStats.streamAvatarCdnImages,
                )
            }

            else -> {
                NewPillPluralIndicator(
                    id = R.plurals.feed_new_posts_notice,
                    count = notesSyncStats.latestNotesCount,
                    avatars = notesSyncStats.latestAvatarCdnImages,
                )

                VerticalDivider(
                    modifier = Modifier.padding(vertical = 8.dp),
                    color = Color.White.copy(alpha = 0.5f),
                )

                NewPillPluralIndicator(
                    id = R.plurals.feed_new_lives_notice,
                    count = streamsSyncStats.streamsCount,
                    avatars = streamsSyncStats.streamAvatarCdnImages,
                )
            }
        }
    }
}

private const val MAX_NOTES_IN_PILL = 20

@Composable
private fun NewPillPluralIndicator(
    modifier: Modifier = Modifier,
    @PluralsRes id: Int,
    count: Int,
    avatars: List<CdnImage?>,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        AvatarThumbnailsRow(avatarCdnImages = avatars)

        Text(
            modifier = Modifier.wrapContentHeight(),
            text = buildAnnotatedString {
                withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                    append(count.coerceAtMost(MAX_NOTES_IN_PILL).toString())
                    if (count > MAX_NOTES_IN_PILL) {
                        append("+")
                    }
                    append(" ")
                }
                append(pluralStringResource(id = id, count = count))
            },
            style = AppTheme.typography.bodySmall,
            color = Color.White,
        )
    }
}

@Composable
private fun NewPillStringIndicator(
    modifier: Modifier = Modifier,
    @StringRes id: Int,
    count: Int,
    avatars: List<CdnImage?>,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        AvatarThumbnailsRow(avatarCdnImages = avatars)

        Text(
            modifier = Modifier.wrapContentHeight(),
            text = buildAnnotatedString {
                withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                    append(count.toString())
                    append(" ")
                }
                append(stringResource(id = id))
            },
            style = AppTheme.typography.bodySmall,
            color = Color.White,
        )
    }
}

private const val INITIAL_PRELOAD_COUNT = 10
private const val SCROLL_PRELOAD_COUNT = 5

@OptIn(FlowPreview::class)
@Composable
private fun FeedMediaUrlPreLoader(
    pagingItems: LazyPagingItems<FeedPostUi>,
    listState: LazyListState,
    mediaCacher: MediaCacher,
    feedWidthPx: Int,
    preloadCount: Int = INITIAL_PRELOAD_COUNT,
) {
    val currentMediaCacher by rememberUpdatedState(mediaCacher)

    LaunchedEffect(pagingItems) {
        snapshotFlow { pagingItems.itemCount > 0 }
            .distinctUntilChanged()
            .filter { it }
            .take(1)
            .collect {
                withContext(Dispatchers.Default) {
                    val countToPreload = minOf(preloadCount, pagingItems.itemCount)
                    val initialItems = (0 until countToPreload).mapNotNull { pagingItems.peek(it) }
                    val urls = initialItems.flatMap { it.extractMediaUrls(feedWidthPx) }

                    if (urls.isNotEmpty()) {
                        currentMediaCacher.preCacheFeedMedia(urls)
                    }
                }
            }
    }

    LaunchedEffect(listState, pagingItems) {
        snapshotFlow { listState.firstVisibleItemIndex / SCROLL_PRELOAD_COUNT }
            .distinctUntilChanged()
            .debounce(150.milliseconds)
            .collect { firstVisible ->
                val itemCount = pagingItems.itemCount
                if (itemCount == 0) return@collect

                val start = (firstVisible + 1).coerceAtMost(itemCount - 1)
                val end = (firstVisible + 1 + SCROLL_PRELOAD_COUNT).coerceAtMost(itemCount)

                if (start < end) {
                    withContext(Dispatchers.Default) {
                        val upcomingItems = (start until end).mapNotNull { pagingItems.peek(it) }
                        val urls = upcomingItems.flatMap { it.extractMediaUrls(feedWidthPx) }

                        if (urls.isNotEmpty()) {
                            currentMediaCacher.preCacheFeedMedia(urls)
                        }
                    }
                }
            }
    }
}

private fun FeedPostUi.extractMediaUrls(feedWidthPx: Int): List<String> {
    val directNoteMediaUrls = this.uris
        .filter { it.type == EventUriType.Image }
        .take(MaxDisplayImages)
        .map { eventUri ->
            val cdnUrl = eventUri.variants.findNearestOrNull(maxWidthPx = feedWidthPx)?.mediaUrl
            cdnUrl ?: eventUri.url
        }

    val directNoteThumbnailUrls = this.uris.mapNotNull { it.thumbnailUrl }

    val referencedNoteImageUrls = this.nostrUris.map {
        val refAttachments = it.referencedNote?.attachments
        val thumbnails = refAttachments
            ?.mapNotNull { link -> link.thumbnail }
            ?: emptyList()

        val media = refAttachments
            ?.filter { link -> link.type == EventUriType.Image }
            ?.take(MaxDisplayImages)
            ?.map { link ->
                val cdnUrl = link.variants.findNearestOrNull(maxWidthPx = feedWidthPx)?.mediaUrl
                cdnUrl ?: link.url
            }
            ?: emptyList()

        thumbnails + media
    }.flatten()

    val referencedArticleImageUrls = this.nostrUris
        .mapNotNull { it.referencedArticle?.articleImageCdnImage }
        .map { cdnImage ->
            val cdnUrl = cdnImage.variants.findNearestOrNull(maxWidthPx = feedWidthPx)?.mediaUrl
            cdnUrl ?: cdnImage.sourceUrl
        }

    return directNoteMediaUrls + directNoteThumbnailUrls + referencedNoteImageUrls + referencedArticleImageUrls
}
