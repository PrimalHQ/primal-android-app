package net.primal.domain.streams

data class Stream(
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
