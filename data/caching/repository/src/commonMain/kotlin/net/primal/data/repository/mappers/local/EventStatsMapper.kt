package net.primal.data.repository.mappers.local

import net.primal.data.local.dao.events.EventStats
import net.primal.domain.events.NostrEventStats

fun EventStats.asNostrEventStats(): NostrEventStats {
    return NostrEventStats(
        eventId = this.eventId,
        likes = this.likes,
        replies = this.replies,
        mentions = this.mentions,
        reposts = this.reposts,
        zaps = this.zaps,
        satsZapped = this.satsZapped,
        score = this.score,
        score24h = this.score24h,
    )
}
