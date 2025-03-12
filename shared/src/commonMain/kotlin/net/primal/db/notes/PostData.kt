package net.primal.db.notes

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.json.JsonArray

@Entity
data class PostData(
    @PrimaryKey
    val postId: String,
    val authorId: String,
    val createdAt: Long,
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
