package net.primal.android.feed.db

import androidx.room.Embedded
import androidx.room.Relation
import net.primal.android.profile.db.ProfileMetadata

data class FeedPost(

    @Embedded
    val data: FeedPostData,

    @Relation(
        entityColumn = "ownerId",
        parentColumn = "authorId",
    )
    val author: ProfileMetadata,

    @Relation(
        entityColumn = "postId",
        parentColumn = "referencePostId",
    )
    val referencedPost: PostData? = null,

    @Relation(
        entityColumn = "ownerId",
        parentColumn = "referencePostAuthorId",
    )
    val referencedPostAuthor: ProfileMetadata? = null,

    @Relation(
        entityColumn = "ownerId",
        parentColumn = "repostAuthorId",
    )
    val repostAuthor: ProfileMetadata? = null,

    @Relation(
        entityColumn = "postId",
        parentColumn = "postId",
    )
    val postStats: PostStats? = null

)


