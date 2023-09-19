package net.primal.android.notifications.list.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.unit.dp
import net.primal.android.R
import net.primal.android.core.compose.AvatarThumbnailsRow
import net.primal.android.core.compose.NostrUserText
import net.primal.android.core.compose.feed.FeedPostContent
import net.primal.android.core.compose.feed.FeedPostStatsRow
import net.primal.android.core.utils.shortened
import net.primal.android.notifications.domain.NotificationType
import net.primal.android.theme.AppTheme
import net.primal.android.theme.colors.LikeColor
import net.primal.android.theme.colors.ReplyDarkColor
import net.primal.android.theme.colors.ReplyLightColor
import net.primal.android.theme.colors.RepostColor
import net.primal.android.theme.colors.ZapColor

@Composable
fun NotificationListItem(
    notifications: List<NotificationUi>,
    imagePainter: Painter,
    suffixText: String,
    onProfileClick: (String) -> Unit,
    onPostClick: (String) -> Unit,
) {
    val firstNotification = notifications.first()

    Row {
        Box(
            modifier = Modifier.padding(all = 16.dp),
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Image(
                    modifier = Modifier.padding(top = 8.dp),
                    painter = imagePainter,
                    contentDescription = null,
                )

                val extraStat = when (firstNotification.notificationType) {
                    NotificationType.YOUR_POST_WAS_ZAPPED -> notifications.mapNotNull { it.actionUserSatsZapped }.sum()
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
                            NotificationType.YOUR_POST_WAS_ZAPPED -> ZapColor
                            NotificationType.YOUR_POST_WAS_LIKED -> LikeColor
                            NotificationType.YOUR_POST_WAS_REPOSTED -> RepostColor
                            NotificationType.YOUR_POST_WAS_REPLIED_TO -> if (isSystemInDarkTheme()) ReplyDarkColor else ReplyLightColor
                            else -> Color.Unspecified
                        }
                    )
                }
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp)
        ) {
            val actionPost = firstNotification.actionPost

            Column {
                AvatarThumbnailsRow(
                    avatarUrls = notifications.map { it.actionUserPicture },
                    authorInternetIdentifiers = notifications.map { null },
                    onClick = {
                        firstNotification.actionUserId?.let(onPostClick)
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
                        .padding(end = 8.dp),
                    style = AppTheme.typography.bodyLarge.copy(
                        color = AppTheme.extraColorScheme.onSurfaceVariantAlt1,
                    ),
                    displayName = firstNotification.actionUserDisplayName ?: "undefined",
                    displayNameColor = AppTheme.extraColorScheme.onSurfaceVariantAlt1,
                    internetIdentifier = firstNotification.actionUserInternetIdentifier,
                    annotatedStringSuffixBuilder = {
                        if (notifications.size > 1) append(" $andOthersText")
                        append(" $suffixText")
                    }
                )

                if (actionPost != null) {
                    FeedPostContent(
                        modifier = Modifier.padding(end = 16.dp),
                        content = actionPost.content,
                        expanded = false,
                        hashtags = actionPost.hashtags,
                        mediaResources = actionPost.mediaResources,
                        nostrResources = actionPost.nostrResources,
                        onClick = {
                            //TODO launchRippleEffect(it)
                            onPostClick(actionPost.postId)
                        },
                        onProfileClick = onProfileClick,
                        onPostClick = onPostClick,
                        onUrlClick = {
                            //TODO localUriHandler.openUriSafely(it)
                        },
                        onHashtagClick = {
                            //TODO Handle hashtag clicks
                        },
                    )

                    FeedPostStatsRow(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp)
                            .padding(end = 16.dp),
                        postStats = actionPost.stats,
                        onPostAction = {
                            // TODO onPostAction,
                        },
                        onPostLongPressAction = {
                            // TODO onPostLongClickAction,
                        },
                    )
                } else {
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}
