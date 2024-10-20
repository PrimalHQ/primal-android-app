package net.primal.android.feeds.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlin.time.Duration.Companion.milliseconds
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.getAndUpdate
import kotlinx.coroutines.launch
import net.primal.android.feeds.db.Feed
import net.primal.android.feeds.domain.DvmFeed
import net.primal.android.feeds.domain.FeedSpecKind
import net.primal.android.feeds.domain.buildSpec
import net.primal.android.feeds.list.FeedListContract.UiEvent
import net.primal.android.feeds.list.FeedListContract.UiState
import net.primal.android.feeds.list.FeedListContract.UiState.FeedMarketplaceStage
import net.primal.android.feeds.list.ui.model.FeedUi
import net.primal.android.feeds.list.ui.model.asFeedPO
import net.primal.android.feeds.list.ui.model.asFeedUi
import net.primal.android.feeds.repository.DvmFeedListHandler
import net.primal.android.feeds.repository.FeedsRepository
import net.primal.android.networking.sockets.errors.WssException
import net.primal.android.notes.repository.FeedRepository
import net.primal.android.user.accounts.active.ActiveAccountStore
import timber.log.Timber

@HiltViewModel(assistedFactory = FeedListViewModel.Factory::class)
class FeedListViewModel @AssistedInject constructor(
    @Assisted activeFeed: FeedUi,
    @Assisted private val specKind: FeedSpecKind,
    private val feedRepository: FeedRepository,
    private val feedsRepository: FeedsRepository,
    private val activeAccountStore: ActiveAccountStore,
    private val dvmFeedListHandler: DvmFeedListHandler,
) : ViewModel() {

    @AssistedFactory
    interface Factory {
        fun create(activeFeed: FeedUi, specKind: FeedSpecKind): FeedListViewModel
    }

    private val _state = MutableStateFlow(UiState(activeFeed = activeFeed, specKind = specKind))
    val state = _state.asStateFlow()
    private fun setState(reducer: UiState.() -> UiState) = _state.getAndUpdate { it.reducer() }

    private val events: MutableSharedFlow<UiEvent> = MutableSharedFlow()
    fun setEvent(event: UiEvent) = viewModelScope.launch { events.emit(event) }

    private var allFeeds: List<FeedUi> = emptyList()
    private var defaultFeeds: List<Feed> = emptyList()

    private var dvmFeedsJob: Job? = null

    init {
        observeEvents()
        observeFeeds()
        fetchAndProcessDefaultFeeds()
        fetchAndObserveLatestFeedMarketplace()
    }

    private fun observeEvents() =
        viewModelScope.launch {
            events.collect {
                when (it) {
                    UiEvent.ShowFeedMarketplace -> setState {
                        copy(
                            feedMarketplaceStage = FeedMarketplaceStage.FeedMarketplace,
                        )
                    }

                    UiEvent.CloseFeedMarketplace -> setState {
                        copy(
                            feedMarketplaceStage = FeedMarketplaceStage.FeedList,
                        )
                    }

                    is UiEvent.ShowFeedDetails -> {
                        setState {
                            copy(
                                selectedDvmFeed = it.dvmFeed,
                                feedMarketplaceStage = FeedMarketplaceStage.FeedDetails,
                            )
                        }
                    }

                    UiEvent.CloseFeedDetails -> {
                        val closingDvmFeed = _state.value.selectedDvmFeed
                        if (closingDvmFeed != null) scheduleClearingDvmFeed(dvmFeed = closingDvmFeed.data)
                        setState { copy(feedMarketplaceStage = FeedMarketplaceStage.FeedMarketplace) }
                    }

                    is UiEvent.AddDvmFeedToUserFeeds -> {
                        addToUserFeeds(dvmFeed = it.dvmFeed.data)
                        setState { copy(feedMarketplaceStage = FeedMarketplaceStage.FeedList) }
                    }

                    is UiEvent.RemoveDvmFeedFromUserFeeds -> {
                        removeFromUserFeeds(spec = it.dvmFeed.data.buildSpec(specKind = specKind))
                        setState { copy(feedMarketplaceStage = FeedMarketplaceStage.FeedList) }
                    }

                    is UiEvent.RemoveFeedFromUserFeeds -> {
                        removeFromUserFeeds(spec = it.spec)
                    }

                    UiEvent.OpenEditMode -> {
                        setState { copy(isEditMode = true) }
                        updateFeedsState()
                    }

                    UiEvent.CloseEditMode -> {
                        setState { copy(isEditMode = false) }
                        updateFeedsState()
                        persistRemotelyFeeds()
                    }

                    is UiEvent.FeedReordered -> {
                        changeAllFeeds(feeds = it.feeds)
                    }

                    is UiEvent.UpdateFeedSpecEnabled -> {
                        updateFeedSpecEnabled(feedSpec = it.feedSpec, enabled = it.enabled)
                    }

                    UiEvent.RestoreDefaultPrimalFeeds -> {
                        restoreDefaultPrimalFeeds()
                    }
                }
            }
        }

    private fun restoreDefaultPrimalFeeds() =
        viewModelScope.launch {
            try {
                feedsRepository.fetchAndPersistDefaultFeeds(
                    userId = activeAccountStore.activeUserId(),
                    givenDefaultFeeds = defaultFeeds,
                    specKind = specKind,
                )
                setState { copy(isEditMode = false) }
                updateFeedsState()
            } catch (error: WssException) {
                Timber.w(error)
            }
        }

    private fun observeFeeds() =
        viewModelScope.launch {
            feedsRepository.observeFeeds(specKind = specKind)
                .collect { feeds ->
                    changeAllFeeds(feeds = feeds.map { it.asFeedUi() })
                }
        }

    private fun changeAllFeeds(feeds: List<FeedUi>) {
        allFeeds = feeds
        updateFeedsState()
    }

    private fun updateFeedSpecEnabled(feedSpec: String, enabled: Boolean) {
        if (allFeeds.count { it.enabled } == 1 && !enabled) return

        val index = allFeeds.indexOfFirst { it.spec == feedSpec }
        if (index != -1) {
            allFeeds = allFeeds.toMutableList().apply {
                this[index] = this[index].copy(enabled = enabled)
            }
        }
        updateFeedsState()
    }

    private fun updateFeedsState() {
        val currentState = _state.value
        if (currentState.isEditMode) {
            setState { copy(feeds = allFeeds) }
        } else {
            setState { copy(feeds = allFeeds.filter { it.enabled }) }
        }
    }

    private fun fetchAndProcessDefaultFeeds() =
        viewModelScope.launch {
            try {
                defaultFeeds = feedsRepository.fetchDefaultFeeds(specKind = specKind) ?: emptyList()
            } catch (error: WssException) {
                Timber.w(error)
            }

            try {
                feedsRepository.persistNewDefaultFeeds(
                    userId = activeAccountStore.activeUserId(),
                    givenDefaultFeeds = defaultFeeds,
                    specKind = specKind,
                )
            } catch (error: WssException) {
                Timber.w(error)
            }
        }

    private fun fetchAndObserveLatestFeedMarketplace() {
        dvmFeedsJob?.cancel()
        dvmFeedsJob = viewModelScope.launch {
            setState { copy(fetchingDvmFeeds = true) }
            try {
                dvmFeedListHandler.fetchDvmFeedsAndObserveStatsUpdates(
                    scope = viewModelScope,
                    userId = activeAccountStore.activeUserId(),
                ) { dvmFeeds ->
                    val updatedSelectedDvmFeed = dvmFeeds.firstOrNull {
                        _state.value.selectedDvmFeed?.data?.eventId == it.data.eventId
                    }
                    setState { copy(dvmFeeds = dvmFeeds, selectedDvmFeed = updatedSelectedDvmFeed) }
                }
            } catch (error: WssException) {
                Timber.w(error)
            } finally {
                setState { copy(fetchingDvmFeeds = false) }
            }
        }
    }

    private fun scheduleClearingDvmFeed(dvmFeed: DvmFeed) =
        viewModelScope.launch {
            delay(400.milliseconds)
            feedRepository.removeFeedSpec(feedSpec = dvmFeed.buildSpec(specKind = specKind))
            setState { copy(selectedDvmFeed = null) }
        }

    private fun addToUserFeeds(dvmFeed: DvmFeed) =
        viewModelScope.launch {
            feedsRepository.addDvmFeedLocally(dvmFeed = dvmFeed, specKind = specKind)
        }

    private fun removeFromUserFeeds(spec: String) =
        viewModelScope.launch {
            allFeeds = allFeeds.toMutableList().apply {
                removeIf { it.spec == spec }
            }
            updateFeedsState()
            feedsRepository.removeFeedLocally(feedSpec = spec)
            persistRemotelyFeeds()
        }

    private fun persistRemotelyFeeds() =
        viewModelScope.launch {
            val currentFeeds = allFeeds.map { it.asFeedPO() }
            try {
                feedsRepository.persistLocallyAndRemotelyUserFeeds(
                    userId = activeAccountStore.activeUserId(),
                    feeds = currentFeeds,
                    specKind = specKind,
                )
            } catch (error: WssException) {
                Timber.w(error)
            }
        }
}
