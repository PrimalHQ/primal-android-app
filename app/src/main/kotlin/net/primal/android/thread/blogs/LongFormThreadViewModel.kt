package net.primal.android.thread.blogs

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.getAndUpdate
import kotlinx.coroutines.launch
import net.primal.android.navigation.naddrOrThrow
import net.primal.android.nostr.utils.Naddr
import net.primal.android.nostr.utils.Nip19TLV
import net.primal.android.read.ReadRepository
import net.primal.android.thread.blogs.LongFormThreadContract.UiEvent
import net.primal.android.thread.blogs.LongFormThreadContract.UiState
import net.primal.android.user.accounts.active.ActiveAccountStore

@HiltViewModel
class LongFormThreadViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val activeAccountStore: ActiveAccountStore,
    private val readsRepository: ReadRepository,
) : ViewModel() {

    private val naddr = Nip19TLV.parseAsNaddr(savedStateHandle.naddrOrThrow)

    private val _state = MutableStateFlow(UiState())
    val state = _state.asStateFlow()
    private fun setState(reducer: UiState.() -> UiState) = _state.getAndUpdate { it.reducer() }

    private val events: MutableSharedFlow<UiEvent> = MutableSharedFlow()
    fun setEvent(event: UiEvent) = viewModelScope.launch { events.emit(event) }

    init {
        observeEvents()
    }

    private fun observeEvents() =
        viewModelScope.launch {
            events.collect {
                when (it) {
                    UiEvent.UpdateContent -> fetchData(naddr)
                    UiEvent.DismissErrors -> setState { copy(error = null) }
                }
            }
        }

    private fun fetchData(naddr: Naddr?) =
        viewModelScope.launch {
            if (naddr == null) {
                setState { copy(error = UiState.LongFormThreadError.InvalidNaddr) }
            } else {
                readsRepository.fetchBlogContentAndReplies(
                    userId = activeAccountStore.activeUserId(),
                    authorUserId = naddr.userId,
                    identifier = naddr.identifier,
                )
            }
        }
}
