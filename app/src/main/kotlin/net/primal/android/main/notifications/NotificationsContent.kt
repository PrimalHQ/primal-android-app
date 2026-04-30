package net.primal.android.main.notifications

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.paging.LoadState
import androidx.paging.PagingData
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemContentType
import androidx.paging.compose.itemKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import net.primal.android.R
import net.primal.android.core.compose.AppBarPage
import net.primal.android.core.compose.ListLoadingError
import net.primal.android.core.compose.ListNoContent
import net.primal.android.core.compose.PrimalDivider
import net.primal.android.core.compose.PrimalTopLevelAppBar
import net.primal.android.core.compose.heightAdjustableLoadingLazyListPlaceholder
import net.primal.android.core.compose.isEmpty
import net.primal.android.core.compose.isNotEmpty
import net.primal.android.core.compose.runtime.DisposableLifecycleObserverEffect
import net.primal.android.notes.feed.NoteRepostOrQuoteBottomSheet
import net.primal.android.notes.feed.model.FeedPostUi
import net.primal.android.notes.feed.model.asNeventString
import net.primal.android.notes.feed.note.NoteContract
import net.primal.android.notes.feed.note.NoteViewModel
import net.primal.android.notes.feed.note.ui.events.NoteCallbacks
import net.primal.android.notes.feed.zaps.UnableToZapBottomSheet
import net.primal.android.notes.feed.zaps.ZapBottomSheet
import net.primal.android.notifications.list.ui.NotificationListItem
import net.primal.android.notifications.list.ui.NotificationUi
import net.primal.android.premium.legend.domain.LegendaryCustomization
import net.primal.android.user.domain.Badges
import net.primal.domain.links.CdnImage
import net.primal.domain.notifications.NotificationGroup
import net.primal.domain.utils.canZap

@Suppress("LongParameterList")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun NotificationsContent(
    pagerState: PagerState,
    badges: Badges,
    seenNotificationsProvider: (NotificationGroup) -> Flow<PagingData<NotificationUi>>,
    unseenNotificationsProvider: (NotificationGroup) -> Flow<List<List<NotificationUi>>>,
    onNotificationsSeen: (NotificationGroup) -> Unit,
    paddingValues: PaddingValues,
    noteCallbacks: NoteCallbacks,
    onGoToWallet: () -> Unit,
    shouldAnimateScrollToTop: MutableState<Boolean>,
) {
    val noteViewModel = hiltViewModel<NoteViewModel, NoteViewModel.Factory> { it.create() }
    val noteState by noteViewModel.state.collectAsState()

    val currentGroup by remember {
        derivedStateOf {
            NotificationGroup.entries.getOrElse(pagerState.currentPage) { NotificationGroup.ALL }
        }
    }

    LaunchedEffect(currentGroup) {
        onNotificationsSeen(currentGroup)
    }

    DisposableLifecycleObserverEffect(pagerState) {
        when (it) {
            Lifecycle.Event.ON_STOP -> onNotificationsSeen(currentGroup)
            else -> Unit
        }
    }

    HorizontalPager(state = pagerState) { pageIndex ->
        val group = NotificationGroup.entries[pageIndex]
        val isActive by remember(pageIndex) {
            derivedStateOf { pageIndex == pagerState.currentPage }
        }
        NotificationFilterPage(
            group = group,
            isActive = isActive,
            seenNotificationsProvider = seenNotificationsProvider,
            unseenNotificationsProvider = unseenNotificationsProvider,
            badges = badges,
            noteState = noteState,
            noteEventPublisher = noteViewModel::setEvent,
            paddingValues = paddingValues,
            noteCallbacks = noteCallbacks,
            onGoToWallet = onGoToWallet,
            shouldAnimateScrollToTop = shouldAnimateScrollToTop,
        )
    }
}

@Suppress("LongMethod", "LongParameterList")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun NotificationFilterPage(
    group: NotificationGroup,
    isActive: Boolean,
    seenNotificationsProvider: (NotificationGroup) -> Flow<PagingData<NotificationUi>>,
    unseenNotificationsProvider: (NotificationGroup) -> Flow<List<List<NotificationUi>>>,
    badges: Badges,
    noteState: NoteContract.UiState,
    noteEventPublisher: (NoteContract.UiEvent) -> Unit,
    paddingValues: PaddingValues,
    noteCallbacks: NoteCallbacks,
    onGoToWallet: () -> Unit,
    shouldAnimateScrollToTop: MutableState<Boolean>,
) {
    val seenPagingItems = remember(group) {
        seenNotificationsProvider(group)
    }.collectAsLazyPagingItems()

    val unseenNotifications by remember(group) {
        unseenNotificationsProvider(group)
    }.collectAsState(initial = emptyList())

    val listState = rememberLazyListState()
    val uiScope = rememberCoroutineScope()

    var hasUserEverScrolled by remember { mutableStateOf(false) }
    var isAutoScrolling by remember { mutableStateOf(false) }
    var previousUnseenIds by remember { mutableStateOf<List<String>>(emptyList()) }

    LaunchedEffect(listState) {
        snapshotFlow {
            listState.isScrollInProgress to listState.interactionSource.interactions
        }.first { (isScrolling, _) ->
            isScrolling && !isAutoScrolling
        }
        hasUserEverScrolled = true
    }

    LaunchedEffect(unseenNotifications) {
        val currentIds = unseenNotifications.flatten().map { it.notificationId }
        if (currentIds != previousUnseenIds) {
            if (!hasUserEverScrolled) {
                isAutoScrolling = true
                uiScope.launch {
                    listState.animateScrollToItem(0)
                    isAutoScrolling = false
                }
            }
            previousUnseenIds = currentIds
        }
    }

    LaunchedEffect(shouldAnimateScrollToTop.value) {
        if (shouldAnimateScrollToTop.value) {
            uiScope.launch { listState.animateScrollToItem(0) }
        }
    }

    LaunchedEffect(isActive, seenPagingItems, badges) {
        if (isActive && badges.unreadNotificationsCount > 0) {
            seenPagingItems.refresh()
        }
    }

    NotificationsList(
        unseenNotifications = unseenNotifications,
        noteState = noteState,
        seenPagingItems = seenPagingItems,
        listState = listState,
        paddingValues = paddingValues,
        noteCallbacks = noteCallbacks,
        onGoToWallet = onGoToWallet,
        onPostLikeClick = {
            noteEventPublisher(
                NoteContract.UiEvent.PostLikeAction(postId = it.postId, postAuthorId = it.authorId),
            )
        },
        onRepostClick = {
            noteEventPublisher(
                NoteContract.UiEvent.RepostAction(
                    postId = it.postId,
                    postAuthorId = it.authorId,
                    postNostrEvent = it.rawNostrEventJson,
                ),
            )
        },
        onDeleteRepostClick = {
            noteEventPublisher(
                NoteContract.UiEvent.DeleteRepostAction(
                    postId = it.postId,
                    repostId = it.repostId,
                    repostAuthorId = it.repostAuthorId,
                ),
            )
        },
        onZapClick = { postData, amount, description ->
            noteEventPublisher(
                NoteContract.UiEvent.ZapAction(
                    postId = postData.postId,
                    postAuthorId = postData.authorId,
                    zapAmount = amount,
                    zapDescription = description,
                ),
            )
        },
        onPostQuoteClick = {
            noteCallbacks.onNoteQuoteClick?.invoke(it.asNeventString())
        },
        onBookmarkClick = {
            noteEventPublisher(NoteContract.UiEvent.BookmarkAction(noteId = it.postId))
        },
    )
}

@Suppress("LongMethod", "LongParameterList", "CyclomaticComplexMethod")
@ExperimentalMaterial3Api
@Composable
private fun NotificationsList(
    unseenNotifications: List<List<NotificationUi>>,
    noteState: NoteContract.UiState,
    listState: LazyListState,
    seenPagingItems: LazyPagingItems<NotificationUi>,
    paddingValues: PaddingValues,
    noteCallbacks: NoteCallbacks,
    onGoToWallet: () -> Unit,
    onPostLikeClick: (FeedPostUi) -> Unit,
    onRepostClick: (FeedPostUi) -> Unit,
    onDeleteRepostClick: (FeedPostUi) -> Unit,
    onZapClick: (FeedPostUi, ULong?, String?) -> Unit,
    onPostQuoteClick: (FeedPostUi) -> Unit,
    onBookmarkClick: (FeedPostUi) -> Unit,
) {
    var repostQuotePostConfirmation by remember { mutableStateOf<FeedPostUi?>(null) }
    if (repostQuotePostConfirmation != null) {
        repostQuotePostConfirmation?.let { post ->
            NoteRepostOrQuoteBottomSheet(
                isReposted = post.stats.userReposted,
                onDismiss = { repostQuotePostConfirmation = null },
                onRepostClick = { onRepostClick(post) },
                onDeleteRepostClick = { onDeleteRepostClick(post) },
                onPostQuoteClick = { onPostQuoteClick(post) },
            )
        }
    }

    var showCantZapWarning by remember { mutableStateOf(false) }
    if (showCantZapWarning) {
        UnableToZapBottomSheet(
            zappingState = noteState.zappingState,
            onDismissRequest = { showCantZapWarning = false },
            onGoToWallet = onGoToWallet,
        )
    }

    var zapOptionsPostConfirmation by remember { mutableStateOf<FeedPostUi?>(null) }
    if (zapOptionsPostConfirmation != null) {
        zapOptionsPostConfirmation?.let { post ->
            ZapBottomSheet(
                onDismissRequest = { zapOptionsPostConfirmation = null },
                receiverName = post.authorName,
                zappingState = noteState.zappingState,
                onZap = { zapAmount, zapDescription ->
                    if (noteState.zappingState.canZap(zapAmount)) {
                        onZapClick(post, zapAmount.toULong(), zapDescription)
                    } else {
                        showCantZapWarning = true
                    }
                },
            )
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = paddingValues,
        state = listState,
    ) {
        items(
            items = unseenNotifications,
            key = { it.map { it.notificationId } },
            contentType = { it.first().notificationType },
        ) {
            NotificationListItem(
                notifications = it,
                type = it.first().notificationType,
                isSeen = false,
                onReplyClick = noteCallbacks.onNoteClick,
                onPostLikeClick = onPostLikeClick,
                onDefaultZapClick = { postData ->
                    if (noteState.zappingState.canZap()) {
                        onZapClick(postData, null, null)
                    } else {
                        showCantZapWarning = true
                    }
                },
                onZapOptionsClick = { postData ->
                    if (noteState.zappingState.walletConnected) {
                        zapOptionsPostConfirmation = postData
                    } else {
                        showCantZapWarning = true
                    }
                },
                onRepostClick = { postData -> repostQuotePostConfirmation = postData },
                onBookmarkClick = onBookmarkClick,
                noteCallbacks = noteCallbacks,
            )

            if (unseenNotifications.last() != it || seenPagingItems.isNotEmpty()) {
                PrimalDivider()
            }
        }

        items(
            count = seenPagingItems.itemCount,
            key = seenPagingItems.itemKey(key = { it.notificationId }),
            contentType = seenPagingItems.itemContentType { it.notificationType },
        ) {
            val item = seenPagingItems[it]

            when {
                item != null -> {
                    NotificationListItem(
                        notifications = listOf(item),
                        type = item.notificationType,
                        isSeen = true,
                        noteCallbacks = noteCallbacks,
                        onReplyClick = noteCallbacks.onNoteReplyClick,
                        onPostLikeClick = onPostLikeClick,
                        onDefaultZapClick = { postData ->
                            if (noteState.zappingState.canZap()) {
                                onZapClick(postData, null, null)
                            } else {
                                showCantZapWarning = true
                            }
                        },
                        onZapOptionsClick = { postData ->
                            if (noteState.zappingState.walletConnected) {
                                zapOptionsPostConfirmation = postData
                            } else {
                                showCantZapWarning = true
                            }
                        },
                        onRepostClick = { postData -> repostQuotePostConfirmation = postData },
                        onBookmarkClick = onBookmarkClick,
                    )

                    if (it < seenPagingItems.itemCount - 1) {
                        PrimalDivider()
                    }
                }

                else -> {}
            }
        }

        if (seenPagingItems.isEmpty() && unseenNotifications.isEmpty()) {
            when (seenPagingItems.loadState.refresh) {
                LoadState.Loading -> {
                    heightAdjustableLoadingLazyListPlaceholder(height = 98.dp)
                }

                is LoadState.NotLoading -> {
                    item(contentType = "NoContent") {
                        ListNoContent(
                            modifier = Modifier.fillParentMaxSize(),
                            noContentText = stringResource(id = R.string.notifications_no_content),
                            refreshButtonVisible = false,
                        )
                    }
                }

                is LoadState.Error -> {
                    item(contentType = "RefreshError") {
                        ListNoContent(
                            modifier = Modifier.fillParentMaxSize(),
                            noContentText = stringResource(id = R.string.notifications_initial_loading_error),
                            onRefresh = { seenPagingItems.refresh() },
                        )
                    }
                }
            }
        }

        when (seenPagingItems.loadState.mediator?.append) {
            LoadState.Loading ->
                heightAdjustableLoadingLazyListPlaceholder(
                    contentType = { "LoadingAppend" },
                    height = 98.dp,
                    repeat = 1,
                )

            is LoadState.Error -> item(contentType = "AppendError") {
                ListLoadingError(text = stringResource(R.string.app_error_loading_next_page))
            }

            else -> Unit
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun NotificationsTopAppBar(
    avatarCdnImage: CdnImage?,
    avatarLegendaryCustomization: LegendaryCustomization?,
    avatarBlossoms: List<String>,
    scrollBehavior: TopAppBarScrollBehavior?,
    onAvatarClick: () -> Unit,
    onAvatarSwipeDown: (() -> Unit)? = null,
    titleOverride: String? = null,
    subtitleOverride: String? = null,
    pagerState: PagerState? = null,
    pages: List<AppBarPage> = emptyList(),
    showTitleChevron: Boolean = false,
    chevronExpanded: Boolean = false,
    onTitleClick: (() -> Unit)? = null,
) {
    PrimalTopLevelAppBar(
        title = stringResource(id = R.string.notifications_title),
        titleOverride = titleOverride,
        subtitleOverride = subtitleOverride,
        avatarCdnImage = avatarCdnImage,
        avatarBlossoms = avatarBlossoms,
        avatarLegendaryCustomization = avatarLegendaryCustomization,
        onAvatarClick = onAvatarClick,
        onAvatarSwipeDown = onAvatarSwipeDown,
        showDivider = false,
        scrollBehavior = scrollBehavior,
        showTitleChevron = showTitleChevron,
        chevronExpanded = chevronExpanded,
        onTitleClick = onTitleClick,
        pagerState = pagerState,
        pages = pages,
    )
}
