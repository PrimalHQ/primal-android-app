package net.primal.domain.polls

import net.primal.domain.profile.ProfileData

data class PollVoteStats(
    val eventId: String,
    val isZapPoll: Boolean = false,
    val options: List<PollOptionStats>,
)

data class PollOptionStats(
    val optionId: String,
    val optionTitle: String,
    val voteCount: Int,
    val totalSats: Long = 0,
    val voters: List<PollVoter>,
)

data class PollVoter(
    val profile: ProfileData,
    val satsZapped: Long = 0,
    val zapComment: String? = null,
)
