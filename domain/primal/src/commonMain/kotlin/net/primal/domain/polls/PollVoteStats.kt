package net.primal.domain.polls

data class PollVoteStats(
    val eventId: String,
    val isZapPoll: Boolean = false,
    val options: List<PollOptionStats>,
)
