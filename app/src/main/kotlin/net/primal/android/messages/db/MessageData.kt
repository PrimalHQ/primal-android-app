package net.primal.android.messages.db

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.json.JsonArray

@Entity
data class MessageData(
    @PrimaryKey
    val messageId: String,
    val senderId: String,
    val receiverId: String,
    val participantId: String,
    val createdAt: Long,
    val tags: List<JsonArray>,
    val content: String,
    val uris: List<String>,
    val hashtags: List<String>,
)
