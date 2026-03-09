package net.primal.domain.polls

data class PollOptionStats(
    val optionId: String,
    val optionTitle: String,
    val voteCount: Int,
    val totalSats: Long = 0,
    val voters: List<PollVoter>,
)
