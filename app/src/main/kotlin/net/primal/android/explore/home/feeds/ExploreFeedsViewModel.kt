package net.primal.android.explore.home.feeds

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.getAndUpdate
import kotlinx.coroutines.launch
import net.primal.android.explore.home.feeds.ExploreFeedsContract.UiState
import net.primal.android.feeds.domain.DvmFeed
import net.primal.android.feeds.domain.buildSpec
import net.primal.android.feeds.repository.FeedsRepository
import net.primal.android.feeds.ui.model.asFeedUi
import net.primal.android.networking.sockets.errors.WssException
import timber.log.Timber

@HiltViewModel(assistedFactory = ExploreFeedsViewModel.Factory::class)
class ExploreFeedsViewModel @AssistedInject constructor(
    @Assisted private val activeAccountPubkey: String?,
    private val feedsRepository: FeedsRepository,
) : ViewModel() {

    @AssistedFactory
    interface Factory {
        fun create(activeAccountPubkey: String?): ExploreFeedsViewModel
    }

    private val _state = MutableStateFlow(UiState())
    val state = _state.asStateFlow()
    private fun setState(reducer: UiState.() -> UiState) = _state.getAndUpdate { it.reducer() }

    private val events: MutableSharedFlow<ExploreFeedsContract.UiEvent> = MutableSharedFlow()
    fun setEvent(event: ExploreFeedsContract.UiEvent) = viewModelScope.launch { events.emit(event) }

    init {
        fetchExploreFeeds()
        observeUserReadFeeds()
        observeUserNoteFeeds()
        observeEvents()
    }

    private fun observeUserReadFeeds() =
        viewModelScope.launch {
            feedsRepository.observeReadsFeeds()
                .collect {
                    setState { copy(userReadFeeds = it.map { it.asFeedUi() }) }
                }
        }

    private fun observeUserNoteFeeds() =
        viewModelScope.launch {
            feedsRepository.observeNotesFeeds()
                .collect {
                    setState { copy(userNoteFeeds = it.map { it.asFeedUi() }) }
                }
        }

    private fun observeEvents() =
        viewModelScope.launch {
            events.collect {
                when (it) {
                    is ExploreFeedsContract.UiEvent.AddToUserFeeds -> addToUserFeeds(it.dvmFeed)
                    is ExploreFeedsContract.UiEvent.RemoveFromUserFeeds -> removeFromUserFeeds(it.dvmFeed)
                    ExploreFeedsContract.UiEvent.RefreshFeeds -> fetchExploreFeeds()
                }
            }
        }

    private fun addToUserFeeds(dvmFeed: DvmFeed) =
        viewModelScope.launch {
            dvmFeed.kind?.let {
                feedsRepository.addDvmFeed(dvmFeed = dvmFeed, specKind = dvmFeed.kind)
            }
        }

    private fun removeFromUserFeeds(dvmFeed: DvmFeed) =
        viewModelScope.launch {
            dvmFeed.kind?.let {
                feedsRepository.removeFeed(feedSpec = dvmFeed.buildSpec(specKind = dvmFeed.kind))
            }
        }

    private fun fetchExploreFeeds() =
        viewModelScope.launch {
            setState { copy(loading = true) }
            try {
                val feeds = feedsRepository.fetchRecommendedDvmFeeds(pubkey = activeAccountPubkey)
                setState { copy(feeds = feeds) }
            } catch (error: WssException) {
                Timber.w(error)
                setState { copy(error = error) }
            } finally {
                setState { copy(loading = false) }
            }
        }
}
