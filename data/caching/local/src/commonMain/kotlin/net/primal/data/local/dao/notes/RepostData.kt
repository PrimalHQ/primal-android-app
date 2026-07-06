package net.primal.data.local.dao.notes

import androidx.room3.Entity
import androidx.room3.Index
import androidx.room3.PrimaryKey
import kotlinx.serialization.json.JsonArray

@Entity(
    indices = [
        Index(value = ["postId"]),
    ],
)
data class RepostData(
    @PrimaryKey
    val repostId: String,
    val authorId: String,
    val createdAt: Long,
    val tags: List<JsonArray>,
    val postId: String,
    val postAuthorId: String,
    val sig: String,
)
