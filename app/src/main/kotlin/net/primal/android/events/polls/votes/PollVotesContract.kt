package net.primal.android.events.polls.votes

import net.primal.android.core.compose.profile.model.UserProfileItemUi

interface PollVotesContract {
    data class UiState(
        val loading: Boolean = true,
        val pollOptions: List<PollOptionUi> = emptyList(),
        val isZapPoll: Boolean = false,
        val error: Throwable? = null,
    )

    data class ScreenCallbacks(
        val onClose: () -> Unit,
        val onProfileClick: (String) -> Unit,
    )
}

data class PollOptionUi(
    val id: String,
    val title: String,
    val voteCount: Int,
    val totalSats: Long = 0,
    val voters: List<PollVoterUi>,
)

data class PollVoterUi(
    val profile: UserProfileItemUi,
    val satsZapped: Long = 0,
    val zapComment: String? = null,
)
