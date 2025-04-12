package net.primal.data.repository.mappers.remote

import net.primal.core.utils.serialization.decodeFromJsonStringOrNull
import net.primal.data.local.dao.notifications.NotificationData
import net.primal.domain.common.PrimalEvent
import net.primal.domain.notifications.NotificationType

private fun ContentPrimalNotification.parseActionUserId(type: NotificationType): String? {
    return when (type) {
        NotificationType.NEW_USER_FOLLOWED_YOU -> this.follower
        NotificationType.YOUR_POST_WAS_ZAPPED -> this.whoZappedIt
        NotificationType.YOUR_POST_WAS_LIKED -> this.whoLikedIt
        NotificationType.YOUR_POST_WAS_REPOSTED -> this.whoRepostedIt
        NotificationType.YOUR_POST_WAS_REPLIED_TO -> this.whoRepliedToIt
        NotificationType.YOU_WERE_MENTIONED_IN_POST -> this.youWereMentionedBy
        NotificationType.YOUR_POST_WAS_MENTIONED_IN_POST -> this.yourPostWasMentionedBy
        NotificationType.POST_YOU_WERE_MENTIONED_IN_WAS_ZAPPED -> this.whoZappedIt
        NotificationType.POST_YOU_WERE_MENTIONED_IN_WAS_LIKED -> this.whoLikedIt
        NotificationType.POST_YOU_WERE_MENTIONED_IN_WAS_REPOSTED -> this.whoRepostedIt
        NotificationType.POST_YOU_WERE_MENTIONED_IN_WAS_REPLIED_TO -> this.whoRepliedToIt
        NotificationType.POST_YOUR_POST_WAS_MENTIONED_IN_WAS_ZAPPED -> this.whoZappedIt
        NotificationType.POST_YOUR_POST_WAS_MENTIONED_IN_WAS_LIKED -> this.whoLikedIt
        NotificationType.POST_YOUR_POST_WAS_MENTIONED_IN_WAS_REPOSTED -> this.whoRepostedIt
        NotificationType.POST_YOUR_POST_WAS_MENTIONED_IN_WAS_REPLIED_TO -> this.whoRepliedToIt
        NotificationType.YOUR_POST_WAS_HIGHLIGHTED -> this.whoHighlightedIt
        NotificationType.YOUR_POST_WAS_BOOKMARKED -> this.whoBookmarkedIt
    }
}

private fun ContentPrimalNotification.parseActionPostId(type: NotificationType): String? {
    return when (type) {
        NotificationType.YOUR_POST_WAS_ZAPPED -> this.yourPost
        NotificationType.YOUR_POST_WAS_LIKED -> this.yourPost
        NotificationType.YOUR_POST_WAS_REPOSTED -> this.yourPost
        NotificationType.YOUR_POST_WAS_REPLIED_TO -> this.reply
        NotificationType.YOU_WERE_MENTIONED_IN_POST -> this.youWereMentionedIn
        NotificationType.YOUR_POST_WAS_MENTIONED_IN_POST -> this.yourPostWereMentionedIn
        NotificationType.POST_YOU_WERE_MENTIONED_IN_WAS_ZAPPED -> this.postYouWereMentionedIn
        NotificationType.POST_YOU_WERE_MENTIONED_IN_WAS_LIKED -> this.postYouWereMentionedIn
        NotificationType.POST_YOU_WERE_MENTIONED_IN_WAS_REPOSTED -> this.postYouWereMentionedIn
        NotificationType.POST_YOU_WERE_MENTIONED_IN_WAS_REPLIED_TO -> this.reply
        NotificationType.POST_YOUR_POST_WAS_MENTIONED_IN_WAS_ZAPPED -> this.postYourPostWasMentionedIn
        NotificationType.POST_YOUR_POST_WAS_MENTIONED_IN_WAS_LIKED -> this.postYourPostWasMentionedIn
        NotificationType.POST_YOUR_POST_WAS_MENTIONED_IN_WAS_REPOSTED -> this.postYourPostWasMentionedIn
        NotificationType.POST_YOUR_POST_WAS_MENTIONED_IN_WAS_REPLIED_TO -> this.reply
        NotificationType.YOUR_POST_WAS_HIGHLIGHTED -> this.yourPost
        NotificationType.YOUR_POST_WAS_BOOKMARKED -> this.yourPost
        else -> null
    }
}

fun ContentPrimalNotification.asNotificationPOOrNull(): NotificationData? {
    val type = NotificationType.valueOf(type = this.type) ?: return null
    val actionUserId = parseActionUserId(type = type) ?: return null
    val actionOnPostId = parseActionPostId(type = type)

    return NotificationData(
        notificationId = this.id,
        ownerId = this.pubkey,
        createdAt = this.createdAt,
        type = type,
        actionUserId = actionUserId,
        actionPostId = actionOnPostId,
        satsZapped = this.satsZapped,
    )
}

fun List<PrimalEvent>.mapNotNullAsNotificationPO() =
    this.mapNotNull { it.content.decodeFromJsonStringOrNull<ContentPrimalNotification>() }
        .mapNotNull { it.asNotificationPOOrNull() }
