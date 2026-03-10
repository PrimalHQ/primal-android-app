package net.primal.domain.polls

data class PollOptionInfo(
    val id: String,
    val label: String,
    val voteCount: Int = 0,
    val satsZapped: Long = 0,
)
