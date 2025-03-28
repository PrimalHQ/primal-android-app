package net.primal.data.repository.mappers.local

import net.primal.data.local.dao.messages.DirectMessage as DirectMessagePO
import net.primal.data.local.dao.messages.DirectMessageData
import net.primal.domain.model.DirectMessage as DirectMessageDO

fun DirectMessagePO.asDirectMessageDO(): DirectMessageDO {
    return this.data.asDirectMessageDO().copy(
        links = this.eventUris.map { it.asEventLinkDO() },
        nostrUris = this.eventNostrUris.map { it.asReferencedNostrUriDO() },
    )
}

fun DirectMessageData.asDirectMessageDO(): DirectMessageDO {
    return DirectMessageDO(
        messageId = this.messageId,
        ownerId = this.ownerId,
        senderId = this.senderId,
        receiverId = this.receiverId,
        participantId = this.participantId,
        createdAt = this.createdAt,
        content = this.content,
        hashtags = this.hashtags,
        links = emptyList(),
        nostrUris = emptyList(),
    )
}
