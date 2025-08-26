package net.primal.domain.streams.mappers

import kotlin.time.Duration.Companion.hours
import kotlinx.datetime.Clock
import net.primal.domain.nostr.NostrEvent
import net.primal.domain.nostr.findFirstEnds
import net.primal.domain.nostr.findFirstStarts
import net.primal.domain.nostr.findFirstStatus
import net.primal.domain.nostr.findFirstStreaming
import net.primal.domain.streams.StreamStatus

private val LIVE_STREAM_STALE_THRESHOLD = 2.hours

fun NostrEvent.resolveStreamStatus(): StreamStatus {
    val streamingUrl = this.tags.findFirstStreaming()
    val statusTagValue = this.tags.findFirstStatus()
    val startsAtTimestamp = this.tags.findFirstStarts()?.toLongOrNull()
    val endsAtTimestamp = this.tags.findFirstEnds()?.toLongOrNull()

    val initialStatus = StreamStatus.fromString(statusTagValue)
    var finalStatus = initialStatus

    if (initialStatus == StreamStatus.LIVE) {
        val nowEpochSecond = Clock.System.now().epochSeconds

        if (streamingUrl.isNullOrBlank()) {
            finalStatus = when {
                startsAtTimestamp == null -> StreamStatus.ENDED
                startsAtTimestamp > nowEpochSecond -> StreamStatus.PLANNED
                else -> StreamStatus.ENDED
            }
        } else if (endsAtTimestamp != null && endsAtTimestamp < nowEpochSecond) {
            finalStatus = StreamStatus.ENDED
        } else {
            val eventAgeSeconds = nowEpochSecond - this.createdAt
            if (eventAgeSeconds > LIVE_STREAM_STALE_THRESHOLD.inWholeSeconds) {
                finalStatus = StreamStatus.ENDED
            }
        }
    }

    return finalStatus
}
