package net.primal.android.events.polls.votes

import net.primal.android.notes.feed.model.PollUi

interface PollVotesContract {

    data class UiState(
        val loading: Boolean = true,
        val pollUi: PollUi? = null,
        val selectedOptionId: String? = null,
        val error: Throwable? = null,
    )

    sealed class UiEvent {
        data class SelectOption(val optionId: String) : UiEvent()
    }

    data class ScreenCallbacks(
        val onClose: () -> Unit,
        val onProfileClick: (String) -> Unit,
    )
}
