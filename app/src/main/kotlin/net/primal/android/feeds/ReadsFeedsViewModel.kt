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
import net.primal.android.feeds.ReadsFeedsContract.UiEvent
import net.primal.android.feeds.ReadsFeedsContract.UiState
import net.primal.android.feeds.ReadsFeedsContract.UiState.FeedMarketplaceStage
import net.primal.android.feeds.repository.DvmFeed
import net.primal.android.feeds.repository.FeedsRepository
import net.primal.android.feeds.ui.model.FeedUi
import net.primal.android.feeds.ui.model.asArticleFeedDb
import net.primal.android.feeds.ui.model.asFeedUi
import net.primal.android.networking.sockets.errors.WssException
import timber.log.Timber

@HiltViewModel(assistedFactory = ReadsFeedsViewModel.Factory::class)
class ReadsFeedsViewModel @AssistedInject constructor(
    @Assisted activeFeed: FeedUi,
    private val feedsRepository: FeedsRepository,
) : ViewModel() {

    @AssistedFactory
    interface Factory {
        fun create(activeFeed: FeedUi): ReadsFeedsViewModel
    }

    private val _state = MutableStateFlow(UiState(activeFeed = activeFeed))
    val state = _state.asStateFlow()
    private fun setState(reducer: UiState.() -> UiState) = _state.getAndUpdate { it.reducer() }

    private val events: MutableSharedFlow<UiEvent> = MutableSharedFlow()
    fun setEvent(event: UiEvent) = viewModelScope.launch { events.emit(event) }

    private var allFeeds: List<FeedUi> = emptyList()

    init {
        observeEvents()
        observeReadsFeeds()
        fetchDefaultReadsFeeds()
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

                    is UiEvent.AddDvmFeedToUserFeeds -> addToUserFeeds(dvmFeed = it.dvmFeed)
                    is UiEvent.RemoveDvmFeedFromUserFeeds -> removeFromUserFeeds(dvmFeed = it.dvmFeed)

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
                }
            }
        }

    private fun observeReadsFeeds() =
        viewModelScope.launch {
            feedsRepository.observeReadsFeeds().collect { feeds ->
                changeAllFeeds(feeds = feeds.map { it.asFeedUi() })
            }
        }

    private fun changeAllFeeds(feeds: List<FeedUi>) {
        allFeeds = feeds
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
                val dvmFeeds = feedsRepository.fetchRecommendedDvmFeeds()
                setState { copy(dvmFeeds = dvmFeeds) }
            } catch (error: WssException) {
                Timber.w(error)
            } finally {
                setState { copy(fetchingDvmFeeds = false) }
            }
        }

    private fun fetchDefaultReadsFeeds() =
        viewModelScope.launch {
            try {
                val defaultArticleFeeds = feedsRepository.fetchDefaultArticleFeeds()
                if (defaultArticleFeeds != null) {
                    setState { copy(defaultFeeds = defaultArticleFeeds.map { it.asFeedUi() }) }
                }
            } catch (error: WssException) {
                Timber.w(error)
            }
        }

    private fun scheduleClearingDvmFeed(dvmFeed: DvmFeed) =
        viewModelScope.launch {
            delay(400.milliseconds)
            feedsRepository.clearDvmFeed(dvmFeed)
            setState { copy(selectedDvmFeed = null) }
        }

    private fun addToUserFeeds(dvmFeed: DvmFeed) =
        viewModelScope.launch {
            feedsRepository.addDvmFeed(dvmFeed)
        }

    private fun removeFromUserFeeds(dvmFeed: DvmFeed) =
        viewModelScope.launch {
            feedsRepository.removeDvmFeed(dvmFeed)
        }

    private fun persistReadsFeed() =
        viewModelScope.launch {
            val currentFeeds = allFeeds.map { it.asArticleFeedDb() }
            try {
                feedsRepository.persistArticleFeeds(feeds = currentFeeds)
            } catch (error: WssException) {
                Timber.w(error)
            }
        }
}
