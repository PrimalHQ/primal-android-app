package net.primal.android.feeds

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlin.time.Duration.Companion.milliseconds
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.getAndUpdate
import kotlinx.coroutines.launch
import net.primal.android.feeds.FeedsContract.UiEvent
import net.primal.android.feeds.FeedsContract.UiState
import net.primal.android.feeds.FeedsContract.UiState.FeedMarketplaceStage
import net.primal.android.feeds.domain.DvmFeed
import net.primal.android.feeds.domain.FeedSpecKind
import net.primal.android.feeds.domain.buildSpec
import net.primal.android.feeds.repository.FeedsRepository
import net.primal.android.feeds.ui.model.FeedUi
import net.primal.android.feeds.ui.model.asFeedPO
import net.primal.android.feeds.ui.model.asFeedUi
import net.primal.android.networking.sockets.errors.WssException
import timber.log.Timber

@HiltViewModel(assistedFactory = FeedsViewModel.Factory::class)
class FeedsViewModel @AssistedInject constructor(
    @Assisted activeFeed: FeedUi,
    @Assisted private val specKind: FeedSpecKind,
    private val feedsRepository: FeedsRepository,
) : ViewModel() {

    @AssistedFactory
    interface Factory {
        fun create(activeFeed: FeedUi, specKind: FeedSpecKind): FeedsViewModel
    }

    private val _state = MutableStateFlow(UiState(activeFeed = activeFeed, specKind = specKind))
    val state = _state.asStateFlow()
    private fun setState(reducer: UiState.() -> UiState) = _state.getAndUpdate { it.reducer() }

    private val events: MutableSharedFlow<UiEvent> = MutableSharedFlow()
    fun setEvent(event: UiEvent) = viewModelScope.launch { events.emit(event) }

    private var allFeeds: List<FeedUi> = emptyList()

    init {
        observeEvents()
        observeFeeds()
        persistNewDefaultFeeds()
        fetchLatestFeedMarketplace()
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
                        if (closingDvmFeed != null) scheduleClearingDvmFeed(dvmFeed = closingDvmFeed)
                        setState { copy(feedMarketplaceStage = FeedMarketplaceStage.FeedMarketplace) }
                    }

                    is UiEvent.AddDvmFeedToUserFeeds -> {
                        addToUserFeeds(dvmFeed = it.dvmFeed)
                        setState { copy(feedMarketplaceStage = FeedMarketplaceStage.FeedList) }
                    }

                    is UiEvent.RemoveDvmFeedFromUserFeeds -> {
                        removeFromUserFeeds(spec = it.dvmFeed.buildSpec(specKind = specKind))
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
                        persistReadsFeed()
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
                feedsRepository.fetchAndPersistDefaultFeeds(FeedSpecKind.Notes)
                setState { copy(isEditMode = false) }
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

    private fun fetchLatestFeedMarketplace() =
        viewModelScope.launch {
            setState { copy(fetchingDvmFeeds = true) }
            try {
                val dvmFeeds = feedsRepository.fetchRecommendedDvmFeeds(specKind = specKind)
                setState { copy(dvmFeeds = dvmFeeds) }
            } catch (error: WssException) {
                Timber.w(error)
            } finally {
                setState { copy(fetchingDvmFeeds = false) }
            }
        }

    private fun persistNewDefaultFeeds() =
        viewModelScope.launch {
            try {
                feedsRepository.persistNewDefaultFeeds(specKind = specKind)
            } catch (error: WssException) {
                Timber.w(error)
            }
        }

    private fun scheduleClearingDvmFeed(dvmFeed: DvmFeed) =
        viewModelScope.launch {
            delay(400.milliseconds)
            feedsRepository.clearReadsDvmFeed(dvmFeed = dvmFeed, specKind = specKind)
            setState { copy(selectedDvmFeed = null) }
        }

    private fun addToUserFeeds(dvmFeed: DvmFeed) =
        viewModelScope.launch {
            feedsRepository.addReadsDvmFeed(dvmFeed = dvmFeed, specKind = specKind)
        }

    private fun removeFromUserFeeds(spec: String) =
        viewModelScope.launch {
            allFeeds = allFeeds.toMutableList().apply {
                removeIf { it.spec == spec }
            }
            updateFeedsState()
            feedsRepository.removeFeed(feedSpec = spec)
            persistReadsFeed()
        }

    private fun persistReadsFeed() =
        viewModelScope.launch {
            val currentFeeds = allFeeds.map { it.asFeedPO() }
            try {
                feedsRepository.persistArticleFeeds(feeds = currentFeeds, specKind = specKind)
            } catch (error: WssException) {
                Timber.w(error)
            }
        }
}
