package net.primal.android.notifications.list.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.unit.dp
import net.primal.android.R
import net.primal.android.core.compose.AvatarThumbnailsRow
import net.primal.android.core.compose.NostrUserText
import net.primal.android.core.utils.asEllipsizedNpub
import net.primal.android.notifications.db.Notification
import net.primal.android.profile.db.authorNameUiFriendly

@Composable
fun MasterNotificationListItem(
    notifications: List<Notification>,
    imagePainter: Painter,
    suffixText: String,
    onProfileClick: (String) -> Unit,
    onPostClick: (String) -> Unit,
) {
    NotificationListItem(
        iconPainter = imagePainter,
    ) {
        val firstNotification = notifications.first()

        val firstFollower = firstNotification.owner

        Column {
            AvatarThumbnailsRow(
                avatarUrls = notifications.map { it.owner?.picture },
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
                    ?: firstNotification.data.actionByUserId.asEllipsizedNpub(),
                internetIdentifier = firstFollower?.internetIdentifier,
                annotatedStringSuffixBuilder = {
                    if (notifications.size > 1) append(" $andOthersText")
                    append(" $suffixText")
                }
            )

//            FeedPostContent(
//                content = data.content,
//                expanded = expanded,
//                hashtags = data.hashtags,
//                mediaResources = data.mediaResources,
//                nostrResources = data.nostrResources,
//                onClick = {
//                    launchRippleEffect(it)
//                    onPostClick(data.postId)
//                },
//                onProfileClick = onProfileClick,
//                onPostClick = onPostClick,
//                onUrlClick = {
//                    localUriHandler.openUriSafely(it)
//                },
//                onHashtagClick = onHashtagClick,
//            )
//
//            FeedPostStatsRow(
//                postStats = data.stats,
//                onPostAction = {}, //onPostAction,
//                onPostLongPressAction = {}, // onPostLongClickAction,
//            )
        }
    }
}
