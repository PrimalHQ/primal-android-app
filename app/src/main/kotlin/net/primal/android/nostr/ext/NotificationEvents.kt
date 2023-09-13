package net.primal.android.nostr.ext

import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import net.primal.android.nostr.model.primal.PrimalEvent
import net.primal.android.nostr.model.primal.content.ContentPrimalNotification
import net.primal.android.notifications.db.NotificationData
import net.primal.android.notifications.domain.NotificationType
import net.primal.android.notifications.domain.NotificationsSummary
import net.primal.android.serialization.NostrJson
import net.primal.android.serialization.decodeFromStringOrNull

fun PrimalEvent.asNotificationSummary(): NotificationsSummary {
    val summaryJsonObject = NostrJson.parseToJsonElement(this.content).jsonObject
    val pairs = NotificationType.values().mapNotNull {
        val count = summaryJsonObject[it.type.toString()]?.jsonPrimitive?.contentOrNull?.toInt()
        if (count != null) it to count else null
    }
    return NotificationsSummary(
        count = pairs.sumOf { it.second },
        countPerType = pairs.toMap(),
    )
}

private fun ContentPrimalNotification.parseActionByUserId(type: NotificationType): String? {
    return when (type) {
        NotificationType.NEW_USER_FOLLOWED_YOU -> this.follower
        NotificationType.USER_UNFOLLOWED_YOU -> this.follower
        NotificationType.YOUR_POST_WAS_ZAPPED -> this.whoZappedIt
        NotificationType.YOUR_POST_WAS_LIKED -> this.whoLikedIt
        NotificationType.YOUR_POST_WAS_REPOSTED -> this.whoRepostedIt
        NotificationType.YOUR_POST_WAS_REPLIED_TO -> this.whoRepliedToIt
        NotificationType.YOU_WERE_MENTIONED_IN_POST -> null // TODO Waiting on backend
        NotificationType.YOUR_POST_WAS_MENTIONED_IN_POST -> null // TODO Waiting on backend
        NotificationType.POST_YOU_WERE_MENTIONED_IN_WAS_ZAPPED -> this.whoZappedIt
        NotificationType.POST_YOU_WERE_MENTIONED_IN_WAS_LIKED -> this.whoLikedIt
        NotificationType.POST_YOU_WERE_MENTIONED_IN_WAS_REPOSTED -> this.whoRepostedIt
        NotificationType.POST_YOU_WERE_MENTIONED_IN_WAS_REPLIED_TO -> this.whoRepliedToIt
        NotificationType.POST_YOUR_POST_WAS_MENTIONED_IN_WAS_ZAPPED -> this.whoZappedIt
        NotificationType.POST_YOUR_POST_WAS_MENTIONED_IN_WAS_LIKED -> this.whoLikedIt
        NotificationType.POST_YOUR_POST_WAS_MENTIONED_IN_WAS_REPOSTED -> this.whoRepostedIt
        NotificationType.POST_YOUR_POST_WAS_MENTIONED_IN_WAS_REPLIED_TO -> this.whoRepliedToIt
    }
}

private fun ContentPrimalNotification.parseActionOnPostId(type: NotificationType): String? {
    return when (type) {
        NotificationType.YOUR_POST_WAS_ZAPPED -> this.yourPost
        NotificationType.YOUR_POST_WAS_LIKED -> this.yourPost
        NotificationType.YOUR_POST_WAS_REPOSTED -> this.yourPost
        NotificationType.YOUR_POST_WAS_REPLIED_TO -> this.yourPost
        NotificationType.YOU_WERE_MENTIONED_IN_POST -> this.youWereMentionedIn
        NotificationType.YOUR_POST_WAS_MENTIONED_IN_POST -> this.yourPostWereMentionedIn
        NotificationType.POST_YOU_WERE_MENTIONED_IN_WAS_ZAPPED -> this.postYouWereMentionedIn
        NotificationType.POST_YOU_WERE_MENTIONED_IN_WAS_LIKED -> this.postYouWereMentionedIn
        NotificationType.POST_YOU_WERE_MENTIONED_IN_WAS_REPOSTED -> this.postYouWereMentionedIn
        NotificationType.POST_YOU_WERE_MENTIONED_IN_WAS_REPLIED_TO -> this.postYouWereMentionedIn
        NotificationType.POST_YOUR_POST_WAS_MENTIONED_IN_WAS_ZAPPED -> this.postYourPostWasMentionedIn
        NotificationType.POST_YOUR_POST_WAS_MENTIONED_IN_WAS_LIKED -> this.postYourPostWasMentionedIn
        NotificationType.POST_YOUR_POST_WAS_MENTIONED_IN_WAS_REPOSTED -> this.postYourPostWasMentionedIn
        NotificationType.POST_YOUR_POST_WAS_MENTIONED_IN_WAS_REPLIED_TO -> this.postYourPostWasMentionedIn
        else -> null
    }
}

fun ContentPrimalNotification.asNotificationPOOrNull(): NotificationData? {
    val type = NotificationType.valueOf(type = this.type) ?: return null
    val actionByUserId = parseActionByUserId(type = type) ?: return null
    val actionOnPostId = parseActionOnPostId(type = type)

    return NotificationData(
        ownerId = this.pubkey,
        createdAt = this.createdAt,
        type = type,
        actionByUserId = actionByUserId,
        replyPostId = this.reply,
        ownerPostId = this.yourPost,
        actionOnPostId = actionOnPostId,
        satsZapped = this.satsZapped,
    )
}

fun List<PrimalEvent>.mapNotNullAsNotificationPO() =
    this.mapNotNull { NostrJson.decodeFromStringOrNull<ContentPrimalNotification>(it.content) }
        .mapNotNull { it.asNotificationPOOrNull() }