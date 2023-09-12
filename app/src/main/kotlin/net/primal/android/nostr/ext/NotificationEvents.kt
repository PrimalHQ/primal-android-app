package net.primal.android.nostr.ext

import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import net.primal.android.nostr.model.primal.PrimalEvent
import net.primal.android.nostr.model.primal.content.ContentPrimalNotification
import net.primal.android.notifications.db.Notification
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

fun ContentPrimalNotification.asNotificationPOOrNull(): Notification? {
    val type = NotificationType.valueOf(type = this.type) ?: return null
    return Notification(
        userId = this.pubkey,
        createdAt = this.createdAt,
        type = type,
        follower = this.follower,
        yourPost = this.yourPost,
        whoLikedIt = this.whoLikedIt,
        whoRepostedIt = this.whoRepostedIt,
        whoZappedIt = this.whoZappedIt,
        satsZapped = this.satsZapped,
        whoRepliedToIt = this.whoRepliedToIt,
        reply = this.reply,
        youWereMentionedIn = this.youWereMentionedIn,
        yourPostWereMentionedIn = this.yourPostWereMentionedIn,
        postYouWereMentionedIn = this.postYouWereMentionedIn,
        postYourPostWasMentionedIn = this.postYourPostWasMentionedIn,
    )
}

fun List<PrimalEvent>.mapNotNullAsNotificationPO() =
    this.mapNotNull { NostrJson.decodeFromStringOrNull<ContentPrimalNotification>(it.content) }
        .mapNotNull { it.asNotificationPOOrNull() }