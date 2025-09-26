package net.primal.domain.streams.mappers

import kotlin.time.Clock
import kotlin.time.Duration.Companion.hours
import net.primal.domain.streams.StreamStatus

private val LIVE_STREAM_STALE_THRESHOLD = 1.hours

fun resolveStreamStatus(
    status: StreamStatus,
    streamingUrl: String?,
    startsAt: Long?,
    endsAt: Long?,
    createdAt: Long,
): StreamStatus {
    var finalStatus = status

    if (status == StreamStatus.LIVE) {
        val nowEpochSecond = Clock.System.now().epochSeconds

        if (streamingUrl.isNullOrBlank()) {
            finalStatus = when {
                startsAt == null -> StreamStatus.ENDED
                startsAt > nowEpochSecond -> StreamStatus.PLANNED
                else -> StreamStatus.ENDED
            }
        } else if (endsAt != null && endsAt < nowEpochSecond) {
            finalStatus = StreamStatus.ENDED
        } else {
            val eventAgeSeconds = nowEpochSecond - createdAt
            if (eventAgeSeconds > LIVE_STREAM_STALE_THRESHOLD.inWholeSeconds) {
                finalStatus = StreamStatus.ENDED
            }
        }
    }

    return finalStatus
}
