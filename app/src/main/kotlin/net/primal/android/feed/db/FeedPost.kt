package net.primal.android.feed.db

import androidx.room.Embedded
import androidx.room.Relation
import net.primal.android.profile.db.ProfileMetadata

data class FeedPost(

    @Embedded
    val data: FeedPostData,

    @Relation(
        entityColumn = "postId",
        parentColumn = "postId",
    )
    val resources: List<PostResource>,

    @Relation(
        entityColumn = "ownerId",
        parentColumn = "authorId",
    )
    val author: ProfileMetadata? = null,

    @Relation(
        entityColumn = "postId",
        parentColumn = "referencePostId",
    )
    val referencedPost: PostData? = null,

    @Relation(
        entityColumn = "postId",
        parentColumn = "referencePostId",
    )
    val referencedPostResources: List<PostResource>,

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


