package net.primal.domain.polls

data class PollVoteStats(
    val eventId: String,
    val pollType: PollType = PollType.User,
    val options: List<PollOptionStats>,
)
