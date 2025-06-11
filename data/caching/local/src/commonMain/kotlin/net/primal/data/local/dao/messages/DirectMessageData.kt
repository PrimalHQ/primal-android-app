package net.primal.data.local.dao.messages

import androidx.room.Entity
import androidx.room.TypeConverters
import net.primal.shared.data.local.encryption.EncryptableString
import net.primal.shared.data.local.serialization.EncryptableStringConvertor

@Entity(primaryKeys = ["messageId", "ownerId"])
data class DirectMessageData(
    val messageId: String,
    val ownerId: String,
    val senderId: String,
    val receiverId: String,
    val participantId: String,
    val createdAt: Long,
    @field:TypeConverters(EncryptableStringConvertor::class) val content: EncryptableString,
    @field:TypeConverters(EncryptableStringConvertor::class) val uris: List<String>,
    @field:TypeConverters(EncryptableStringConvertor::class) val hashtags: List<String>,
)
