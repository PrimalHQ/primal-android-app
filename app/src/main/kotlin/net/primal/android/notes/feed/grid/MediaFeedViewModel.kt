package net.primal.android.notes.feed.grid

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.cachedIn
import androidx.paging.map
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.getAndUpdate
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import net.primal.android.feeds.domain.isPremiumFeedSpec
import net.primal.android.notes.feed.grid.MediaFeedContract.UiState
import net.primal.android.notes.feed.model.asFeedPostUi
import net.primal.android.notes.repository.FeedRepository
import net.primal.android.premium.utils.hasPremiumMembership
import net.primal.android.user.accounts.active.ActiveAccountStore

@HiltViewModel(assistedFactory = MediaFeedViewModel.Factory::class)
class MediaFeedViewModel @AssistedInject constructor(
    @Assisted private val feedSpec: String,
    private val feedRepository: FeedRepository,
    private val activeAccountStore: ActiveAccountStore,
) : ViewModel() {

    @AssistedFactory
    interface Factory {
        fun create(feedSpec: String): MediaFeedViewModel
    }

    private fun buildFeedByDirective(feedSpec: String) =
        feedRepository.feedBySpec(feedSpec = feedSpec)
            .map { it.map { feedNote -> feedNote.asFeedPostUi() } }
            .cachedIn(viewModelScope)

    private val _state = MutableStateFlow(UiState(notes = buildFeedByDirective(feedSpec = feedSpec)))
    val state = _state.asStateFlow()
    private fun setState(reducer: UiState.() -> UiState) = _state.getAndUpdate { it.reducer() }

    init {
        observeActiveAccount()
    }

    private fun observeActiveAccount() =
        viewModelScope.launch {
            activeAccountStore.activeUserAccount.collect {
                setState { copy(paywall = feedSpec.isPremiumFeedSpec() && !it.hasPremiumMembership()) }
            }
        }
}
