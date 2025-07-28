package net.primal.data.local.dao.streams

import androidx.room.Entity
import androidx.room.PrimaryKey
import net.primal.domain.streams.StreamStatus

@Entity
data class StreamData(
    @PrimaryKey
    val authorId: String,
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
) {
    fun isLive() = status == StreamStatus.LIVE
}
