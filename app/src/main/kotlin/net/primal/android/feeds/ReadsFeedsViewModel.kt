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
import net.primal.android.articles.db.ArticleFeed
import net.primal.android.feeds.ReadsFeedsContract.UiEvent
import net.primal.android.feeds.ReadsFeedsContract.UiState
import net.primal.android.feeds.ReadsFeedsContract.UiState.FeedMarketplaceStage
import net.primal.android.feeds.repository.DvmFeed
import net.primal.android.feeds.repository.FeedsRepository
import net.primal.android.feeds.ui.model.FeedUi
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

    init {
        observeEvents()
        observeReadsFeeds()
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
                    }
                    UiEvent.CloseEditMode -> {
                        setState { copy(isEditMode = false) }
                        // TODO Save new feed
                    }
                    is UiEvent.FeedReordered -> {
                        setState { copy(feeds = it.feeds) }
                    }
                }
            }
        }

    private fun observeReadsFeeds() =
        viewModelScope.launch {
            feedsRepository.observeReadsFeeds().collect { feeds ->
                setState { copy(feeds = feeds.map { it.asFeedUi() }) }
            }
        }

    private fun ArticleFeed.asFeedUi() =
        FeedUi(
            directive = this.spec,
            name = this.name,
            description = this.description,
        )

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
}
