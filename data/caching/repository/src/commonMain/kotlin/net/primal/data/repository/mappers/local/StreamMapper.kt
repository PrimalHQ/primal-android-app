package net.primal.data.repository.mappers.local

import net.primal.data.local.dao.streams.Stream as StreamPO
import net.primal.data.local.dao.streams.StreamChatMessage
import net.primal.domain.streams.Stream as StreamDO
import net.primal.domain.streams.chat.ChatMessage

fun StreamPO.asStreamDO(): StreamDO {
    return StreamDO(
        aTag = this.data.aTag,
        eventId = this.data.eventId,
        authorId = this.data.authorId,
        dTag = this.data.dTag,
        title = this.data.title,
        summary = this.data.summary,
        imageUrl = this.data.imageUrl,
        hashtags = this.data.hashtags,
        streamingUrl = this.data.streamingUrl,
        recordingUrl = this.data.recordingUrl,
        startsAt = this.data.startsAt,
        endsAt = this.data.endsAt,
        status = this.data.status,
        currentParticipants = this.data.currentParticipants,
        totalParticipants = this.data.totalParticipants,
        eventZaps = this.eventZaps.map { it.asEventZapDO() },
        rawNostrEventJson = this.data.raw,
    )
}

fun StreamChatMessage.asChatMessageDO(): ChatMessage? {
    val author = this.author ?: return null
    return ChatMessage(
        messageId = this.data.messageId,
        author = author.asProfileDataDO(),
        content = this.data.content,
        createdAt = this.data.createdAt,
        raw = this.data.raw,
        client = this.data.client,
    )
}
