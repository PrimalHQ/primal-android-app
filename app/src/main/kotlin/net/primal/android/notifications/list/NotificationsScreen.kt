package net.primal.android.notifications.list

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.DrawerState
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemContentType
import androidx.paging.compose.itemKey
import kotlinx.coroutines.launch
import net.primal.android.LocalContentDisplaySettings
import net.primal.android.R
import net.primal.android.core.compose.AppBarIcon
import net.primal.android.core.compose.ListLoadingError
import net.primal.android.core.compose.ListNoContent
import net.primal.android.core.compose.PrimalDivider
import net.primal.android.core.compose.PrimalTopAppBar
import net.primal.android.core.compose.PrimalTopLevelDestination
import net.primal.android.core.compose.fab.NewPostFloatingActionButton
import net.primal.android.core.compose.foundation.rememberLazyListStatePagingWorkaround
import net.primal.android.core.compose.heightAdjustableLoadingLazyListPlaceholder
import net.primal.android.core.compose.icons.PrimalIcons
import net.primal.android.core.compose.icons.primaliconpack.AvatarDefault
import net.primal.android.core.compose.icons.primaliconpack.Search
import net.primal.android.core.compose.isEmpty
import net.primal.android.core.compose.isNotEmpty
import net.primal.android.core.compose.runtime.DisposableLifecycleObserverEffect
import net.primal.android.drawer.DrawerScreenDestination
import net.primal.android.drawer.PrimalDrawerScaffold
import net.primal.android.drawer.multiaccount.events.AccountSwitcherCallbacks
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
import net.primal.android.theme.AppTheme
import net.primal.android.wallet.zaps.canZap

@Composable
fun NotificationsScreen(
    viewModel: NotificationsViewModel,
    onSearchClick: () -> Unit,
    onGoToWallet: () -> Unit,
    noteCallbacks: NoteCallbacks,
    onTopLevelDestinationChanged: (PrimalTopLevelDestination) -> Unit,
    onDrawerScreenClick: (DrawerScreenDestination) -> Unit,
    onDrawerQrCodeClick: () -> Unit,
    accountSwitcherCallbacks: AccountSwitcherCallbacks,
    onNewPostClick: () -> Unit,
) {
    val noteViewModel = hiltViewModel<NoteViewModel, NoteViewModel.Factory> { it.create() }

    val uiState = viewModel.state.collectAsState()
    val noteState = noteViewModel.state.collectAsState()

    DisposableLifecycleObserverEffect(viewModel) {
        when (it) {
            Lifecycle.Event.ON_STOP -> viewModel.setEvent(
                NotificationsContract.UiEvent.NotificationsSeen,
            )

            else -> Unit
        }
    }

    NotificationsScreen(
        state = uiState.value,
        noteState = noteState.value,
        onSearchClick = onSearchClick,
        onPrimaryDestinationChanged = onTopLevelDestinationChanged,
        onDrawerDestinationClick = onDrawerScreenClick,
        onDrawerQrCodeClick = onDrawerQrCodeClick,
        onGoToWallet = onGoToWallet,
        noteCallbacks = noteCallbacks,
        noteEventPublisher = noteViewModel::setEvent,
        accountSwitcherCallbacks = accountSwitcherCallbacks,
        onNewPostClick = onNewPostClick,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationsScreen(
    state: NotificationsContract.UiState,
    noteState: NoteContract.UiState,
    noteCallbacks: NoteCallbacks,
    onGoToWallet: () -> Unit,
    onSearchClick: () -> Unit,
    onPrimaryDestinationChanged: (PrimalTopLevelDestination) -> Unit,
    onDrawerDestinationClick: (DrawerScreenDestination) -> Unit,
    onDrawerQrCodeClick: () -> Unit,
    noteEventPublisher: (NoteContract.UiEvent) -> Unit,
    accountSwitcherCallbacks: AccountSwitcherCallbacks,
    onNewPostClick: () -> Unit,
) {
    val uiScope = rememberCoroutineScope()
    val drawerState: DrawerState = rememberDrawerState(DrawerValue.Closed)
    val snackbarHostState = remember { SnackbarHostState() }

    val seenNotificationsPagingItems = state.seenNotifications.collectAsLazyPagingItems()
    val notificationsListState = seenNotificationsPagingItems.rememberLazyListStatePagingWorkaround()

    val canScrollUp by remember(notificationsListState) {
        derivedStateOf {
            notificationsListState.firstVisibleItemIndex > 0
        }
    }

    LaunchedEffect(seenNotificationsPagingItems, state.badges) {
        if (state.badges.unreadNotificationsCount > 0) {
            seenNotificationsPagingItems.refresh()
        }
    }

    PrimalDrawerScaffold(
        drawerState = drawerState,
        activeDestination = PrimalTopLevelDestination.Notifications,
        onActiveDestinationClick = {
            uiScope.launch {
                notificationsListState.animateScrollToItem(
                    0,
                )
            }
        },
        accountSwitcherCallbacks = accountSwitcherCallbacks,
        onPrimaryDestinationChanged = onPrimaryDestinationChanged,
        onDrawerDestinationClick = onDrawerDestinationClick,
        onDrawerQrCodeClick = onDrawerQrCodeClick,
        badges = state.badges,
        focusModeEnabled = LocalContentDisplaySettings.current.focusModeEnabled,
        topAppBar = {
            PrimalTopAppBar(
                title = stringResource(id = R.string.notifications_title),
                avatarCdnImage = state.activeAccountAvatarCdnImage,
                legendaryCustomization = state.activeAccountLegendaryCustomization,
                navigationIcon = PrimalIcons.AvatarDefault,
                onNavigationIconClick = {
                    uiScope.launch { drawerState.open() }
                },
                scrollBehavior = it,
                actions = {
                    AppBarIcon(
                        icon = PrimalIcons.Search,
                        onClick = onSearchClick,
                        appBarIconContentDescription = stringResource(id = R.string.accessibility_search),
                    )
                },
            )
        },
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        },
        content = { paddingValues ->
            NotificationsList(
                state = state,
                noteState = noteState,
                seenPagingItems = seenNotificationsPagingItems,
                paddingValues = paddingValues,
                listState = notificationsListState,
                onGoToWallet = onGoToWallet,
                onPostLikeClick = {
                    noteEventPublisher(
                        NoteContract.UiEvent.PostLikeAction(
                            postId = it.postId,
                            postAuthorId = it.authorId,
                        ),
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
                    noteCallbacks.onNoteQuoteClick?.invoke(
                        it.asNeventString(),
                    )
                },
                onBookmarkClick = {
                    noteEventPublisher(NoteContract.UiEvent.BookmarkAction(noteId = it.postId))
                },
                noteCallbacks = noteCallbacks,
            )
        },
        floatingNewDataHost = {
            if (canScrollUp && state.badges.unreadNotificationsCount > 0) {
                NewNotificationsButton(
                    onClick = {
                        uiScope.launch {
                            notificationsListState.animateScrollToItem(0)
                        }
                    },
                )
            }
        },
        floatingActionButton = {
            NewPostFloatingActionButton(onNewPostClick = onNewPostClick)
        },
    )
}

@ExperimentalMaterial3Api
@Composable
private fun NotificationsList(
    state: NotificationsContract.UiState,
    noteState: NoteContract.UiState,
    listState: LazyListState,
    seenPagingItems: LazyPagingItems<NotificationUi>,
    paddingValues: PaddingValues,
    noteCallbacks: NoteCallbacks,
    onGoToWallet: () -> Unit,
    onPostLikeClick: (FeedPostUi) -> Unit,
    onRepostClick: (FeedPostUi) -> Unit,
    onZapClick: (FeedPostUi, ULong?, String?) -> Unit,
    onPostQuoteClick: (FeedPostUi) -> Unit,
    onBookmarkClick: (FeedPostUi) -> Unit,
) {
    var repostQuotePostConfirmation by remember { mutableStateOf<FeedPostUi?>(null) }
    if (repostQuotePostConfirmation != null) {
        repostQuotePostConfirmation?.let { post ->
            NoteRepostOrQuoteBottomSheet(
                onDismiss = { repostQuotePostConfirmation = null },
                onRepostClick = { onRepostClick(post) },
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
        contentPadding = paddingValues,
        state = listState,
    ) {
        items(
            items = state.unseenNotifications,
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

            if (state.unseenNotifications.last() != it || seenPagingItems.isNotEmpty()) {
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

        if (seenPagingItems.isEmpty() && state.unseenNotifications.isEmpty()) {
            when (seenPagingItems.loadState.refresh) {
                LoadState.Loading -> {
                    heightAdjustableLoadingLazyListPlaceholder(height = 98.dp)
                }

                is LoadState.NotLoading -> {
                    item(contentType = "NoContent") {
                        ListNoContent(
                            modifier = Modifier.fillParentMaxSize(),
                            noContentText = stringResource(
                                id = R.string.notifications_no_content,
                            ),
                            refreshButtonVisible = false,
                        )
                    }
                }

                is LoadState.Error -> {
                    item(contentType = "RefreshError") {
                        ListNoContent(
                            modifier = Modifier.fillParentMaxSize(),
                            noContentText = stringResource(
                                id = R.string.notifications_initial_loading_error,
                            ),
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
                ListLoadingError(
                    text = stringResource(R.string.app_error_loading_next_page),
                )
            }

            else -> Unit
        }
    }
}

@Composable
private fun NewNotificationsButton(onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .height(40.dp)
            .background(
                color = AppTheme.colorScheme.primary,
                shape = AppTheme.shapes.extraLarge,
            )
            .clickable { onClick() },
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            modifier = Modifier
                .padding(start = 12.dp, end = 16.dp)
                .wrapContentHeight(),
            text = stringResource(id = R.string.notification_list_button_jump_to_start),
            style = AppTheme.typography.bodySmall,
            color = Color.White,
        )
    }
}
