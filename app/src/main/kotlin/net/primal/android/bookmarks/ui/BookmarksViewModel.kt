package net.primal.android.bookmarks.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.getAndUpdate
import kotlinx.coroutines.launch
import net.primal.android.bookmarks.ui.BookmarksContract.UiEvent
import net.primal.android.bookmarks.ui.BookmarksContract.UiState
import net.primal.android.feeds.domain.FeedSpecKind
import net.primal.android.user.accounts.active.ActiveAccountStore

@HiltViewModel
class BookmarksViewModel @Inject constructor(
    private val activeAccountStore: ActiveAccountStore,
) : ViewModel() {

    private val _state = MutableStateFlow(UiState())
    val state = _state.asStateFlow()
    private fun setState(reducer: UiState.() -> UiState) = _state.getAndUpdate(reducer)

    private val events = MutableSharedFlow<UiEvent>()
    fun setEvent(event: UiEvent) = viewModelScope.launch { events.emit(event) }

    init {
        updateFeedSpec(FeedSpecKind.Notes)
        observeEvents()
    }

    private fun observeEvents() =
        viewModelScope.launch {
            events.collect {
                when (it) {
                    is UiEvent.ChangeFeedSpecKind -> {
                        setState { copy(feedSpecKind = it.feedSpecKind) }
                        updateFeedSpec(it.feedSpecKind)
                    }
                }
            }
        }

    private fun updateFeedSpec(feedSpecKind: FeedSpecKind) =
        viewModelScope.launch {
            val userId = activeAccountStore.activeUserId()
            val feedSpec = when (feedSpecKind) {
//                FeedSpecKind.Notes -> "{\"id\":\"feed\",\"kind\":\"notes\",\"notes\":\"bookmarks\",\"pubkey\":\"$userId\"}"
//                FeedSpecKind.Reads -> "{\"id\":\"feed\",\"kind\":\"reads\",\"notes\":\"bookmarks\",\"pubkey\":\"$userId\"}"
                // TODO(marko): swap this with correct feed spec
                FeedSpecKind.Reads -> """{"id":"nostr-reads-feed","kind":"reads"}"""
                FeedSpecKind.Notes -> """{"id":"latest","kind":"notes"}"""
            }
            setState { copy(feedSpec = feedSpec) }
        }
}
