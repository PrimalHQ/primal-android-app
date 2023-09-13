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
        parentColumn = "ownerId"
    )
    val owner: ProfileMetadata? = null,

    @Relation(
        entityColumn = "postId",
        parentColumn = "ownerPostId"
    )
    val ownerPost: PostData? = null,
    // Is any of my posts involved in user's action?

    @Relation(
        entityColumn = "ownerId",
        parentColumn = "actionByUserId"
    )
    val actionByUser: ProfileMetadata? = null,
    // Which user performed an action I'm being notified about?

    @Relation(
        entityColumn = "postId",
        parentColumn = "actionOnPostId"
    )
    val actionOnPostId: PostData? = null,
    // What is the id of the post action was performed to?

    @Relation(
        entityColumn = "postId",
        parentColumn = "replyPostId"
    )
    val replyPost: PostData? = null,
    // Was there a new post involved in this action?
)
