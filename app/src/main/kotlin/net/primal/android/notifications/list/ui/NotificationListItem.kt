package net.primal.android.notifications.list.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.PlaceholderVerticalAlign
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import java.time.Instant
import net.primal.android.R
import net.primal.android.core.compose.AvatarOverlap
import net.primal.android.core.compose.AvatarThumbnailsRow
import net.primal.android.core.compose.NostrUserText
import net.primal.android.core.compose.WrappedContentWithSuffix
import net.primal.android.core.compose.asBeforeNowFormat
import net.primal.android.core.compose.notifications.toImagePainter
import net.primal.android.core.compose.preview.PrimalPreview
import net.primal.android.core.ext.openUriSafely
import net.primal.android.core.utils.shortened
import net.primal.android.notes.feed.model.EventStatsUi
import net.primal.android.notes.feed.model.FeedPostAction
import net.primal.android.notes.feed.model.FeedPostUi
import net.primal.android.notes.feed.model.toNoteContentUi
import net.primal.android.notes.feed.note.ui.FeedNoteActionsRow
import net.primal.android.notes.feed.note.ui.NoteContent
import net.primal.android.notes.feed.note.ui.events.NoteCallbacks
import net.primal.android.premium.legend.domain.LegendaryCustomization
import net.primal.android.premium.legend.domain.LegendaryStyle
import net.primal.android.theme.AppTheme
import net.primal.android.theme.domain.PrimalTheme.Sunset
import net.primal.domain.notifications.NotificationType

@Composable
fun NotificationListItem(
    notifications: List<NotificationUi>,
    type: NotificationType,
    isSeen: Boolean,
    noteCallbacks: NoteCallbacks,
    onReplyClick: ((String) -> Unit)? = null,
    onPostLikeClick: ((FeedPostUi) -> Unit)? = null,
    onRepostClick: ((FeedPostUi) -> Unit)? = null,
    onBookmarkClick: ((FeedPostUi) -> Unit)? = null,
    onDefaultZapClick: ((FeedPostUi) -> Unit)? = null,
    onZapOptionsClick: ((FeedPostUi) -> Unit)? = null,
) {
    notifications.map { it.actionUserSatsZapped }

    val activeUsersTotalSatsZapped = notifications
        .mapNotNull { it.actionUserSatsZapped }
        .sum()
        .takeIf { it > 0 }

    val postTotalSatsZapped = notifications
        .firstOrNull { it.actionPost?.stats != null }
        ?.actionPost
        ?.stats
        ?.satsZapped
        .takeIf { it != null && it > 0 }

    val postData = notifications.first().actionPost

    NotificationListItem(
        notifications = notifications,
        isSeen = isSeen,
        imagePainter = type.toImagePainter(),
        suffixText = type.toSuffixText(
            usersZappedCount = notifications.size,
            totalSatsZapped = if (notifications.size == 1) {
                if (type == NotificationType.YOUR_POST_WAS_ZAPPED) {
                    postTotalSatsZapped?.shortened()
                } else {
                    activeUsersTotalSatsZapped?.shortened()
                }
            } else {
                postTotalSatsZapped?.shortened()
            },
        ),
        noteCallbacks = noteCallbacks,
        onPostAction = { postAction ->
            if (postData != null) {
                when (postAction) {
                    FeedPostAction.Reply -> if (onReplyClick != null) onReplyClick(postData.postId)
                    FeedPostAction.Zap -> onDefaultZapClick?.invoke(postData)
                    FeedPostAction.Like -> onPostLikeClick?.invoke(postData)
                    FeedPostAction.Repost -> onRepostClick?.invoke(postData)
                    FeedPostAction.Bookmark -> onBookmarkClick?.invoke(postData)
                }
            }
        },
        onPostLongPressAction = { postAction ->
            if (postData != null && postAction == FeedPostAction.Zap) {
                onZapOptionsClick?.invoke(postData)
            }
        },
    )
}

@Composable
private fun NotificationListItem(
    notifications: List<NotificationUi>,
    isSeen: Boolean,
    imagePainter: Painter,
    suffixText: String,
    noteCallbacks: NoteCallbacks,
    onPostAction: ((FeedPostAction) -> Unit)? = null,
    onPostLongPressAction: ((FeedPostAction) -> Unit)? = null,
) {
    val firstNotification = notifications.first()

    Row(
        modifier = Modifier
            .background(color = AppTheme.colorScheme.surfaceVariant)
            .clickable(
                enabled = notifications.size == 1 || firstNotification.actionPost != null,
                onClick = {
                    if (notifications.size == 1) {
                        if (firstNotification.notificationType == NotificationType.NEW_USER_FOLLOWED_YOU) {
                            firstNotification.actionUserId?.let { noteCallbacks.onProfileClick?.invoke(it) }
                        } else {
                            firstNotification.actionPost?.postId?.let { noteCallbacks.onNoteClick?.invoke(it) }
                        }
                    } else {
                        firstNotification.actionPost?.postId?.let { noteCallbacks.onNoteClick?.invoke(it) }
                    }
                },
            ),
    ) {
        NotificationIconAndExtraStats(
            icon = imagePainter,
            notifications = notifications,
        )

        NotificationContent(
            notifications = notifications,
            isSeen = isSeen,
            suffixText = suffixText,
            onPostAction = onPostAction,
            onPostLongPressAction = onPostLongPressAction,
            noteCallbacks = noteCallbacks,
        )
    }
}

@Composable
private fun NotificationContent(
    notifications: List<NotificationUi>,
    isSeen: Boolean,
    suffixText: String,
    onPostAction: ((FeedPostAction) -> Unit)?,
    onPostLongPressAction: ((FeedPostAction) -> Unit)?,
    noteCallbacks: NoteCallbacks,
) {
    val firstNotification = notifications.first()
    val actionPost = firstNotification.actionPost
    Column(
        modifier = Modifier.fillMaxWidth(),
    ) {
        if (notifications.size == 1) {
            NotificationHeader(
                modifier = Modifier.padding(top = 12.dp, bottom = 12.dp, end = 12.dp),
                notification = notifications.first(),
                suffixText = suffixText,
                isSeen = isSeen,
                onProfileClick = noteCallbacks.onProfileClick,
            )
        } else {
            NotificationsGroupHeader(
                modifier = Modifier.padding(top = 12.dp, bottom = 12.dp, end = 12.dp),
                notifications = notifications,
                suffixText = suffixText,
                isSeen = isSeen,
                onProfileClick = noteCallbacks.onProfileClick,
            )
        }

        val localUriHandler = LocalUriHandler.current

        if (actionPost != null) {
            NoteContent(
                modifier = Modifier.padding(end = 16.dp),
                data = actionPost.toNoteContentUi(),
                expanded = false,
                onClick = { noteCallbacks.onNoteClick?.invoke(actionPost.postId) },
                onUrlClick = { localUriHandler.openUriSafely(it) },
                noteCallbacks = noteCallbacks,
            )

            FeedNoteActionsRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp)
                    .padding(end = 16.dp),
                eventStats = actionPost.stats,
                isBookmarked = actionPost.isBookmarked,
                onPostAction = onPostAction,
                onPostLongPressAction = onPostLongPressAction,
            )
        }
    }
}

@Composable
private fun NotificationIconAndExtraStats(icon: Painter, notifications: List<NotificationUi>) {
    val firstNotification = notifications.first()
    Column(
        modifier = Modifier.padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Image(
            modifier = Modifier
                .padding(top = 12.dp)
                .size(32.dp),
            painter = icon,
            contentDescription = null,
        )

        val extraStat = notifications.extractExtraStat()
        if (extraStat != null && extraStat > 0) {
            Text(
                modifier = Modifier.padding(top = 8.dp),
                text = extraStat.shortened(),
                style = AppTheme.typography.bodySmall,
                fontWeight = FontWeight.SemiBold,
                color = firstNotification.extraStatColor(),
            )
        }
    }
}

@Composable
private fun NotificationUi.extraStatColor() =
    when (this.notificationType) {
        NotificationType.YOUR_POST_WAS_ZAPPED -> AppTheme.extraColorScheme.zapped
        NotificationType.YOUR_POST_WAS_LIKED -> AppTheme.extraColorScheme.liked
        NotificationType.YOUR_POST_WAS_REPOSTED -> AppTheme.extraColorScheme.reposted
        NotificationType.YOUR_POST_WAS_REPLIED_TO -> AppTheme.extraColorScheme.replied
        else -> Color.Unspecified
    }

@Composable
private fun List<NotificationUi>.extractExtraStat() =
    when (first().notificationType) {
        NotificationType.YOUR_POST_WAS_ZAPPED -> this.mapNotNull { it.actionUserSatsZapped }.sum()
        NotificationType.YOUR_POST_WAS_LIKED -> first().actionPost?.stats?.likesCount
        NotificationType.YOUR_POST_WAS_REPOSTED -> first().actionPost?.stats?.repostsCount
        NotificationType.YOUR_POST_WAS_REPLIED_TO -> first().actionPost?.stats?.repliesCount
        else -> null
    }

@Composable
private fun NotificationHeader(
    modifier: Modifier,
    notification: NotificationUi,
    suffixText: String,
    isSeen: Boolean,
    onProfileClick: ((String) -> Unit)? = null,
) {
    WrappedContentWithSuffix(
        modifier = modifier,
        wrappedContent = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                HeaderContent(
                    notifications = listOf(notification),
                    titlePaddingValues = PaddingValues(start = 8.dp, end = 8.dp, top = 4.dp),
                    suffixText = suffixText,
                    onProfileClick = onProfileClick,
                )
            }
        },
        suffixFixedContent = {
            BadgedBox(
                modifier = Modifier.fillMaxHeight(),
                badge = {
                    if (!isSeen) {
                        Badge(
                            modifier = Modifier.align(Alignment.TopEnd),
                            containerColor = AppTheme.colorScheme.primary,
                        )
                    }
                },
            ) {
                Text(
                    modifier = Modifier.padding(end = 8.dp),
                    text = notification.createdAt.asBeforeNowFormat(),
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    style = AppTheme.typography.bodySmall,
                    color = AppTheme.extraColorScheme.onSurfaceVariantAlt3,
                )
            }
        },
    )
}

@Composable
private fun NotificationsGroupHeader(
    modifier: Modifier,
    notifications: List<NotificationUi>,
    suffixText: String,
    isSeen: Boolean,
    onProfileClick: ((String) -> Unit)? = null,
) {
    val lastNotification = notifications.last()

    Column(
        modifier = modifier,
    ) {
        WrappedContentWithSuffix(
            wrappedContent = {
                AvatarThumbnailsRow(
                    modifier = Modifier.fillMaxWidth(),
                    avatarCdnImages = notifications.map { it.actionUserAvatarCdnImage },
                    avatarLegendaryCustomizations = notifications.map { it.actionUserLegendaryCustomization },
                    avatarOverlap = AvatarOverlap.None,
                    hasAvatarBorder = false,
                    onClick = { index ->
                        notifications.getOrNull(index)?.actionUserId?.let { onProfileClick?.invoke(it) }
                    },
                )
            },
            suffixFixedContent = {
                BadgedBox(
                    modifier = Modifier.fillMaxHeight(),
                    badge = {
                        if (!isSeen) {
                            Badge(
                                modifier = Modifier.align(Alignment.TopEnd),
                                containerColor = AppTheme.colorScheme.primary,
                            )
                        }
                    },
                ) {
                    Text(
                        modifier = Modifier.padding(end = 8.dp),
                        text = lastNotification.createdAt.asBeforeNowFormat(),
                        textAlign = TextAlign.Center,
                        maxLines = 1,
                        style = AppTheme.typography.bodySmall,
                        color = AppTheme.extraColorScheme.onSurfaceVariantAlt3,
                    )
                }
            },
        )

        HeaderContent(
            notifications = notifications,
            titlePaddingValues = PaddingValues(end = 8.dp, top = 8.dp),
            suffixText = suffixText,
            showAvatars = false,
            onProfileClick = onProfileClick,
        )
    }
}

@Composable
private fun HeaderContent(
    notifications: List<NotificationUi>,
    titlePaddingValues: PaddingValues,
    suffixText: String,
    showAvatars: Boolean = true,
    onProfileClick: ((String) -> Unit)? = null,
) {
    val firstNotification = notifications.first()

    if (showAvatars) {
        AvatarThumbnailsRow(
            avatarCdnImages = notifications.map { it.actionUserAvatarCdnImage },
            avatarLegendaryCustomizations = notifications.map { it.actionUserLegendaryCustomization },
            avatarOverlap = AvatarOverlap.None,
            hasAvatarBorder = false,
            onClick = { index ->
                notifications.getOrNull(index)?.actionUserId?.let { onProfileClick?.invoke(it) }
            },
        )
    }

    val andOthersText = pluralStringResource(
        R.plurals.notification_list_item_and_others,
        notifications.size - 1,
        notifications.size - 1,
    )

    NostrUserText(
        modifier = Modifier
            .fillMaxWidth()
            .padding(titlePaddingValues),
        style = AppTheme.typography.bodyMedium.copy(
            color = AppTheme.colorScheme.onSurface,
        ),
        maxLines = 2,
        displayName = firstNotification.actionUserDisplayName ?: "undefined",
        displayNameColor = AppTheme.colorScheme.onSurface,
        internetIdentifier = firstNotification.actionUserInternetIdentifier,
        internetIdentifierBadgeSize = 14.dp,
        internetIdentifierBadgeAlign = PlaceholderVerticalAlign.TextCenter,
        overflow = TextOverflow.Ellipsis,
        annotatedStringSuffixBuilder = {
            val appendText = if (notifications.size > 1) andOthersText else suffixText
            if (firstNotification.actionUserInternetIdentifier.isNullOrEmpty()) append(' ')
            append(appendText)
        },
        legendaryCustomization = firstNotification.actionUserLegendaryCustomization,
    )
}

@Composable
private fun NotificationType.toSuffixText(usersZappedCount: Int = 0, totalSatsZapped: String? = null): String =
    when (this) {
        NotificationType.NEW_USER_FOLLOWED_YOU -> stringResource(
            id = R.string.notification_list_item_followed_you,
        )

        NotificationType.YOUR_POST_WAS_ZAPPED -> when (totalSatsZapped) {
            null -> stringResource(id = R.string.notification_list_item_zapped_your_post)
            else -> stringResource(
                id = R.string.notification_list_item_zapped_your_post_for_total_amount,
                totalSatsZapped,
            )
        }

        NotificationType.YOUR_POST_WAS_LIKED -> stringResource(
            id = R.string.notification_list_item_liked_your_post,
        )

        NotificationType.YOUR_POST_WAS_REPOSTED -> stringResource(
            id = R.string.notification_list_item_reposted_your_post,
        )

        NotificationType.YOUR_POST_WAS_REPLIED_TO -> stringResource(
            id = R.string.notification_list_item_replied_to_your_post,
        )

        NotificationType.YOU_WERE_MENTIONED_IN_POST -> stringResource(
            id = R.string.notification_list_item_mentioned_you_in_post,
        )

        NotificationType.YOUR_POST_WAS_MENTIONED_IN_POST -> stringResource(
            id = R.string.notification_list_item_mentioned_your_post,
        )

        NotificationType.POST_YOU_WERE_MENTIONED_IN_WAS_ZAPPED -> when (totalSatsZapped) {
            null -> stringResource(
                id = R.string.notification_list_item_post_you_were_mentioned_in_was_zapped,
            )

            else -> when (usersZappedCount) {
                1 -> stringResource(
                    id = R.string.notification_list_item_post_you_were_mentioned_in_was_zapped_for,
                    totalSatsZapped,
                )

                in 2..Int.MAX_VALUE -> stringResource(
                    id = R.string.notification_list_item_post_you_were_mentioned_in_was_zapped_for_total_amount,
                    totalSatsZapped,
                )

                else -> stringResource(
                    id = R.string.notification_list_item_post_you_were_mentioned_in_was_zapped,
                )
            }
        }

        NotificationType.POST_YOU_WERE_MENTIONED_IN_WAS_LIKED -> stringResource(
            id = R.string.notification_list_item_post_you_were_mentioned_in_was_liked,
        )

        NotificationType.POST_YOU_WERE_MENTIONED_IN_WAS_REPOSTED -> stringResource(
            id = R.string.notification_list_item_post_you_were_mentioned_in_was_reposted,
        )

        NotificationType.POST_YOU_WERE_MENTIONED_IN_WAS_REPLIED_TO -> stringResource(
            id = R.string.notification_list_item_post_you_were_mentioned_in_was_replied_to,
        )

        NotificationType.POST_YOUR_POST_WAS_MENTIONED_IN_WAS_ZAPPED -> when (totalSatsZapped) {
            null -> stringResource(
                id = R.string.notification_list_item_post_where_you_post_was_mentioned_was_zapped,
            )

            else -> when (usersZappedCount) {
                1 -> stringResource(
                    id = R.string.notification_list_item_post_where_you_post_was_mentioned_was_zapped_for,
                    totalSatsZapped,
                )

                in 2..Int.MAX_VALUE -> stringResource(
                    id = R.string.notification_list_item_post_where_you_post_was_mentioned_was_zapped_for_total_amount,
                    totalSatsZapped,
                )

                else -> stringResource(
                    id = R.string.notification_list_item_post_where_you_post_was_mentioned_was_zapped,
                )
            }
        }

        NotificationType.POST_YOUR_POST_WAS_MENTIONED_IN_WAS_LIKED -> stringResource(
            id = R.string.notification_list_item_post_where_you_post_was_mentioned_was_liked,
        )

        NotificationType.POST_YOUR_POST_WAS_MENTIONED_IN_WAS_REPOSTED -> stringResource(
            id = R.string.notification_list_item_post_where_you_post_was_mentioned_was_reposted,
        )

        NotificationType.POST_YOUR_POST_WAS_MENTIONED_IN_WAS_REPLIED_TO -> stringResource(
            id = R.string.notification_list_item_post_where_you_post_was_mentioned_was_replied_to,
        )

        // TODO Add support for 3xx notifications
        NotificationType.YOUR_POST_WAS_HIGHLIGHTED -> throw NotImplementedError()
        NotificationType.YOUR_POST_WAS_BOOKMARKED -> throw NotImplementedError()
    }

private val PreviewExamplePost = FeedPostUi(
    authorHandle = "alex",
    authorId = "",
    authorName = "Alex",
    content = "This is an example of #Nostr note.",
    hashtags = listOf("#Nostr"),
    postId = "",
    rawNostrEventJson = "",
    stats = EventStatsUi(),
    timestamp = Instant.now(),
)

private class NotificationsParameterProvider : PreviewParameterProvider<List<NotificationUi>> {
    override val values: Sequence<List<NotificationUi>>
        get() = sequenceOf(
            listOf(
                NotificationUi(
                    notificationId = "",
                    ownerId = "",
                    notificationType = NotificationType.NEW_USER_FOLLOWED_YOU,
                    createdAt = Instant.now(),
                    actionUserDisplayName = "alex",
                    actionUserInternetIdentifier = "alex@primal.net",
                    actionUserId = "",
                ),
            ),
            listOf(
                NotificationUi(
                    notificationId = "",
                    ownerId = "",
                    notificationType = NotificationType.NEW_USER_FOLLOWED_YOU,
                    createdAt = Instant.now(),
                    actionUserDisplayName = "miljan",
                    actionUserInternetIdentifier = "miljan@primal.net",
                    actionUserLegendaryCustomization = LegendaryCustomization(
                        avatarGlow = true,
                        legendaryStyle = LegendaryStyle.SUN_FIRE,
                    ),
                    actionUserId = "",
                ),
                NotificationUi(
                    notificationId = "",
                    ownerId = "",
                    notificationType = NotificationType.NEW_USER_FOLLOWED_YOU,
                    createdAt = Instant.now(),
                    actionUserDisplayName = "Alex",
                    actionUserInternetIdentifier = "alex@primal.net",
                    actionUserId = "",
                ),
            ),
            listOf(
                NotificationUi(
                    notificationId = "",
                    ownerId = "",
                    notificationType = NotificationType.YOUR_POST_WAS_ZAPPED,
                    createdAt = Instant.now(),
                    actionUserDisplayName = "Satoshi",
                    actionUserInternetIdentifier = "satoshi@primal.net",
                    actionUserId = "",
                    actionPost = PreviewExamplePost,
                    actionUserSatsZapped = 2121,
                ),
            ),
            listOf(
                NotificationUi(
                    notificationId = "",
                    ownerId = "",
                    notificationType = NotificationType.YOUR_POST_WAS_ZAPPED,
                    createdAt = Instant.now(),
                    actionUserDisplayName = "Satoshi Nakamoto",
                    actionUserInternetIdentifier = "satoshi@primal.net",
                    actionUserId = "",
                    actionPost = PreviewExamplePost,
                    actionUserSatsZapped = 2121,
                ),
                NotificationUi(
                    notificationId = "",
                    ownerId = "",
                    notificationType = NotificationType.YOUR_POST_WAS_ZAPPED,
                    createdAt = Instant.now(),
                    actionUserDisplayName = "Satoshi Nakamoto",
                    actionUserInternetIdentifier = "satoshi@primal.net",
                    actionUserId = "",
                    actionPost = PreviewExamplePost,
                    actionUserSatsZapped = 2121,
                ),
            ),
            listOf(
                NotificationUi(
                    notificationId = "",
                    ownerId = "",
                    notificationType = NotificationType.YOUR_POST_WAS_LIKED,
                    createdAt = Instant.now(),
                    actionUserDisplayName = "Satoshi",
                    actionUserInternetIdentifier = "satoshi@primal.net",
                    actionUserId = "",
                    actionPost = PreviewExamplePost,
                    actionUserSatsZapped = 2121,
                ),
            ),
            listOf(
                NotificationUi(
                    notificationId = "",
                    ownerId = "",
                    notificationType = NotificationType.YOUR_POST_WAS_REPOSTED,
                    createdAt = Instant.now(),
                    actionUserDisplayName = "Satoshi",
                    actionUserInternetIdentifier = "satoshi@primal.net",
                    actionUserId = "",
                    actionPost = PreviewExamplePost,
                    actionUserSatsZapped = 2121,
                ),
            ),
            listOf(
                NotificationUi(
                    notificationId = "",
                    ownerId = "",
                    notificationType = NotificationType.YOUR_POST_WAS_REPLIED_TO,
                    createdAt = Instant.now(),
                    actionUserDisplayName = "Satoshi Nakamoto",
                    actionUserInternetIdentifier = "satoshi@primal.net",
                    actionUserId = "",
                    actionPost = PreviewExamplePost,
                    actionUserSatsZapped = 2121,
                ),
            ),
            listOf(
                NotificationUi(
                    notificationId = "",
                    ownerId = "",
                    notificationType = NotificationType.YOU_WERE_MENTIONED_IN_POST,
                    createdAt = Instant.now(),
                    actionUserDisplayName = "Satoshi Nakamoto",
                    actionUserInternetIdentifier = "satoshi@primal.net",
                    actionUserId = "",
                    actionPost = PreviewExamplePost,
                    actionUserSatsZapped = 2121,
                ),
            ),
            listOf(
                NotificationUi(
                    notificationId = "",
                    ownerId = "",
                    notificationType = NotificationType.YOUR_POST_WAS_MENTIONED_IN_POST,
                    createdAt = Instant.now(),
                    actionUserDisplayName = "Satoshi Nakamoto",
                    actionUserInternetIdentifier = "satoshi@primal.net",
                    actionUserId = "",
                    actionPost = PreviewExamplePost,
                    actionUserSatsZapped = 2121,
                ),
            ),
        )
}

@Preview
@Composable
private fun PreviewUnseenNotificationsListItem(
    @PreviewParameter(NotificationsParameterProvider::class)
    notifications: List<NotificationUi>,
) {
    PrimalPreview(primalTheme = Sunset) {
        NotificationListItem(
            notifications = notifications,
            type = notifications.first().notificationType,
            isSeen = false,
            noteCallbacks = NoteCallbacks(),
        )
    }
}

@Preview
@Composable
private fun PreviewSeenNotificationsListItem(
    @PreviewParameter(NotificationsParameterProvider::class)
    notifications: List<NotificationUi>,
) {
    PrimalPreview(primalTheme = Sunset) {
        NotificationListItem(
            notifications = notifications,
            type = notifications.first().notificationType,
            isSeen = true,
            noteCallbacks = NoteCallbacks(),
        )
    }
}
