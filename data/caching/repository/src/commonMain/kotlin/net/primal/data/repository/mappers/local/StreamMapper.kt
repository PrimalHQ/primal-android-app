package net.primal.data.repository.mappers.local

import net.primal.data.local.dao.streams.Stream as StreamPO
import net.primal.domain.streams.Stream as StreamDO

fun StreamPO.asStreamDO(): StreamDO {
    return StreamDO(
        aTag = this.data.aTag,
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
    )
}
