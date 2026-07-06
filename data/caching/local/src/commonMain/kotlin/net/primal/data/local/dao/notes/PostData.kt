package net.primal.data.local.dao.notes

import androidx.room3.Entity
import androidx.room3.PrimaryKey
import kotlinx.serialization.json.JsonArray

@Entity
data class PostData(
    @PrimaryKey
    val postId: String,
    val authorId: String,
    val createdAt: Long,
    val kind: Int = 1,
    val tags: List<JsonArray>,
    val content: String,
    val uris: List<String>,
    val hashtags: List<String>,
    val sig: String,
    val raw: String,
    val authorMetadataId: String? = null,
    val replyToPostId: String? = null,
    val replyToAuthorId: String? = null,
)
