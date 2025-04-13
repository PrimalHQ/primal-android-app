package net.primal.data.local.dao.messages

import androidx.room.Entity
import net.primal.domain.messages.ConversationRelation

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
