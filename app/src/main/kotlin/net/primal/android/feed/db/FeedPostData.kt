package net.primal.android.feed.db

import androidx.room.DatabaseView

@DatabaseView(
    """
    SELECT 
        PostData.postId,
        PostData.authorId,
        PostData.createdAt,
        PostData.content,
        PostData.urls,
        PostData.referencePostId,
        PostData.referencePostAuthorId,
        RepostData.repostId AS repostId,
        RepostData.authorId AS repostAuthorId,
        COALESCE(RepostData.createdAt, PostData.createdAt) AS feedCreatedAt
    FROM PostData
    LEFT JOIN RepostData ON RepostData.postId = PostData.postId
    """
)
data class FeedPostData(
    val postId: String,
    val authorId: String,
    val createdAt: Long,
    val content: String,
    val urls: List<String>,
    val referencePostId: String? = null,
    val referencePostAuthorId: String? = null,
    val repostId: String? = null,
    val repostAuthorId: String? = null,
    val feedCreatedAt: Long,
)
