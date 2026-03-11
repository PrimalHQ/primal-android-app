package net.primal.android.events.polls.votes

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.map
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.aakira.napier.Napier
import javax.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.getAndUpdate
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import net.primal.android.core.compose.profile.model.asUserProfileItemUi
import net.primal.android.events.polls.votes.PollVotesContract.UiEvent
import net.primal.android.events.polls.votes.PollVotesContract.UiState
import net.primal.android.events.polls.votes.model.PollVoterUi
import net.primal.android.navigation.eventIdOrThrow
import net.primal.android.user.accounts.active.ActiveAccountStore
import net.primal.domain.polls.PollVoter
import net.primal.domain.polls.PollsRepository

@HiltViewModel
@OptIn(ExperimentalCoroutinesApi::class)
class PollVotesViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val activeAccountStore: ActiveAccountStore,
    private val pollsRepository: PollsRepository,
) : ViewModel() {

    private val eventId: String = savedStateHandle.eventIdOrThrow

    private val _state = MutableStateFlow(UiState())
    val state = _state.asStateFlow()
    private fun setState(reducer: UiState.() -> UiState) = _state.getAndUpdate { it.reducer() }

    private val events: MutableSharedFlow<UiEvent> = MutableSharedFlow()
    fun setEvent(event: UiEvent) = viewModelScope.launch { events.emit(event) }

    val votersPagingData: Flow<PagingData<PollVoterUi>> = _state
        .map { it.selectedOptionId }
        .filterNotNull()
        .distinctUntilChanged()
        .flatMapLatest { optionId ->
            pollsRepository.createVotersPager(eventId = eventId, optionId = optionId)
                .map { pagingData -> pagingData.map { voter -> voter.toUi() } }
        }
        .cachedIn(viewModelScope)

    init {
        observeEvents()
        observePollData()
    }

    private fun observePollData() =
        viewModelScope.launch {
            runCatching {
                val userId = activeAccountStore.activeUserId()
                val votedOptionsFlow = pollsRepository.observeUserVotedOptions(
                    userId = userId,
                    postId = eventId,
                )

                pollsRepository.observePollData(eventId = eventId)
                    .filterNotNull()
                    .flatMapLatest { pollInfo ->
                        votedOptionsFlow.map { votedOptions -> pollInfo to votedOptions }
                    }
                    .collect { (pollInfo, votedOptions) ->
                        val pollUi = pollInfo.asPollUi(userVotedOptionIds = votedOptions)
                        setState {
                            copy(
                                loading = false,
                                pollUi = pollUi,
                                isZapPoll = pollInfo.isZapPoll,
                                selectedOptionId = selectedOptionId
                                    ?: pollInfo.options.firstOrNull()?.id,
                            )
                        }
                    }
            }.onFailure { error ->
                Napier.e("Failed to observe poll data", error)
                setState { copy(loading = false, error = error) }
            }
        }

    private fun observeEvents() =
        viewModelScope.launch {
            events.collect { event ->
                when (event) {
                    is UiEvent.SelectOption -> setState { copy(selectedOptionId = event.optionId) }
                }
            }
        }

    private fun PollVoter.toUi() =
        PollVoterUi(
            eventId = this.eventId,
            profile = this.profile.asUserProfileItemUi(),
            satsZapped = this.satsZapped,
            zapComment = this.zapComment,
        )
}
