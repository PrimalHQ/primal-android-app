package net.primal.android.messages.db

import androidx.room.Entity
import androidx.room.PrimaryKey
import net.primal.android.messages.domain.ConversationRelation

@Entity(primaryKeys = ["participantId", "ownerId"])
data class MessageConversationData(
    val ownerId: String,
    val participantId: String,
    val lastMessageId: String,
    val lastMessageAt: Long,
    val unreadMessagesCount: Int,
    val relation: ConversationRelation,
    val participantMetadataId: String?,
)
