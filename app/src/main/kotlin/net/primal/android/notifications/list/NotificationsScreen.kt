package net.primal.android.notifications.list

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.DrawerState
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SnackbarDuration
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemContentType
import androidx.paging.compose.itemKey
import kotlinx.coroutines.launch
import net.primal.android.R
import net.primal.android.core.compose.AppBarIcon
import net.primal.android.core.compose.ListLoading
import net.primal.android.core.compose.ListLoadingError
import net.primal.android.core.compose.ListNoContent
import net.primal.android.core.compose.PrimalDivider
import net.primal.android.core.compose.PrimalTopAppBar
import net.primal.android.core.compose.PrimalTopLevelDestination
import net.primal.android.core.compose.feed.RepostOrQuoteBottomSheet
import net.primal.android.core.compose.feed.model.FeedPostUi
import net.primal.android.core.compose.feed.zaps.UnableToZapBottomSheet
import net.primal.android.core.compose.feed.zaps.ZapBottomSheet
import net.primal.android.core.compose.foundation.rememberLazyListStatePagingWorkaround
import net.primal.android.core.compose.icons.PrimalIcons
import net.primal.android.core.compose.icons.primaliconpack.AvatarDefault
import net.primal.android.core.compose.icons.primaliconpack.Settings
import net.primal.android.core.compose.isEmpty
import net.primal.android.core.compose.isNotEmpty
import net.primal.android.core.compose.runtime.DisposableLifecycleObserverEffect
import net.primal.android.crypto.hexToNoteHrp
import net.primal.android.drawer.DrawerScreenDestination
import net.primal.android.drawer.PrimalDrawerScaffold
import net.primal.android.notifications.list.ui.NotificationListItem
import net.primal.android.notifications.list.ui.NotificationUi
import net.primal.android.theme.AppTheme
import net.primal.android.wallet.zaps.canZap

@Composable
fun NotificationsScreen(
    viewModel: NotificationsViewModel,
    onProfileClick: (String) -> Unit,
    onNoteClick: (String) -> Unit,
    onNoteReplyClick: (String) -> Unit,
    onPostQuoteClick: (String) -> Unit,
    onHashtagClick: (String) -> Unit,
    onMediaClick: (String, String) -> Unit,
    onNotificationSettings: () -> Unit,
    onGoToWallet: () -> Unit,
    onTopLevelDestinationChanged: (PrimalTopLevelDestination) -> Unit,
    onDrawerScreenClick: (DrawerScreenDestination) -> Unit,
) {
    val uiState = viewModel.state.collectAsState()

    DisposableLifecycleObserverEffect {
        when (it) {
            Lifecycle.Event.ON_DESTROY -> viewModel.setEvent(
                NotificationsContract.UiEvent.NotificationsSeen,
            )

            else -> Unit
        }
    }

    NotificationsScreen(
        state = uiState.value,
        onProfileClick = onProfileClick,
        onNoteClick = onNoteClick,
        onNoteReplyClick = onNoteReplyClick,
        onHashtagClick = onHashtagClick,
        onMediaClick = onMediaClick,
        onNotificationSettings = onNotificationSettings,
        onPrimaryDestinationChanged = onTopLevelDestinationChanged,
        onDrawerDestinationClick = onDrawerScreenClick,
        onGoToWallet = onGoToWallet,
        onPostQuoteClick = onPostQuoteClick,
        eventPublisher = { viewModel.setEvent(it) },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationsScreen(
    state: NotificationsContract.UiState,
    onProfileClick: (String) -> Unit,
    onNoteClick: (String) -> Unit,
    onNoteReplyClick: (String) -> Unit,
    onHashtagClick: (String) -> Unit,
    onMediaClick: (String, String) -> Unit,
    onGoToWallet: () -> Unit,
    onPostQuoteClick: (String) -> Unit,
    onNotificationSettings: () -> Unit,
    onPrimaryDestinationChanged: (PrimalTopLevelDestination) -> Unit,
    onDrawerDestinationClick: (DrawerScreenDestination) -> Unit,
    eventPublisher: (NotificationsContract.UiEvent) -> Unit,
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

    ErrorHandler(
        error = state.error,
        snackbarHostState = snackbarHostState,
    )

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
        onPrimaryDestinationChanged = onPrimaryDestinationChanged,
        onDrawerDestinationClick = onDrawerDestinationClick,
        badges = state.badges,
        topBar = {
            PrimalTopAppBar(
                title = stringResource(id = R.string.notifications_title),
                avatarCdnImage = state.activeAccountAvatarCdnImage,
                navigationIcon = PrimalIcons.AvatarDefault,
                onNavigationIconClick = {
                    uiScope.launch { drawerState.open() }
                },
                scrollBehavior = it,
                actions = {
                    AppBarIcon(
                        icon = PrimalIcons.Settings,
                        onClick = onNotificationSettings,
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
                seenPagingItems = seenNotificationsPagingItems,
                paddingValues = paddingValues,
                listState = notificationsListState,
                onProfileClick = onProfileClick,
                onNoteClick = onNoteClick,
                onHashtagClick = onHashtagClick,
                onMediaClick = onMediaClick,
                onGoToWallet = onGoToWallet,
                onNoteReplyClick = onNoteReplyClick,
                onPostLikeClick = {
                    eventPublisher(
                        NotificationsContract.UiEvent.PostLikeAction(
                            postId = it.postId,
                            postAuthorId = it.authorId,
                        ),
                    )
                },
                onRepostClick = {
                    eventPublisher(
                        NotificationsContract.UiEvent.RepostAction(
                            postId = it.postId,
                            postAuthorId = it.authorId,
                            postNostrEvent = it.rawNostrEventJson,
                        ),
                    )
                },
                onZapClick = { postData, amount, description ->
                    eventPublisher(
                        NotificationsContract.UiEvent.ZapAction(
                            postId = postData.postId,
                            postAuthorId = postData.authorId,
                            zapAmount = amount,
                            zapDescription = description,
                        ),
                    )
                },
                onPostQuoteClick = {
                    onPostQuoteClick("\n\nnostr:${it.postId.hexToNoteHrp()}")
                },
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
    )
}

@ExperimentalMaterial3Api
@Composable
private fun NotificationsList(
    state: NotificationsContract.UiState,
    listState: LazyListState,
    seenPagingItems: LazyPagingItems<NotificationUi>,
    paddingValues: PaddingValues,
    onProfileClick: (String) -> Unit,
    onNoteClick: (String) -> Unit,
    onNoteReplyClick: (String) -> Unit,
    onHashtagClick: (String) -> Unit,
    onMediaClick: (String, String) -> Unit,
    onGoToWallet: () -> Unit,
    onPostLikeClick: (FeedPostUi) -> Unit,
    onRepostClick: (FeedPostUi) -> Unit,
    onZapClick: (FeedPostUi, ULong?, String?) -> Unit,
    onPostQuoteClick: (FeedPostUi) -> Unit,
) {
    var repostQuotePostConfirmation by remember { mutableStateOf<FeedPostUi?>(null) }
    if (repostQuotePostConfirmation != null) {
        repostQuotePostConfirmation?.let { post ->
            RepostOrQuoteBottomSheet(
                onDismiss = { repostQuotePostConfirmation = null },
                onRepostClick = { onRepostClick(post) },
                onPostQuoteClick = { onPostQuoteClick(post) },
            )
        }
    }

    var showCantZapWarning by remember { mutableStateOf(false) }
    if (showCantZapWarning) {
        UnableToZapBottomSheet(
            zappingState = state.zappingState,
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
                zappingState = state.zappingState,
                onZap = { zapAmount, zapDescription ->
                    if (state.zappingState.canZap(zapAmount)) {
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
            key = { it.map { notificationUi -> notificationUi.uniqueKey }.toString() },
            contentType = { it.first().notificationType },
        ) {
            NotificationListItem(
                notifications = it,
                type = it.first().notificationType,
                onProfileClick = onProfileClick,
                onNoteClick = onNoteClick,
                onReplyClick = onNoteClick,
                onHashtagClick = onHashtagClick,
                onMediaClick = onMediaClick,
                onPostLikeClick = onPostLikeClick,
                onDefaultZapClick = { postData ->
                    if (state.zappingState.canZap()) {
                        onZapClick(postData, null, null)
                    } else {
                        showCantZapWarning = true
                    }
                },
                onZapOptionsClick = { postData ->
                    if (state.zappingState.walletConnected) {
                        zapOptionsPostConfirmation = postData
                    } else {
                        showCantZapWarning = true
                    }
                },
                onRepostClick = { postData -> repostQuotePostConfirmation = postData },
            )

            if (state.unseenNotifications.last() != it || seenPagingItems.isNotEmpty()) {
                PrimalDivider()
            }
        }

        items(
            count = seenPagingItems.itemCount,
            key = seenPagingItems.itemKey(key = { it.uniqueKey }),
            contentType = seenPagingItems.itemContentType { it.notificationType },
        ) {
            val item = seenPagingItems[it]

            when {
                item != null -> {
                    NotificationListItem(
                        notifications = listOf(item),
                        type = item.notificationType,
                        onProfileClick = onProfileClick,
                        onNoteClick = onNoteClick,
                        onReplyClick = onNoteReplyClick,
                        onHashtagClick = onHashtagClick,
                        onMediaClick = onMediaClick,
                        onPostLikeClick = onPostLikeClick,
                        onDefaultZapClick = { postData ->
                            if (state.zappingState.canZap()) {
                                onZapClick(postData, null, null)
                            } else {
                                showCantZapWarning = true
                            }
                        },
                        onZapOptionsClick = { postData ->
                            if (state.zappingState.walletConnected) {
                                zapOptionsPostConfirmation = postData
                            } else {
                                showCantZapWarning = true
                            }
                        },
                        onRepostClick = { postData -> repostQuotePostConfirmation = postData },
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
            LoadState.Loading -> item(contentType = "LoadingAppend") {
                ListLoading(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(64.dp),
                )
            }

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
                .padding(bottom = 4.dp)
                .wrapContentHeight(),
            text = stringResource(id = R.string.notification_list_button_jump_to_start),
            style = AppTheme.typography.bodySmall,
            color = Color.White,
        )
    }
}

@Composable
private fun ErrorHandler(
    error: NotificationsContract.UiState.NotificationsError?,
    snackbarHostState: SnackbarHostState,
) {
    val context = LocalContext.current
    LaunchedEffect(error ?: true) {
        val errorMessage = when (error) {
            is NotificationsContract.UiState.NotificationsError.InvalidZapRequest ->
                context.getString(R.string.post_action_invalid_zap_request)

            is NotificationsContract.UiState.NotificationsError.MissingLightningAddress ->
                context.getString(R.string.post_action_missing_lightning_address)

            is NotificationsContract.UiState.NotificationsError.FailedToPublishZapEvent ->
                context.getString(R.string.post_action_zap_failed)

            is NotificationsContract.UiState.NotificationsError.FailedToPublishLikeEvent ->
                context.getString(R.string.post_action_like_failed)

            is NotificationsContract.UiState.NotificationsError.FailedToPublishRepostEvent ->
                context.getString(R.string.post_action_repost_failed)

            is NotificationsContract.UiState.NotificationsError.MissingRelaysConfiguration ->
                context.getString(R.string.app_missing_relays_config)

            null -> return@LaunchedEffect
        }

        snackbarHostState.showSnackbar(
            message = errorMessage,
            duration = SnackbarDuration.Short,
        )
    }
}
