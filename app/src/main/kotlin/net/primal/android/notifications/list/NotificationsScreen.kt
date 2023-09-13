package net.primal.android.notifications.list

import androidx.compose.foundation.Image
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.DrawerState
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Surface
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import net.primal.android.R
import net.primal.android.core.compose.AvatarThumbnailsRow
import net.primal.android.core.compose.ListLoading
import net.primal.android.core.compose.ListNoContent
import net.primal.android.core.compose.NostrUserText
import net.primal.android.core.compose.PrimalTopAppBar
import net.primal.android.core.compose.PrimalTopLevelDestination
import net.primal.android.core.compose.icons.PrimalIcons
import net.primal.android.core.compose.icons.primaliconpack.AvatarDefault
import net.primal.android.core.utils.asEllipsizedNpub
import net.primal.android.drawer.DrawerScreenDestination
import net.primal.android.drawer.PrimalDrawerScaffold
import net.primal.android.notifications.db.Notification
import net.primal.android.notifications.db.NotificationData
import net.primal.android.notifications.domain.NotificationType
import net.primal.android.profile.db.authorNameUiFriendly
import net.primal.android.theme.PrimalTheme

@Composable
fun NotificationsScreen(
    viewModel: NotificationsViewModel,
    onProfileClick: (String) -> Unit,
    onNoteClick: (String) -> Unit,
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
            LazyColumn(
                contentPadding = paddingValues
            ) {
                items(
                    items = state.notifications,
                    key = { "${it.data.type};${it.data.createdAt};${it.data.userId}" },
                    contentType = { it.data.type },
                ) {
                    when (it.data.type) {
                        NotificationType.NEW_USER_FOLLOWED_YOU -> UserFollowedYouListItem(
                            notifications = listOf(it),
                            onProfileClick = onProfileClick,
                        )

                        NotificationType.USER_UNFOLLOWED_YOU -> UserUnfollowedYouListItem(
                            notifications = listOf(it),
                            onProfileClick = onProfileClick,
                        )

                        NotificationType.YOUR_POST_WAS_ZAPPED -> Unit
                        NotificationType.YOUR_POST_WAS_LIKED -> Unit
                        NotificationType.YOUR_POST_WAS_REPOSTED -> Unit
                        NotificationType.YOUR_POST_WAS_REPLIED_TO -> Unit

                        NotificationType.YOU_WERE_MENTIONED_IN_POST -> Unit
                        NotificationType.YOUR_POST_WAS_MENTIONED_IN_POST -> Unit

                        NotificationType.POST_YOU_WERE_MENTIONED_IN_WAS_ZAPPED -> Unit
                        NotificationType.POST_YOU_WERE_MENTIONED_IN_WAS_LIKED -> Unit
                        NotificationType.POST_YOU_WERE_MENTIONED_IN_WAS_REPOSTED -> Unit
                        NotificationType.POST_YOU_WERE_MENTIONED_IN_WAS_REPLIED_TO -> Unit

                        NotificationType.POST_YOUR_POST_WAS_MENTIONED_IN_WAS_ZAPPED -> Unit
                        NotificationType.POST_YOUR_POST_WAS_MENTIONED_IN_WAS_LIKED -> Unit
                        NotificationType.POST_YOUR_POST_WAS_MENTIONED_IN_WAS_REPOSTED -> Unit
                        NotificationType.POST_YOUR_POST_WAS_MENTIONED_IN_WAS_REPLIED_TO -> Unit
                    }

                }

                if (state.notifications.isEmpty()) {
                    item {
                        if (state.loading) {
                            ListLoading(
                                modifier = Modifier.fillParentMaxSize(),
                            )
                        } else {
                            ListNoContent(
                                modifier = Modifier.fillParentMaxSize(),
                                refreshButtonVisible = false,
                                noContentText = "No notifications."
                            )
                        }
                    }
                }
            }
        },
    )
}

@Composable
private fun RootNotificationListItem(
    iconPainter: Painter,
    content: @Composable () -> Unit,
) {
    Row {
        Box(
            modifier = Modifier.padding(all = 16.dp),
        ) {
            Image(
                modifier = Modifier.padding(top = 8.dp),
                painter = iconPainter,
                contentDescription = null,
            )
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
        ) {
            content()
        }
    }
}

@Composable
private fun UserFollowedYouListItem(
    notifications: List<Notification>,
    onProfileClick: (String) -> Unit,
) {
    FollowerListItem(
        notifications = notifications,
        imagePainter = painterResource(
            id = if (isSystemInDarkTheme()) {
                R.drawable.notification_type_new_user_followed_you_dark
            } else {
                R.drawable.notification_type_new_user_followed_you_light
            }
        ),
        suffixText = stringResource(id = R.string.notification_list_item_followed_you),
        onProfileClick = onProfileClick,
    )
}

@Composable
private fun UserUnfollowedYouListItem(
    notifications: List<Notification>,
    onProfileClick: (String) -> Unit,
) {
    FollowerListItem(
        notifications = notifications,
        imagePainter = painterResource(
            id = if (isSystemInDarkTheme()) {
                R.drawable.notification_type_user_unfollowed_you_dark
            } else {
                R.drawable.notification_type_user_unfollowed_you_light
            }
        ),
        suffixText = stringResource(id = R.string.notification_list_item_unfollowed_you),
        onProfileClick = onProfileClick,
    )
}

@Composable
private fun FollowerListItem(
    notifications: List<Notification>,
    imagePainter: Painter,
    suffixText: String,
    onProfileClick: (String) -> Unit,
) {
    RootNotificationListItem(
        iconPainter = imagePainter,
    ) {
        val firstNotification = notifications.first()
        val firstFollower = firstNotification.follower

        Column {
            AvatarThumbnailsRow(
                avatarUrls = notifications.map { it.follower?.picture },
                onClick = {
                    firstFollower?.ownerId?.let(onProfileClick)
                },
            )

            val andOthersText = pluralStringResource(
                R.plurals.notification_list_item_and_others,
                notifications.size - 1,
                notifications.size - 1,
            )
            NostrUserText(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                displayName = firstFollower?.authorNameUiFriendly()
                    ?: firstNotification.data.follower?.asEllipsizedNpub()
                    ?: firstFollower?.displayName.toString(),
                internetIdentifier = firstFollower?.internetIdentifier,
                annotatedStringSuffixBuilder = {
                    if (notifications.size > 1) append(" $andOthersText")
                    append(" $suffixText")
                }
            )
        }
    }
}

@Preview
@Composable
fun PreviewNewUserFollowedYourListItem() {
    PrimalTheme {
        Surface {
            UserFollowedYouListItem(
                notifications = listOf(
                    Notification(
                        data = NotificationData(
                            userId = "",
                            createdAt = 0L,
                            type = NotificationType.NEW_USER_FOLLOWED_YOU
                        )
                    ),
                    Notification(
                        data = NotificationData(
                            userId = "",
                            createdAt = 0L,
                            type = NotificationType.NEW_USER_FOLLOWED_YOU
                        )
                    ),
                ),
                onProfileClick = {},
            )
        }
    }

}