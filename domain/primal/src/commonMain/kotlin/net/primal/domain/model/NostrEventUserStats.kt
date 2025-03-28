package net.primal.domain.model

data class NostrEventUserStats(
    val eventId: String,
    val userId: String,
    val replied: Boolean = false,
    val liked: Boolean = false,
    val reposted: Boolean = false,
    val zapped: Boolean = false,
)
