package net.primal.android.bookmarks.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.getAndUpdate
import kotlinx.coroutines.launch
import net.primal.android.bookmarks.list.BookmarksContract.UiEvent
import net.primal.android.bookmarks.list.BookmarksContract.UiState
import net.primal.android.feeds.domain.FeedSpecKind
import net.primal.android.feeds.domain.buildArticleBookmarksFeedSpec
import net.primal.android.feeds.domain.buildNotesBookmarksFeedSpec
import net.primal.android.user.accounts.active.ActiveAccountStore

@HiltViewModel
class BookmarksViewModel @Inject constructor(
    private val activeAccountStore: ActiveAccountStore,
) : ViewModel() {

    private val _state = MutableStateFlow(
        UiState(
            feedSpec = buildNotesBookmarksFeedSpec(userId = activeAccountStore.activeUserId()),
            feedSpecKind = FeedSpecKind.Notes,
        ),
    )
    val state = _state.asStateFlow()
    private fun setState(reducer: UiState.() -> UiState) = _state.getAndUpdate(reducer)

    private val events = MutableSharedFlow<UiEvent>()
    fun setEvent(event: UiEvent) = viewModelScope.launch { events.emit(event) }

    init {
        observeEvents()
    }

    private fun observeEvents() =
        viewModelScope.launch {
            events.collect {
                when (it) {
                    is UiEvent.ChangeFeedSpecKind -> updateFeedSpec(it.feedSpecKind)
                }
            }
        }

    private fun updateFeedSpec(feedSpecKind: FeedSpecKind) =
        viewModelScope.launch {
            val userId = activeAccountStore.activeUserId()
            val feedSpec = when (feedSpecKind) {
                FeedSpecKind.Reads -> buildArticleBookmarksFeedSpec(userId = userId)
                FeedSpecKind.Notes -> buildNotesBookmarksFeedSpec(userId = userId)
            }
            setState { copy(feedSpec = feedSpec, feedSpecKind = feedSpecKind) }
        }
}
