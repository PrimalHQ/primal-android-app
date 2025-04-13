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
import net.primal.android.feeds.DvmFeedListHandler
import net.primal.android.feeds.dvm.ui.DvmFeedUi
import net.primal.android.user.accounts.active.ActiveAccountStore
import net.primal.core.networking.sockets.errors.WssException
import net.primal.domain.feeds.DvmFeed
import net.primal.domain.feeds.FeedsRepository
import net.primal.domain.feeds.buildSpec
import net.primal.domain.nostr.cryptography.SignatureException
import net.primal.domain.posts.FeedRepository
import timber.log.Timber

@HiltViewModel
class ExploreFeedsViewModel @Inject constructor(
    private val activeAccountStore: ActiveAccountStore,
    private val feedsRepository: FeedsRepository,
    private val feedRepository: FeedRepository,
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
            feedsRepository.observeAllFeeds(userId = activeAccountStore.activeUserId())
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
                    is ExploreFeedsContract.UiEvent.ClearDvmFeed -> scheduleClearingDvmFeed(it.dvmFeed)
                }
            }
        }

    private fun scheduleClearingDvmFeed(dvmFeed: DvmFeedUi) =
        viewModelScope.launch {
            dvmFeed.data.kind?.let {
                feedRepository.removeFeedSpec(
                    userId = activeAccountStore.activeUserId(),
                    feedSpec = dvmFeed.data.buildSpec(specKind = it),
                )
            }
        }

    private fun addToUserFeeds(dvmFeed: DvmFeed) =
        viewModelScope.launch {
            try {
                val userId = activeAccountStore.activeUserId()
                dvmFeed.kind?.let { feedSpecKind ->
                    feedsRepository.addDvmFeedLocally(
                        userId = userId,
                        dvmFeed = dvmFeed,
                        specKind = feedSpecKind,
                    )
                }
                feedsRepository.persistRemotelyAllLocalUserFeeds(userId = userId)
            } catch (error: SignatureException) {
                Timber.w(error)
            } catch (error: WssException) {
                Timber.w(error)
            }
        }

    private fun removeFromUserFeeds(dvmFeed: DvmFeed) =
        viewModelScope.launch {
            try {
                val userId = activeAccountStore.activeUserId()
                dvmFeed.kind?.let { feedSpecKind ->
                    feedsRepository.removeFeedLocally(
                        userId = userId,
                        feedSpec = dvmFeed.buildSpec(specKind = feedSpecKind),
                    )
                }
                feedsRepository.persistRemotelyAllLocalUserFeeds(userId = userId)
            } catch (error: SignatureException) {
                Timber.w(error)
            } catch (error: WssException) {
                Timber.w(error)
            }
        }

    private fun fetchAndObserveExploreFeeds() {
        dvmFeedsJob?.cancel()
        dvmFeedsJob = viewModelScope.launch {
            setState { copy(loading = true) }
            try {
                dvmFeedListHandler.fetchDvmFeedsAndObserveStatsUpdates(
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
}
