 package net.primal.android.feed.db

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.json.JsonArray

@Entity
data class ShortTextNote(
    @PrimaryKey
    val eventId: String,
    val authorId: String,
    val createdAt: Long,
    val tags: List<JsonArray>,
    val content: String,
    val sig: String,
)
