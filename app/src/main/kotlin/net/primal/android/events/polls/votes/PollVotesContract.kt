package net.primal.android.events.polls.votes

import net.primal.android.events.polls.votes.model.PollVoteOptionUi

interface PollVotesContract {
    data class UiState(
        val loading: Boolean = true,
        val pollOptions: List<PollVoteOptionUi> = emptyList(),
        val isZapPoll: Boolean = false,
        val error: Throwable? = null,
    )

    data class ScreenCallbacks(
        val onClose: () -> Unit,
        val onProfileClick: (String) -> Unit,
    )
}
