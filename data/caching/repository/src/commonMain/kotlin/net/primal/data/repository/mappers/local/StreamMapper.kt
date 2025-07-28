package net.primal.data.repository.mappers.local

import net.primal.data.local.dao.streams.StreamData as StreamPO
import net.primal.domain.streams.Stream as StreamDO

fun StreamPO.asStreamDO(): StreamDO {
    return StreamDO(
        authorId = this.authorId,
        dTag = this.dTag,
        title = this.title,
        summary = this.summary,
        imageUrl = this.imageUrl,
        hashtags = this.hashtags,
        streamingUrl = this.streamingUrl,
        recordingUrl = this.recordingUrl,
        startsAt = this.startsAt,
        endsAt = this.endsAt,
        status = this.status,
        currentParticipants = this.currentParticipants,
        totalParticipants = this.totalParticipants,
    )
}
