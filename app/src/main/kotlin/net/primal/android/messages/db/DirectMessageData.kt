package net.primal.android.messages.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class DirectMessageData(
    @PrimaryKey
    val messageId: String,
    val senderId: String,
    val receiverId: String,
    val participantId: String,
    val createdAt: Long,
    val content: String,
    val uris: List<String>,
    val hashtags: List<String>,
)
