package net.primal.android.explore.home.zaps

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.Instant
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.getAndUpdate
import kotlinx.coroutines.launch
import net.primal.android.core.compose.profile.model.asProfileDetailsUi
import net.primal.android.explore.home.zaps.ExploreZapsContract.UiEvent
import net.primal.android.explore.home.zaps.ExploreZapsContract.UiState
import net.primal.android.explore.home.zaps.ui.ExploreZapNoteUi
import net.primal.android.notes.feed.model.asNoteNostrUriUi
import net.primal.android.notes.feed.model.toNoteContentUi
import net.primal.android.user.accounts.active.ActiveAccountStore
import net.primal.domain.common.exception.NetworkException
import net.primal.domain.explore.ExploreRepository
import net.primal.domain.explore.ExploreZapNoteData
import timber.log.Timber

@HiltViewModel
class ExploreZapsViewModel @Inject constructor(
    private val exploreRepository: ExploreRepository,
    private val activeAccountStore: ActiveAccountStore,
) : ViewModel() {

    private val _state = MutableStateFlow(UiState())
    val state = _state.asStateFlow()
    private fun setState(reducer: UiState.() -> UiState) = _state.getAndUpdate(reducer)

    private val events = MutableSharedFlow<UiEvent>()
    fun setEvent(event: UiEvent) = viewModelScope.launch { events.emit(event) }

    init {
        fetchExploreZaps()
        observeEvents()
    }

    private fun fetchExploreZaps() =
        viewModelScope.launch {
            try {
                setState { copy(loading = true) }
                val zaps = exploreRepository.fetchTrendingZaps(
                    userId = activeAccountStore.activeUserId(),
                )
                setState { copy(zaps = zaps.mapAsUiModel()) }
            } catch (error: NetworkException) {
                Timber.w(error)
            } finally {
                setState { copy(loading = false) }
            }
        }

    private fun observeEvents() =
        viewModelScope.launch {
            events.collect {
                when (it) {
                    UiEvent.RefreshZaps -> fetchExploreZaps()
                }
            }
        }

    private fun List<ExploreZapNoteData>.mapAsUiModel() =
        map { zapData ->
            ExploreZapNoteUi(
                sender = zapData.sender?.asProfileDetailsUi(),
                receiver = zapData.receiver?.asProfileDetailsUi(),
                amountSats = zapData.amountSats,
                zapMessage = zapData.zapMessage,
                createdAt = Instant.ofEpochSecond(zapData.createdAt.epochSeconds),
                noteContentUi = zapData.noteData.toNoteContentUi(
                    nostrUris = zapData.noteNostrUris.map { it.asNoteNostrUriUi() },
                ).copy(hashtags = emptyList()),
            )
        }
}
