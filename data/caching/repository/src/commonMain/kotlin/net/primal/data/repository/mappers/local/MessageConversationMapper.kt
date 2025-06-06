package net.primal.data.repository.mappers.local

import net.primal.data.local.dao.messages.MessageConversation
import net.primal.domain.messages.DMConversation
import net.primal.domain.profile.ProfileData

fun MessageConversation.asDMConversation(): DMConversation {
    return DMConversation(
        ownerId = this.data.ownerId,
        participant = this.participant?.asProfileDataDO() ?: ProfileData(profileId = this.data.participantId),
        lastMessage = this.lastMessage?.asDirectMessageDO(),
        unreadMessagesCount = this.data.unreadMessagesCount,
        relation = this.data.relation,
    )
}
