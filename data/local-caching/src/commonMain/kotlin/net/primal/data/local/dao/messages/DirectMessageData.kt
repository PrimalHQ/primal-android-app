package net.primal.data.local.dao.messages

import androidx.room.Entity
import androidx.room.TypeConverters
import net.primal.data.local.encryption.EncryptableString
import net.primal.data.local.encryption.SecureConverter

@Entity(primaryKeys = ["messageId", "ownerId"])
data class DirectMessageData(
    val messageId: String,
    val ownerId: String,
    val senderId: String,
    val receiverId: String,
    val participantId: String,
    val createdAt: Long,
    @field:TypeConverters(SecureConverter::class) val content: EncryptableString,
    @field:TypeConverters(SecureConverter::class) val uris: List<String>,
    @field:TypeConverters(SecureConverter::class) val hashtags: List<String>,
)
