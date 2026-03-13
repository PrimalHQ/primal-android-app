package net.primal.domain.polls

data class PollOptionStats(
    val optionInfo: PollOptionInfo,
    val voters: List<PollVoter>,
)
