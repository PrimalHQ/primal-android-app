package net.primal.android.events.reactions

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.cachedIn
import androidx.paging.map
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.getAndUpdate
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import net.primal.android.core.compose.profile.model.asProfileDetailsUi
import net.primal.android.events.reactions.ReactionsContract.UiState
import net.primal.android.events.ui.asEventZapUiModel
import net.primal.android.navigation.articleATag
import net.primal.android.navigation.eventIdOrThrow
import net.primal.android.navigation.reactionTypeOrThrow
import net.primal.android.user.accounts.active.ActiveAccountStore
import net.primal.domain.common.exception.NetworkException
import net.primal.domain.events.EventRepository
import net.primal.domain.events.NostrEventAction
import net.primal.domain.nostr.NostrEventKind
import timber.log.Timber

@HiltViewModel
class ReactionsViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    activeAccountStore: ActiveAccountStore,
    private val eventRepository: EventRepository,
) : ViewModel() {

    private val eventId = savedStateHandle.eventIdOrThrow
    private val articleATag = savedStateHandle.articleATag

    private val _state = MutableStateFlow(
        UiState(
            zaps = eventRepository.pagedEventZaps(
                userId = activeAccountStore.activeUserId(),
                eventId = eventId,
                articleATag = articleATag,
            )
                .map { it.map { noteZap -> noteZap.asEventZapUiModel() } }
                .cachedIn(viewModelScope),
            initialReactionType = savedStateHandle.reactionTypeOrThrow,
        ),
    )
    val state = _state.asStateFlow()
    private fun setState(reducer: UiState.() -> UiState) = _state.getAndUpdate { it.reducer() }

    init {
        fetchLikes()
        fetchReposts()
    }

    private fun fetchLikes() =
        viewModelScope.launch {
            try {
                setState { copy(loading = true) }
                val likes = eventRepository.fetchEventActions(
                    eventId = eventId,
                    kind = NostrEventKind.Reaction.value,
                )
                setState { copy(likes = likes.map { it.mapAsEventActionUi() }) }
            } catch (error: NetworkException) {
                Timber.e(error)
            } finally {
                setState { copy(loading = false) }
            }
        }

    private fun fetchReposts() =
        viewModelScope.launch {
            try {
                setState { copy(loading = true) }
                val reposts = eventRepository.fetchEventActions(
                    eventId = eventId,
                    kind = NostrEventKind.ShortTextNoteRepost.value,
                )
                setState { copy(reposts = reposts.map { it.mapAsEventActionUi() }) }
            } catch (error: NetworkException) {
                Timber.e(error)
            } finally {
                setState { copy(loading = false) }
            }
        }

    private fun NostrEventAction.mapAsEventActionUi(): EventActionUi {
        return EventActionUi(
            profile = this.profile.asProfileDetailsUi(),
            action = this.actionEventData.content,
        )
    }
}
