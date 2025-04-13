package net.primal.domain.events

data class NostrEventStats(
    val eventId: String,
    val likes: Long? = null,
    val replies: Long? = null,
    val mentions: Long? = null,
    val reposts: Long? = null,
    val zaps: Long? = null,
    val satsZapped: Long? = null,
    val score: Long? = null,
    val score24h: Long? = null,
)
