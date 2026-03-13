package net.primal.domain.polls

import net.primal.domain.profile.ProfileData

data class PollVoter(
    val eventId: String,
    val profile: ProfileData,
    val satsZapped: Long = 0,
    val zapComment: String? = null,
)
