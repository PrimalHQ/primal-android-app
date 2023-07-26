package net.primal.android.feed.db

data class FeedPostData(
    val postId: String,
    val authorId: String,
    val createdAt: Long,
    val content: String,
    val raw: String,
    val feedCreatedAt: Long = createdAt,
    val authorMetadataId: String? = null,
    val referencePostId: String? = null,
    val referencePostAuthorId: String? = null,
    val repostId: String? = null,
    val repostAuthorId: String? = null,
)
