package net.primal.android.notifications.list

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Divider
import androidx.compose.material3.DrawerState
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
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
import net.primal.android.core.compose.PrimalTopAppBar
import net.primal.android.core.compose.PrimalTopLevelDestination
import net.primal.android.core.compose.feed.RepostOrQuoteBottomSheet
import net.primal.android.core.compose.feed.ZapBottomSheet
import net.primal.android.core.compose.feed.model.FeedPostAction
import net.primal.android.core.compose.feed.model.FeedPostUi
import net.primal.android.core.compose.icons.PrimalIcons
import net.primal.android.core.compose.icons.primaliconpack.AvatarDefault
import net.primal.android.core.compose.icons.primaliconpack.Settings
import net.primal.android.core.compose.isEmpty
import net.primal.android.core.compose.isNotEmpty
import net.primal.android.core.compose.notifications.toImagePainter
import net.primal.android.core.utils.shortened
import net.primal.android.crypto.hexToNoteHrp
import net.primal.android.drawer.DrawerScreenDestination
import net.primal.android.drawer.PrimalDrawerScaffold
import net.primal.android.notifications.domain.NotificationType
import net.primal.android.notifications.list.ui.NotificationListItem
import net.primal.android.notifications.list.ui.NotificationUi
import net.primal.android.theme.AppTheme

@Composable
fun NotificationsScreen(
    viewModel: NotificationsViewModel,
    onProfileClick: (String) -> Unit,
    onNoteClick: (String) -> Unit,
    onPostQuoteClick: (String) -> Unit,
    onHashtagClick: (String) -> Unit,
    onNotificationSettings: () -> Unit,
    onWalletUnavailable: () -> Unit,
    onTopLevelDestinationChanged: (PrimalTopLevelDestination) -> Unit,
    onDrawerScreenClick: (DrawerScreenDestination) -> Unit,
) {
    val uiState = viewModel.state.collectAsState()

    LaunchedEffect(viewModel) {
        viewModel.setEvent(NotificationsContract.UiEvent.NotificationsSeen)
    }

    NotificationsScreen(
        state = uiState.value,
        onProfileClick = onProfileClick,
        onNoteClick = onNoteClick,
        onHashtagClick = onHashtagClick,
        onNotificationSettings = onNotificationSettings,
        onPrimaryDestinationChanged = onTopLevelDestinationChanged,
        onDrawerDestinationClick = onDrawerScreenClick,
        onWalletUnavailable = onWalletUnavailable,
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
    onHashtagClick: (String) -> Unit,
    onWalletUnavailable: () -> Unit,
    onPostQuoteClick: (String) -> Unit,
    onNotificationSettings: () -> Unit,
    onPrimaryDestinationChanged: (PrimalTopLevelDestination) -> Unit,
    onDrawerDestinationClick: (DrawerScreenDestination) -> Unit,
    eventPublisher: (NotificationsContract.UiEvent) -> Unit,
) {
    val uiScope = rememberCoroutineScope()
    val drawerState: DrawerState = rememberDrawerState(DrawerValue.Closed)
    val listState = rememberLazyListState()
    val snackbarHostState = remember { SnackbarHostState() }

    ErrorHandler(
        error = state.error,
        snackbarHostState = snackbarHostState,
    )

    PrimalDrawerScaffold(
        drawerState = drawerState,
        activeDestination = PrimalTopLevelDestination.Notifications,
        onActiveDestinationClick = { uiScope.launch { listState.animateScrollToItem(0) } },
        onPrimaryDestinationChanged = onPrimaryDestinationChanged,
        onDrawerDestinationClick = onDrawerDestinationClick,
        badges = state.badges,
        topBar = {
            PrimalTopAppBar(
                title = stringResource(id = R.string.notifications_title),
                avatarUrl = state.activeAccountAvatarUrl,
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
                }
            )
        },
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        },
        content = { paddingValues ->
            val seenPagingItems = state.seenNotifications.collectAsLazyPagingItems()

            LaunchedEffect(state.badges) {
                if (state.badges.notifications > 0) {
                    seenPagingItems.refresh()
                }
            }

            NotificationsList(
                state = state,
                seenPagingItems = seenPagingItems,
                paddingValues =  paddingValues,
                listState = listState,
                onProfileClick = onProfileClick,
                onNoteClick = onNoteClick,
                onHashtagClick = onHashtagClick,
                onWalletUnavailable = onWalletUnavailable,
                onPostLikeClick = {
                    eventPublisher(
                        NotificationsContract.UiEvent.PostLikeAction(
                            postId =  it.postId,
                            postAuthorId = it.authorId,
                        )
                    )
                },
                onRepostClick = {
                    eventPublisher(
                        NotificationsContract.UiEvent.RepostAction(
                            postId = it.postId,
                            postAuthorId = it.authorId,
                            postNostrEvent = it.rawNostrEventJson,
                        )
                    )
                },
                onZapClick = { postData, amount, description ->
                    eventPublisher(
                        NotificationsContract.UiEvent.ZapAction(
                            postId = postData.postId,
                            postAuthorId = postData.authorId,
                            zapAmount = amount,
                            zapDescription = description,
                            postAuthorLightningAddress = postData.authorLightningAddress
                        )
                    )
                },
                onPostQuoteClick = {
                    onPostQuoteClick("\n\nnostr:${it.postId.hexToNoteHrp()}")
                },
            )
        },
    )
}

@ExperimentalMaterial3Api
@Composable
private fun NotificationsList(
    state: NotificationsContract.UiState,
    seenPagingItems: LazyPagingItems<NotificationUi>,
    paddingValues: PaddingValues,
    listState: LazyListState,
    onProfileClick: (String) -> Unit,
    onNoteClick: (String) -> Unit,
    onHashtagClick: (String) -> Unit,
    onWalletUnavailable: () -> Unit,
    onPostLikeClick: (FeedPostUi) -> Unit,
    onRepostClick: (FeedPostUi) -> Unit,
    onZapClick: (FeedPostUi, ULong?, String?) -> Unit,
    onPostQuoteClick: (FeedPostUi) -> Unit,
) {

    var repostQuotePostConfirmation by remember { mutableStateOf<FeedPostUi?>(null) }
    if (repostQuotePostConfirmation != null) repostQuotePostConfirmation?.let { post ->
        RepostOrQuoteBottomSheet(
            onDismiss = { repostQuotePostConfirmation = null },
            onRepostClick = { onRepostClick(post) },
            onPostQuoteClick = { onPostQuoteClick(post) },
        )
    }

    var zapOptionsPostConfirmation by remember { mutableStateOf<FeedPostUi?>(null) }
    if (zapOptionsPostConfirmation != null) zapOptionsPostConfirmation?.let { post ->
        ZapBottomSheet(
            onDismissRequest = { zapOptionsPostConfirmation = null },
            receiverName = post.authorName,
            defaultZapAmount = state.defaultZapAmount ?: 42.toULong(),
            userZapOptions = state.zapOptions,
            onZap = { zapAmount, zapDescription ->
                onZapClick(post, zapAmount, zapDescription)
            }
        )
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
                walletConnected = state.walletConnected,
                onProfileClick = onProfileClick,
                onNoteClick = onNoteClick,
                onReplyClick = onNoteClick,
                onHashtagClick = onHashtagClick,
                onPostLikeClick = onPostLikeClick,
                onDefaultZapClick = { postData -> onZapClick(postData, null, null) },
                onZapOptionsClick = { postData -> zapOptionsPostConfirmation = postData },
                onRepostClick = { postData -> repostQuotePostConfirmation = postData },
                onWalletUnavailable = onWalletUnavailable,
            )

            if (state.unseenNotifications.last() != it || seenPagingItems.isNotEmpty()) {
                Divider(
                    color = AppTheme.colorScheme.outline,
                    thickness = 1.dp,
                )
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
                        walletConnected = state.walletConnected,
                        onProfileClick = onProfileClick,
                        onNoteClick = onNoteClick,
                        onReplyClick = onNoteClick,
                        onHashtagClick = onHashtagClick,
                        onPostLikeClick = onPostLikeClick,
                        onDefaultZapClick = { postData -> onZapClick(postData, null, null) },
                        onZapOptionsClick = { postData -> zapOptionsPostConfirmation = postData },
                        onRepostClick = { postData -> repostQuotePostConfirmation = postData },
                        onWalletUnavailable = onWalletUnavailable,
                    )

                    if (it < seenPagingItems.itemCount - 1) {
                        Divider(
                            color = AppTheme.colorScheme.outline,
                            thickness = 1.dp,
                        )
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
                            onRefresh = { seenPagingItems.refresh() }
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
                        .height(64.dp)
                )
            }

            is LoadState.Error -> item(contentType = "AppendError") {
                ListLoadingError(
                    text = stringResource(R.string.app_error_loading_next_page)
                )
            }

            else -> Unit
        }
    }
}

@Composable
private fun NotificationListItem(
    notifications: List<NotificationUi>,
    type: NotificationType,
    walletConnected: Boolean,
    onProfileClick: (String) -> Unit,
    onNoteClick: (String) -> Unit,
    onHashtagClick: (String) -> Unit,
    onReplyClick: (String) -> Unit,
    onPostLikeClick: (FeedPostUi) -> Unit,
    onRepostClick: (FeedPostUi) -> Unit,
    onDefaultZapClick: (FeedPostUi) -> Unit,
    onZapOptionsClick: (FeedPostUi) -> Unit,
    onWalletUnavailable: () -> Unit,
) {
    val totalSatsZapped = notifications
        .firstOrNull { it.actionPost?.stats != null }
        ?.actionPost
        ?.stats
        ?.satsZapped
        .takeIf { it != null && it > 0 }

    val postData = notifications.first().actionPost

    NotificationListItem(
        notifications = notifications,
        imagePainter = type.toImagePainter(),
        suffixText = type.toSuffixText(
            usersZappedCount = notifications.size,
            totalSatsZapped = totalSatsZapped?.shortened(),
        ),
        onProfileClick = onProfileClick,
        onPostClick = onNoteClick,
        onHashtagClick = onHashtagClick,
        onPostAction = { postAction ->
            when (postAction) {
                FeedPostAction.Reply -> {
                    postData?.postId?.let(onReplyClick)
                }
                FeedPostAction.Zap -> {
                    if (walletConnected) {
                        postData?.let { postData ->
                            onDefaultZapClick(postData)
                        }
                    } else {
                        onWalletUnavailable()
                    }
                }
                FeedPostAction.Like -> {
                    postData?.let(onPostLikeClick)
                }
                FeedPostAction.Repost -> {
                    postData?.let(onRepostClick)
                }
            }
        },
        onPostLongPressAction = { postAction ->
            when (postAction) {
                FeedPostAction.Zap -> {
                    if (walletConnected) {
                        postData?.let(onZapOptionsClick)
                    } else {
                        onWalletUnavailable()
                    }
                }
                else -> Unit
            }
        },
    )
}

@Composable
private fun ErrorHandler(
    error: NotificationsContract.UiState.NotificationsError?,
    snackbarHostState: SnackbarHostState,
) {
    val context = LocalContext.current
    LaunchedEffect(error ?: true) {
        val errorMessage = when (error) {
            is NotificationsContract.UiState.NotificationsError.InvalidZapRequest -> context.getString(R.string.post_action_invalid_zap_request)
            is NotificationsContract.UiState.NotificationsError.MissingLightningAddress -> context.getString(R.string.post_action_missing_lightning_address)
            is NotificationsContract.UiState.NotificationsError.FailedToPublishZapEvent -> context.getString(R.string.post_action_zap_failed)
            is NotificationsContract.UiState.NotificationsError.FailedToPublishLikeEvent -> context.getString(R.string.post_action_like_failed)
            is NotificationsContract.UiState.NotificationsError.FailedToPublishRepostEvent -> context.getString(R.string.post_action_repost_failed)
            is NotificationsContract.UiState.NotificationsError.MissingRelaysConfiguration -> context.getString(R.string.app_missing_relays_config)
            null -> return@LaunchedEffect
        }

        snackbarHostState.showSnackbar(
            message = errorMessage,
            duration = SnackbarDuration.Short,
        )
    }
}

@Composable
private fun NotificationType.toSuffixText(
    usersZappedCount: Int = 0,
    totalSatsZapped: String? = null,
): String = when (this) {
    NotificationType.NEW_USER_FOLLOWED_YOU -> stringResource(id = R.string.notification_list_item_followed_you)

    NotificationType.YOUR_POST_WAS_ZAPPED -> when (totalSatsZapped) {
        null -> stringResource(id = R.string.notification_list_item_zapped_your_post)
        else -> stringResource(
            id = R.string.notification_list_item_zapped_your_post_for_total_amount,
            totalSatsZapped
        )
    }

    NotificationType.YOUR_POST_WAS_LIKED -> stringResource(id = R.string.notification_list_item_liked_your_post)
    NotificationType.YOUR_POST_WAS_REPOSTED -> stringResource(id = R.string.notification_list_item_reposted_your_post)
    NotificationType.YOUR_POST_WAS_REPLIED_TO -> stringResource(id = R.string.notification_list_item_replied_to_your_post)

    NotificationType.YOU_WERE_MENTIONED_IN_POST -> stringResource(id = R.string.notification_list_item_mentioned_you_in_post)
    NotificationType.YOUR_POST_WAS_MENTIONED_IN_POST -> stringResource(id = R.string.notification_list_item_mentioned_your_post)

    NotificationType.POST_YOU_WERE_MENTIONED_IN_WAS_ZAPPED -> when (totalSatsZapped) {
        null -> stringResource(id = R.string.notification_list_item_post_you_were_mentioned_in_was_zapped)
        else -> when (usersZappedCount) {
            1 -> stringResource(
                id = R.string.notification_list_item_post_you_were_mentioned_in_was_zapped_for,
                totalSatsZapped
            )

            in 2..Int.MAX_VALUE -> stringResource(
                id = R.string.notification_list_item_post_you_were_mentioned_in_was_zapped_for_total_amount,
                totalSatsZapped
            )

            else -> stringResource(id = R.string.notification_list_item_post_you_were_mentioned_in_was_zapped)
        }
    }

    NotificationType.POST_YOU_WERE_MENTIONED_IN_WAS_LIKED -> stringResource(id = R.string.notification_list_item_post_you_were_mentioned_in_was_liked)
    NotificationType.POST_YOU_WERE_MENTIONED_IN_WAS_REPOSTED -> stringResource(id = R.string.notification_list_item_post_you_were_mentioned_in_was_reposted)
    NotificationType.POST_YOU_WERE_MENTIONED_IN_WAS_REPLIED_TO -> stringResource(id = R.string.notification_list_item_post_you_were_mentioned_in_was_replied_to)

    NotificationType.POST_YOUR_POST_WAS_MENTIONED_IN_WAS_ZAPPED -> when (totalSatsZapped) {
        null -> stringResource(id = R.string.notification_list_item_post_where_you_post_was_mentioned_was_zapped)
        else -> when (usersZappedCount) {
            1 -> stringResource(
                id = R.string.notification_list_item_post_where_you_post_was_mentioned_was_zapped_for,
                totalSatsZapped
            )

            in 2..Int.MAX_VALUE -> stringResource(
                id = R.string.notification_list_item_post_where_you_post_was_mentioned_was_zapped_for_total_amount,
                totalSatsZapped
            )

            else -> stringResource(id = R.string.notification_list_item_post_where_you_post_was_mentioned_was_zapped)
        }
    }

    NotificationType.POST_YOUR_POST_WAS_MENTIONED_IN_WAS_LIKED -> stringResource(id = R.string.notification_list_item_post_where_you_post_was_mentioned_was_liked)
    NotificationType.POST_YOUR_POST_WAS_MENTIONED_IN_WAS_REPOSTED -> stringResource(id = R.string.notification_list_item_post_where_you_post_was_mentioned_was_reposted)
    NotificationType.POST_YOUR_POST_WAS_MENTIONED_IN_WAS_REPLIED_TO -> stringResource(id = R.string.notification_list_item_post_where_you_post_was_mentioned_was_replied_to)
}
