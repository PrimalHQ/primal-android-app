package net.primal.domain.messages

import net.primal.domain.profile.ProfileData

data class DMConversation(
    val ownerId: String,
    val participant: ProfileData,
    val lastMessage: DirectMessage?,
    val unreadMessagesCount: Int,
    val relation: ConversationRelation,
)
