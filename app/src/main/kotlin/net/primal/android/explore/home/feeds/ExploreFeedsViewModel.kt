package net.primal.android.explore.home.feeds

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.room.util.copy
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.getAndUpdate
import kotlinx.coroutines.launch
import net.primal.android.explore.home.feeds.ExploreFeedsContract.UiState
import net.primal.android.feeds.domain.DvmFeed
import net.primal.android.feeds.domain.buildSpec
import net.primal.android.feeds.repository.FeedsRepository
import net.primal.android.networking.sockets.errors.WssException
import net.primal.android.user.accounts.active.ActiveAccountStore
import timber.log.Timber

@HiltViewModel
class ExploreFeedsViewModel @Inject constructor(
    private val activeAccountStore: ActiveAccountStore,
    private val feedsRepository: FeedsRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(UiState())
    val state = _state.asStateFlow()
    private fun setState(reducer: UiState.() -> UiState) = _state.getAndUpdate { it.reducer() }

    private val events: MutableSharedFlow<ExploreFeedsContract.UiEvent> = MutableSharedFlow()
    fun setEvent(event: ExploreFeedsContract.UiEvent) = viewModelScope.launch { events.emit(event) }

    init {
        fetchExploreFeeds()
        observeAllUserFeeds()
        observeEvents()
    }

    private fun observeAllUserFeeds() =
        viewModelScope.launch {
            feedsRepository.observeAllFeeds()
                .collect {
                    setState { copy(userFeedSpecs = it.map { it.spec }) }
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
            try {
                dvmFeed.kind?.let {
                    feedsRepository.addDvmFeed(dvmFeed = dvmFeed, specKind = dvmFeed.kind)
                }
                feedsRepository.persistAllLocalUserFeeds(userId = activeAccountStore.activeUserId())
            } catch (error: WssException) {
                Timber.w(error)
            }
        }

    private fun removeFromUserFeeds(dvmFeed: DvmFeed) =
        viewModelScope.launch {
            try {
                dvmFeed.kind?.let {
                    feedsRepository.removeFeed(feedSpec = dvmFeed.buildSpec(specKind = dvmFeed.kind))
                }
                feedsRepository.persistAllLocalUserFeeds(userId = activeAccountStore.activeUserId())
            } catch (error: WssException) {
                Timber.w(error)
            }
        }

    private fun fetchExploreFeeds() =
        viewModelScope.launch {
            setState { copy(loading = true) }
            try {
                val feeds = feedsRepository.fetchRecommendedDvmFeeds(pubkey = activeAccountStore.activeUserId())
                setState { copy(feeds = feeds) }
            } catch (error: WssException) {
                Timber.w(error)
                setState { copy(error = error) }
            } finally {
                setState { copy(loading = false) }
            }
        }
}
