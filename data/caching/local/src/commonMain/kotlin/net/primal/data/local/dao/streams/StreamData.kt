package net.primal.data.local.dao.streams

import androidx.room3.Entity
import androidx.room3.Index
import androidx.room3.PrimaryKey
import net.primal.domain.streams.StreamStatus
import net.primal.domain.streams.mappers.resolveStreamStatus

@Entity(
    indices = [
        Index(value = ["mainHostId"]),
    ],
)
data class StreamData(
    @PrimaryKey
    val aTag: String,
    val eventId: String,
    val eventAuthorId: String,
    val mainHostId: String,
    val dTag: String,
    val title: String?,
    val summary: String?,
    val imageUrl: String?,
    val hashtags: List<String>,
    val streamingUrl: String?,
    val recordingUrl: String?,
    val startsAt: Long?,
    val endsAt: Long?,
    val status: StreamStatus,
    val currentParticipants: Int?,
    val totalParticipants: Int?,
    val raw: String,
    val createdAt: Long,
) {
    val resolvedStatus
        get() = resolveStreamStatus(
            status = status,
            streamingUrl = streamingUrl,
            startsAt = startsAt,
            endsAt = endsAt,
            createdAt = createdAt,
        )

    fun isLive() = resolvedStatus == StreamStatus.LIVE
}
