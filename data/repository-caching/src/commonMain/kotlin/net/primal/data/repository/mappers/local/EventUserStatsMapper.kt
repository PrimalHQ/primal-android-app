package net.primal.data.repository.mappers.local

import net.primal.data.local.dao.events.EventUserStats
import net.primal.domain.model.NostrEventUserStats

fun EventUserStats.asNostrEventUserStats(): NostrEventUserStats {
    return NostrEventUserStats(
        eventId = this.eventId,
        userId = this.userId,
        replied = this.replied,
        liked = this.liked,
        reposted = this.reposted,
        zapped = this.zapped,
    )
}
