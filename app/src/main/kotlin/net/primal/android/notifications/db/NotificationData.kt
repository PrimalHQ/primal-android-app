package net.primal.android.notifications.db

import androidx.room.Entity
import net.primal.android.notifications.domain.NotificationType

@Entity(
    primaryKeys = ["userId", "createdAt", "type"],
)
data class NotificationData(
    val userId: String,
    val createdAt: Long,
    val type: NotificationType,
    val follower: String? = null,
    val yourPost: String? = null,
    val whoLikedIt: String? = null,
    val whoRepostedIt: String? = null,
    val whoZappedIt: String? = null,
    val satsZapped: Int? = null,
    val whoRepliedToIt: String? = null,
    val reply: String? = null,
    val youWereMentionedIn: String? = null,
    val yourPostWereMentionedIn: String? = null,
    val postYouWereMentionedIn: String? = null,
    val postYourPostWasMentionedIn: String? = null,
)
