package net.primal.data.repository.mappers.local

import net.primal.data.local.dao.messages.MessageConversation
import net.primal.domain.model.DMConversation
import net.primal.domain.model.ProfileData

fun MessageConversation.asDMConversation(): DMConversation {
    return DMConversation(
        ownerId = this.data.ownerId,
        participant = this.participant?.asProfileDataDO() ?: ProfileData(profileId = this.data.participantId),
        lastMessage = this.lastMessage?.asDirectMessageDO(),
        unreadMessagesCount = this.data.unreadMessagesCount,
        relation = this.data.relation,
    )
}
