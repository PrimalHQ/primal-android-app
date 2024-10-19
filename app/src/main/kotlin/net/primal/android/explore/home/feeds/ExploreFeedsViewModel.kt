package net.primal.android.explore.home.feeds

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.getAndUpdate
import kotlinx.coroutines.launch
import net.primal.android.explore.home.feeds.ExploreFeedsContract.UiState
import net.primal.android.feeds.domain.DvmFeed
import net.primal.android.feeds.domain.buildSpec
import net.primal.android.feeds.repository.DvmFeedListHandler
import net.primal.android.feeds.repository.FeedsRepository
import net.primal.android.networking.sockets.errors.WssException
import net.primal.android.user.accounts.active.ActiveAccountStore
import timber.log.Timber

@HiltViewModel
class ExploreFeedsViewModel @Inject constructor(
    private val activeAccountStore: ActiveAccountStore,
    private val feedsRepository: FeedsRepository,
    private val dvmFeedListHandler: DvmFeedListHandler,
) : ViewModel() {

    private val _state = MutableStateFlow(UiState())
    val state = _state.asStateFlow()
    private fun setState(reducer: UiState.() -> UiState) = _state.getAndUpdate { it.reducer() }

    private val events: MutableSharedFlow<ExploreFeedsContract.UiEvent> = MutableSharedFlow()
    fun setEvent(event: ExploreFeedsContract.UiEvent) = viewModelScope.launch { events.emit(event) }

    private var dvmFeedsJob: Job? = null

    init {
        fetchAndObserveExploreFeeds()
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
                    is ExploreFeedsContract.UiEvent.AddToUserFeeds -> addToUserFeeds(it.dvmFeed.data)
                    is ExploreFeedsContract.UiEvent.RemoveFromUserFeeds -> removeFromUserFeeds(it.dvmFeed.data)
                    ExploreFeedsContract.UiEvent.RefreshFeeds -> fetchAndObserveExploreFeeds()
                }
            }
        }

    private fun addToUserFeeds(dvmFeed: DvmFeed) =
        viewModelScope.launch {
            try {
                dvmFeed.kind?.let {
                    feedsRepository.addDvmFeedLocally(dvmFeed = dvmFeed, specKind = dvmFeed.kind)
                }
                feedsRepository.persistRemotelyAllLocalUserFeeds(userId = activeAccountStore.activeUserId())
            } catch (error: WssException) {
                Timber.w(error)
            }
        }

    private fun removeFromUserFeeds(dvmFeed: DvmFeed) =
        viewModelScope.launch {
            try {
                dvmFeed.kind?.let {
                    feedsRepository.removeFeedLocally(feedSpec = dvmFeed.buildSpec(specKind = dvmFeed.kind))
                }
                feedsRepository.persistRemotelyAllLocalUserFeeds(userId = activeAccountStore.activeUserId())
            } catch (error: WssException) {
                Timber.w(error)
            }
        }

    private fun fetchAndObserveExploreFeeds() =
        viewModelScope.launch {
            setState { copy(loading = true) }
            try {
                dvmFeedsJob?.cancel()
                dvmFeedsJob = dvmFeedListHandler.fetchDvmFeedsAndObserveStatsUpdates(
                    scope = viewModelScope,
                    userId = activeAccountStore.activeUserId(),
                ) { dvmFeeds ->
                    setState { copy(feeds = dvmFeeds) }
                }
            } catch (error: WssException) {
                Timber.w(error)
            } finally {
                setState { copy(loading = false) }
            }
        }
}
