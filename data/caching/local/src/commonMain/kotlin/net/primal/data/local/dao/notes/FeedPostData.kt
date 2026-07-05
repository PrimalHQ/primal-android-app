package net.primal.data.local.dao.notes

import kotlinx.serialization.json.JsonArray

data class FeedPostData(
    val postId: String,
    val authorId: String,
    val createdAt: Long,
    val kind: Int,
    val content: String,
    val raw: String,
    val tags: List<JsonArray>? = null,
    val hashtags: List<String> = emptyList(),
    val authorMetadataId: String? = null,
    val repostId: String? = null,
    val repostAuthorId: String? = null,
    val repostCreatedAt: Long? = null,
    val isAuthorMuted: Boolean? = false,
    val isThreadMuted: Boolean? = false,
    val replyToPostId: String? = null,
    val replyToAuthorId: String? = null,
)
