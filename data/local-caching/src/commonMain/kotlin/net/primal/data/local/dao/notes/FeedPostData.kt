package net.primal.data.local.dao.notes

import kotlinx.serialization.json.JsonArray

data class FeedPostData(
    val postId: String,
    val authorId: String,
    val createdAt: Long,
    val content: String,
    val tags: List<JsonArray>,
    val raw: String,
    val feedCreatedAt: Long = createdAt,
    val hashtags: List<String> = emptyList(),
    val authorMetadataId: String? = null,
    val repostId: String? = null,
    val repostAuthorId: String? = null,
    val repostTimestamp: Long? = null,
    val isAuthorMuted: Boolean? = false,
    val isThreadMuted: Boolean? = false,
    val replyToPostId: String? = null,
    val replyToAuthorId: String? = null,
)
