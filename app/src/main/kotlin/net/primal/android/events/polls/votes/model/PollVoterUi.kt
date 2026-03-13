package net.primal.android.events.polls.votes.model

import net.primal.android.core.compose.profile.model.UserProfileItemUi

data class PollVoterUi(
    val eventId: String,
    val profile: UserProfileItemUi,
    val satsZapped: Long = 0,
    val zapComment: String? = null,
)
