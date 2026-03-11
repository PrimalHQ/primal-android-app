package net.primal.android.events.polls.votes

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.aakira.napier.Napier
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.getAndUpdate
import kotlinx.coroutines.launch
import net.primal.android.core.compose.profile.model.asUserProfileItemUi
import net.primal.android.events.polls.votes.PollVotesContract.UiState
import net.primal.android.events.polls.votes.model.PollVoteOptionUi
import net.primal.android.events.polls.votes.model.PollVoterUi
import net.primal.android.navigation.eventIdOrThrow
import net.primal.domain.polls.PollsRepository

@HiltViewModel
class PollVotesViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val pollsRepository: PollsRepository,
) : ViewModel() {

    private val eventId: String = savedStateHandle.eventIdOrThrow

    private val _state = MutableStateFlow(UiState())
    val state = _state.asStateFlow()
    private fun setState(reducer: UiState.() -> UiState) = _state.getAndUpdate { it.reducer() }

    init {
        observeVotes()
        fetchVotes()
    }

    private fun observeVotes() =
        viewModelScope.launch {
            pollsRepository.observePollVotes(eventId = eventId)
                .collect { stats ->
                    setState {
                        copy(
                            loading = false,
                            isZapPoll = stats.isZapPoll,
                            pollOptions = stats.options.map { option ->
                                PollVoteOptionUi(
                                    id = option.optionInfo.id,
                                    title = option.optionInfo.label,
                                    voteCount = option.optionInfo.voteCount,
                                    totalSats = option.optionInfo.satsZapped,
                                    voters = option.voters.map { voter ->
                                        PollVoterUi(
                                            eventId = voter.eventId,
                                            profile = voter.profile.asUserProfileItemUi(),
                                            satsZapped = voter.satsZapped,
                                            zapComment = voter.zapComment,
                                        )
                                    },
                                )
                            },
                        )
                    }
                }
        }

    private fun fetchVotes() =
        viewModelScope.launch {
            setState { copy(loading = true) }
            runCatching { pollsRepository.fetchPollVotes(eventId = eventId) }
                .onFailure { error ->
                    Napier.w(throwable = error) { "Failed to fetch poll votes for eventId=$eventId" }
                    setState { copy(error = error) }
                }
            setState { copy(loading = false) }
        }
}
