package net.primal.android.notifications.db

import androidx.room.Embedded
import androidx.room.Relation
import net.primal.android.feed.db.PostData
import net.primal.android.profile.db.ProfileMetadata

data class Notification(

    @Embedded
    val data: NotificationData,

    @Relation(
        entityColumn = "ownerId",
        parentColumn = "follower"
    )
    val follower: ProfileMetadata? = null,

    @Relation(
        entityColumn = "postId",
        parentColumn = "yourPost"
    )
    val yourPost: PostData? = null,

    @Relation(
        entityColumn = "ownerId",
        parentColumn = "whoLikedIt"
    )
    val whoLikedIt: ProfileMetadata? = null,

    @Relation(
        entityColumn = "ownerId",
        parentColumn = "whoRepostedIt"
    )
    val whoRepostedIt: ProfileMetadata? = null,

    @Relation(
        entityColumn = "ownerId",
        parentColumn = "whoZappedIt"
    )
    val whoZappedIt: ProfileMetadata? = null,

    @Relation(
        entityColumn = "ownerId",
        parentColumn = "whoRepliedToIt"
    )
    val whoRepliedToIt: ProfileMetadata? = null,

    @Relation(
        entityColumn = "postId",
        parentColumn = "reply"
    )
    val reply: PostData? = null,

    @Relation(
        entityColumn = "postId",
        parentColumn = "youWereMentionedIn"
    )
    val youWereMentionedIn: PostData? = null,

    @Relation(
        entityColumn = "postId",
        parentColumn = "yourPostWereMentionedIn"
    )
    val yourPostWereMentionedIn: PostData? = null,

    @Relation(
        entityColumn = "postId",
        parentColumn = "postYouWereMentionedIn"
    )
    val postYouWereMentionedIn: PostData? = null,

    @Relation(
        entityColumn = "postId",
        parentColumn = "postYourPostWasMentionedIn"
    )
    val postYourPostWasMentionedIn: PostData? = null

)
