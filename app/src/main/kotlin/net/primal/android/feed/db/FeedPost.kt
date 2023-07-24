package net.primal.android.feed.db

import androidx.room.Embedded
import androidx.room.Relation
import net.primal.android.profile.db.ProfileMetadata

data class FeedPost(

    @Embedded
    val data: FeedPostData,

    @Relation(
        entityColumn = "eventId",
        parentColumn = "postId",
    )
    val postResources: List<MediaResource>,

    @Relation(
        entityColumn = "eventId",
        parentColumn = "postId",
    )
    val nostrUris: List<NostrUri>,

    @Relation(
        entityColumn = "ownerId",
        parentColumn = "authorId",
    )
    val author: ProfileMetadata? = null,

    @Relation(
        entityColumn = "eventId",
        parentColumn = "authorMetadataId",
    )
    val authorResources: List<MediaResource>,

    @Relation(
        entityColumn = "postId",
        parentColumn = "referencePostId",
    )
    val referencedPost: PostData? = null,

    @Relation(
        entityColumn = "eventId",
        parentColumn = "referencePostId",
    )
    val referencedPostResources: List<MediaResource>,

    @Relation(
        entityColumn = "ownerId",
        parentColumn = "referencePostAuthorId",
    )
    val referencedPostAuthor: ProfileMetadata? = null,

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
    val repostAuthor: ProfileMetadata? = null,
)
