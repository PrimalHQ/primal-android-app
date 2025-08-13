package net.primal.data.local.dao.messages

import androidx.room.Entity
import net.primal.shared.data.local.encryption.Encryptable

@Entity(primaryKeys = ["messageId", "ownerId"])
data class DirectMessageData(
    val messageId: String,
    val ownerId: String,
    val senderId: String,
    val receiverId: String,
    val participantId: String,
    val createdAt: Long,
    val content: Encryptable<String>,
    val uris: Encryptable<List<String>>,
    val hashtags: Encryptable<List<String>>,
)
