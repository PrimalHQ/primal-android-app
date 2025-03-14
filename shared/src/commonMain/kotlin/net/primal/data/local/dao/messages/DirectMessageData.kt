package net.primal.data.local.dao.messages

import androidx.room.Entity

@Entity(primaryKeys = ["messageId", "ownerId"])
data class DirectMessageData(
    val messageId: String,
    val ownerId: String,
    val senderId: String,
    val receiverId: String,
    val participantId: String,
    val createdAt: Long,
    val content: String,
    val uris: List<String>,
    val hashtags: List<String>,
)
