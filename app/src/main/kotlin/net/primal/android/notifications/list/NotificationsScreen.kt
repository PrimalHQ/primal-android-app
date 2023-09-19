package net.primal.android.notifications.list

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Divider
import androidx.compose.material3.DrawerState
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemContentType
import androidx.paging.compose.itemKey
import kotlinx.coroutines.launch
import net.primal.android.R
import net.primal.android.core.compose.ListLoading
import net.primal.android.core.compose.ListLoadingError
import net.primal.android.core.compose.ListNoContent
import net.primal.android.core.compose.PrimalTopAppBar
import net.primal.android.core.compose.PrimalTopLevelDestination
import net.primal.android.core.compose.icons.PrimalIcons
import net.primal.android.core.compose.icons.primaliconpack.AvatarDefault
import net.primal.android.core.compose.isEmpty
import net.primal.android.core.compose.isNotEmpty
import net.primal.android.core.compose.res.painterResource
import net.primal.android.core.utils.shortened
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
    onHashtagClick: (String) -> Unit,
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
        onPrimaryDestinationChanged = onTopLevelDestinationChanged,
        onDrawerDestinationClick = onDrawerScreenClick,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationsScreen(
    state: NotificationsContract.UiState,
    onProfileClick: (String) -> Unit,
    onNoteClick: (String) -> Unit,
    onHashtagClick: (String) -> Unit,
    onPrimaryDestinationChanged: (PrimalTopLevelDestination) -> Unit,
    onDrawerDestinationClick: (DrawerScreenDestination) -> Unit,
) {
    val uiScope = rememberCoroutineScope()
    val drawerState: DrawerState = rememberDrawerState(DrawerValue.Closed)
    val listState = rememberLazyListState()

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
            )
        },
        content = { paddingValues ->
            val seenPagingItems = state.seenNotifications.collectAsLazyPagingItems()

            LaunchedEffect(state.badges) {
                if (state.badges.notifications > 0) {
                    seenPagingItems.refresh()
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
                        onPostClick = onNoteClick,
                        onHashtagClick = onHashtagClick,
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
                                onProfileClick = onProfileClick,
                                onPostClick = onNoteClick,
                                onHashtagClick = onHashtagClick,
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
        },
    )
}

@Composable
private fun NotificationListItem(
    notifications: List<NotificationUi>,
    type: NotificationType,
    onProfileClick: (String) -> Unit,
    onPostClick: (String) -> Unit,
    onHashtagClick: (String) -> Unit,
) {
    val totalSatsZapped = notifications
        .firstOrNull { it.actionPost?.stats != null }
        ?.actionPost
        ?.stats
        ?.satsZapped
        .takeIf { it != null && it > 0 }

    NotificationListItem(
        notifications = notifications,
        imagePainter = type.toImagePainter(),
        suffixText = type.toSuffixText(
            usersZappedCount = notifications.size,
            totalSatsZapped = totalSatsZapped?.shortened(),
        ),
        onProfileClick = onProfileClick,
        onPostClick = onPostClick,
        onHashtagClick = onHashtagClick,
    )
}

@Composable
private fun NotificationType.toImagePainter(): Painter = when (this) {
    NotificationType.NEW_USER_FOLLOWED_YOU -> painterResource(
        darkResId = R.drawable.notification_type_new_user_followed_you_dark,
        lightResId = R.drawable.notification_type_new_user_followed_you_light,
    )

    NotificationType.YOUR_POST_WAS_ZAPPED -> painterResource(
        darkResId = R.drawable.notification_type_your_post_was_zapped_dark,
        lightResId = R.drawable.notification_type_your_post_was_zapped_light,
    )

    NotificationType.YOUR_POST_WAS_LIKED -> painterResource(
        darkResId = R.drawable.notification_type_your_post_was_liked_dark,
        lightResId = R.drawable.notification_type_your_post_was_liked_light,
    )

    NotificationType.YOUR_POST_WAS_REPOSTED -> painterResource(
        darkResId = R.drawable.notification_type_your_post_was_reposted_dark,
        lightResId = R.drawable.notification_type_your_post_was_reposted_light,
    )

    NotificationType.YOUR_POST_WAS_REPLIED_TO -> painterResource(
        darkResId = R.drawable.notification_type_your_post_was_replied_to_dark,
        lightResId = R.drawable.notification_type_your_post_was_replied_to_light,
    )

    NotificationType.YOU_WERE_MENTIONED_IN_POST -> painterResource(
        darkResId = R.drawable.notification_type_you_were_mentioned_in_a_post_dark,
        lightResId = R.drawable.notification_type_you_were_mentioned_in_a_post_light,
    )

    NotificationType.YOUR_POST_WAS_MENTIONED_IN_POST -> painterResource(
        darkResId = R.drawable.notification_type_your_post_was_mentioned_in_a_post_dark,
        lightResId = R.drawable.notification_type_your_post_was_mentioned_in_a_post_light,
    )

    NotificationType.POST_YOU_WERE_MENTIONED_IN_WAS_ZAPPED -> painterResource(
        darkResId = R.drawable.notification_type_post_you_were_mentioned_in_was_zapped_dark,
        lightResId = R.drawable.notification_type_post_you_were_mentioned_in_was_zapped_light,
    )

    NotificationType.POST_YOU_WERE_MENTIONED_IN_WAS_LIKED -> painterResource(
        darkResId = R.drawable.notification_type_post_you_were_mentioned_in_was_liked_dark,
        lightResId = R.drawable.notification_type_post_you_were_mentioned_in_was_liked_light,
    )

    NotificationType.POST_YOU_WERE_MENTIONED_IN_WAS_REPOSTED -> painterResource(
        darkResId = R.drawable.notification_type_post_you_were_mentioned_in_was_reposted_dark,
        lightResId = R.drawable.notification_type_post_you_were_mentioned_in_was_reposted_light,
    )

    NotificationType.POST_YOU_WERE_MENTIONED_IN_WAS_REPLIED_TO -> painterResource(
        darkResId = R.drawable.notification_type_post_you_were_mentioned_in_was_replied_to_dark,
        lightResId = R.drawable.notification_type_post_you_were_mentioned_in_was_replied_to_light,
    )

    NotificationType.POST_YOUR_POST_WAS_MENTIONED_IN_WAS_ZAPPED -> painterResource(
        darkResId = R.drawable.notification_type_post_your_post_was_mentioned_in_was_zapped_dark,
        lightResId = R.drawable.notification_type_post_your_post_was_mentioned_in_was_zapped_light,
    )

    NotificationType.POST_YOUR_POST_WAS_MENTIONED_IN_WAS_LIKED -> painterResource(
        darkResId = R.drawable.notification_type_post_your_post_was_mentioned_in_was_liked_dark,
        lightResId = R.drawable.notification_type_post_your_post_was_mentioned_in_was_liked_light,
    )

    NotificationType.POST_YOUR_POST_WAS_MENTIONED_IN_WAS_REPOSTED -> painterResource(
        darkResId = R.drawable.notification_type_post_your_post_was_mentioned_in_was_reposted_dark,
        lightResId = R.drawable.notification_type_post_your_post_was_mentioned_in_was_reposted_light,
    )

    NotificationType.POST_YOUR_POST_WAS_MENTIONED_IN_WAS_REPLIED_TO -> painterResource(
        darkResId = R.drawable.notification_type_post_your_post_was_mentioned_in_was_replied_to_dark,
        lightResId = R.drawable.notification_type_post_your_post_was_mentioned_in_was_replied_to_light,
    )
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
