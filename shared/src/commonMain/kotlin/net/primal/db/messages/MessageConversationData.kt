package net.primal.db.messages

import androidx.room.Entity

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
