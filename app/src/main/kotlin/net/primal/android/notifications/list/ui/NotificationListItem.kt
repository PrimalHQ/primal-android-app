package net.primal.android.notifications.list.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import net.primal.android.R
import net.primal.android.core.compose.AvatarThumbnailsRow
import net.primal.android.core.compose.NostrUserText
import net.primal.android.core.compose.feed.model.FeedPostAction
import net.primal.android.core.compose.feed.model.FeedPostUi
import net.primal.android.core.compose.feed.model.toNoteContentUi
import net.primal.android.core.compose.feed.note.FeedNoteStatsRow
import net.primal.android.core.compose.feed.note.NoteContent
import net.primal.android.core.compose.notifications.toImagePainter
import net.primal.android.core.ext.openUriSafely
import net.primal.android.core.utils.shortened
import net.primal.android.notifications.domain.NotificationType
import net.primal.android.theme.AppTheme

@Composable
fun NotificationListItem(
    notifications: List<NotificationUi>,
    type: NotificationType,
    walletConnected: Boolean,
    onProfileClick: (String) -> Unit,
    onNoteClick: (String) -> Unit,
    onHashtagClick: (String) -> Unit,
    onMediaClick: (String, String) -> Unit,
    onReplyClick: (String) -> Unit,
    onPostLikeClick: (FeedPostUi) -> Unit,
    onRepostClick: (FeedPostUi) -> Unit,
    onDefaultZapClick: (FeedPostUi) -> Unit,
    onZapOptionsClick: (FeedPostUi) -> Unit,
    onWalletUnavailable: () -> Unit,
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
        imagePainter = type.toImagePainter(),
        suffixText = type.toSuffixText(
            usersZappedCount = notifications.size,
            totalSatsZapped = when (notifications.size) {
                1 -> when (type) {
                    NotificationType.YOUR_POST_WAS_ZAPPED -> postTotalSatsZapped?.shortened()
                    else -> activeUsersTotalSatsZapped?.shortened()
                }

                else -> postTotalSatsZapped?.shortened()
            },
        ),
        onProfileClick = onProfileClick,
        onPostClick = onNoteClick,
        onHashtagClick = onHashtagClick,
        onMediaClick = onMediaClick,
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
private fun NotificationListItem(
    notifications: List<NotificationUi>,
    imagePainter: Painter,
    suffixText: String,
    onProfileClick: (String) -> Unit,
    onPostClick: (String) -> Unit,
    onHashtagClick: (String) -> Unit,
    onMediaClick: (String, String) -> Unit,
    onPostAction: (FeedPostAction) -> Unit,
    onPostLongPressAction: (FeedPostAction) -> Unit,
) {
    val firstNotification = notifications.first()

    Row(
        modifier = Modifier
            .background(color = AppTheme.colorScheme.surfaceVariant)
            .clickable(
                enabled = notifications.size == 1 || firstNotification.actionPost != null,
                onClick = {
                    when (notifications.size) {
                        1 -> when (firstNotification.notificationType) {
                            NotificationType.NEW_USER_FOLLOWED_YOU -> {
                                firstNotification.actionUserId?.let(onProfileClick)
                            }

                            else -> firstNotification.actionPost?.postId?.let(onPostClick)
                        }

                        else -> firstNotification.actionPost?.postId?.let(onPostClick)
                    }
                },
            ),
    ) {
        Box(
            modifier = Modifier.padding(all = 16.dp),
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Image(
                    modifier = Modifier
                        .padding(top = 2.dp)
                        .size(28.dp),
                    painter = imagePainter,
                    contentDescription = null,
                )

                val extraStat = when (firstNotification.notificationType) {
                    NotificationType.YOUR_POST_WAS_ZAPPED -> notifications.mapNotNull { it.actionUserSatsZapped }
                        .sum()

                    NotificationType.YOUR_POST_WAS_LIKED -> firstNotification.actionPost?.stats?.likesCount
                    NotificationType.YOUR_POST_WAS_REPOSTED -> firstNotification.actionPost?.stats?.repostsCount
                    NotificationType.YOUR_POST_WAS_REPLIED_TO -> firstNotification.actionPost?.stats?.repliesCount
                    else -> null
                }

                if (extraStat != null && extraStat > 0) {
                    Text(
                        modifier = Modifier.padding(vertical = 4.dp),
                        text = extraStat.shortened(),
                        style = AppTheme.typography.bodySmall,
                        color = when (firstNotification.notificationType) {
                            NotificationType.YOUR_POST_WAS_ZAPPED -> AppTheme.extraColorScheme.zapped
                            NotificationType.YOUR_POST_WAS_LIKED -> AppTheme.extraColorScheme.liked
                            NotificationType.YOUR_POST_WAS_REPOSTED -> AppTheme.extraColorScheme.reposted
                            NotificationType.YOUR_POST_WAS_REPLIED_TO -> AppTheme.extraColorScheme.replied
                            else -> Color.Unspecified
                        },
                    )
                }
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp),
        ) {
            val actionPost = firstNotification.actionPost

            Column {
                AvatarThumbnailsRow(
                    avatarCdnImages = notifications.map { it.actionUserAvatarCdnImage },
                    overlapAvatars = false,
                    hasAvatarBorder = false,
                    onClick = { index ->
                        notifications.getOrNull(index)?.actionUserId?.let(onProfileClick)
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
                        .padding(vertical = 8.dp)
                        .padding(end = 16.dp),
                    style = AppTheme.typography.bodyLarge.copy(
                        color = AppTheme.extraColorScheme.onSurfaceVariantAlt1,
                    ),
                    maxLines = 2,
                    displayName = firstNotification.actionUserDisplayName ?: "undefined",
                    displayNameColor = AppTheme.extraColorScheme.onSurfaceVariantAlt1,
                    internetIdentifier = firstNotification.actionUserInternetIdentifier,
                    annotatedStringSuffixBuilder = {
                        if (notifications.size > 1) append(" $andOthersText")
                        append(" $suffixText")
                    },
                )

                val localUriHandler = LocalUriHandler.current

                if (actionPost != null) {
                    NoteContent(
                        modifier = Modifier.padding(end = 16.dp),
                        data = actionPost.toNoteContentUi(),
                        expanded = false,
                        onClick = { onPostClick(actionPost.postId) },
                        onProfileClick = onProfileClick,
                        onPostClick = onPostClick,
                        onUrlClick = { localUriHandler.openUriSafely(it) },
                        onHashtagClick = { onHashtagClick(it) },
                        onMediaClick = onMediaClick,
                    )

                    FeedNoteStatsRow(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp)
                            .padding(end = 16.dp),
                        postStats = actionPost.stats,
                        onPostAction = onPostAction,
                        onPostLongPressAction = onPostLongPressAction,
                    )
                } else {
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
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
    }
