package net.primal.android.notifications.db

import androidx.room.Entity
import net.primal.android.notifications.domain.NotificationType

@Entity(
    primaryKeys = ["ownerId", "createdAt", "type"],
)
data class NotificationData(
    val ownerId: String,
    val createdAt: Long,
    val type: NotificationType,
    val actionByUserId: String, // Which user performed an action I'm being notified about?
    val ownerPostId: String? = null, // Is any of my posts involved in user's action?
    val actionOnPostId: String? = null, // What is the id of the post action was performed to?
    val replyPostId: String? = null, // Was there a new post involved in this action?
    val satsZapped: Int? = null,
//    val follower: String? = null,
//    val yourPost: String? = null,
//    val whoLikedIt: String? = null,
//    val whoRepostedIt: String? = null,
//    val whoZappedIt: String? = null,
//    val satsZapped: Int? = null,
//    val whoRepliedToIt: String? = null,
//    val reply: String? = null,
//    val youWereMentionedIn: String? = null,
//    val yourPostWereMentionedIn: String? = null,
//    val postYouWereMentionedIn: String? = null,
//    val postYourPostWasMentionedIn: String? = null,
)
