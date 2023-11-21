package net.primal.android.feed.db

import androidx.room.Embedded
import androidx.room.Relation
import net.primal.android.attachments.db.NoteAttachment
import net.primal.android.attachments.db.NoteNostrUri
import net.primal.android.profile.db.ProfileData

data class FeedPost(

    @Embedded
    val data: FeedPostData,

    @Relation(
        entityColumn = "eventId",
        parentColumn = "postId",
    )
    val attachments: List<NoteAttachment>,

    @Relation(
        entityColumn = "postId",
        parentColumn = "postId",
    )
    val nostrUris: List<NoteNostrUri>,

    @Relation(
        entityColumn = "ownerId",
        parentColumn = "authorId",
    )
    val author: ProfileData? = null,

    @Relation(
        entityColumn = "postId",
        parentColumn = "postId",
    )
    val postStats: PostStats? = null,

    @Embedded
    val userStats: FeedPostUserStats? = null,

    @Relation(
        entityColumn = "ownerId",
        parentColumn = "repostAuthorId",
    )
    val repostAuthor: ProfileData? = null,
)
