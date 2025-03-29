package net.primal.domain.model

import net.primal.domain.ConversationRelation

data class DMConversation(
    val ownerId: String,
    val participant: ProfileData?,
    val lastMessage: DirectMessage?,
    val unreadMessagesCount: Int,
    val relation: ConversationRelation,
)
