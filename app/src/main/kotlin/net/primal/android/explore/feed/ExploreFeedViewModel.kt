package net.primal.android.explore.feed

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.cachedIn
import androidx.paging.map
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.getAndUpdate
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import net.primal.android.core.coroutines.CoroutineDispatcherProvider
import net.primal.android.explore.feed.ExploreFeedContract.UiEvent
import net.primal.android.explore.feed.ExploreFeedContract.UiState
import net.primal.android.explore.feed.ExploreFeedContract.UiState.ExploreFeedError
import net.primal.android.feeds.domain.isNotesBookmarkFeedSpec
import net.primal.android.navigation.exploreFeedSpecOrThrow
import net.primal.android.notes.feed.model.asFeedPostUi
import net.primal.android.notes.repository.FeedRepository

@HiltViewModel
class ExploreFeedViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val dispatcherProvider: CoroutineDispatcherProvider,
    private val feedRepository: FeedRepository,
) : ViewModel() {

    private val exploreFeedSpec = savedStateHandle.exploreFeedSpecOrThrow

    private val _state = MutableStateFlow(
        UiState(
            feedSpec = exploreFeedSpec,
            canBeAddedInUserFeeds = !exploreFeedSpec.isNotesBookmarkFeedSpec(),
            notes = feedRepository.feedBySpec(feedSpec = exploreFeedSpec)
                .map { it.map { feed -> feed.asFeedPostUi() } }
                .cachedIn(viewModelScope),
        ),
    )
    val state = _state.asStateFlow()
    private fun setState(reducer: UiState.() -> UiState) {
        _state.getAndUpdate { it.reducer() }
    }

    private val events: MutableSharedFlow<UiEvent> = MutableSharedFlow()
    fun setEvent(event: UiEvent) {
        viewModelScope.launch { events.emit(event) }
    }

    init {
        observeContainsFeed()
        observeEvents()
    }

    @OptIn(DelicateCoroutinesApi::class)
    override fun onCleared() {
        GlobalScope.launch(dispatcherProvider.io()) {
            feedRepository.removeFeedSpec(feedSpec = exploreFeedSpec)
        }
    }

    private fun observeContainsFeed() =
        viewModelScope.launch {
            feedRepository.observeContainsFeed(feedSpec = exploreFeedSpec).collect {
                setState { copy(existsInUserFeeds = it) }
            }
        }

    private fun observeEvents() =
        viewModelScope.launch {
            events.collect {
                when (it) {
                    is UiEvent.AddToUserFeeds -> addToMyFeeds(title = it.title)
                    UiEvent.RemoveFromUserFeeds -> removeFromMyFeeds()
                }
            }
        }

    private suspend fun addToMyFeeds(title: String) {
        setErrorState(error = ExploreFeedError.FailedToAddToFeed(RuntimeException("Api not implemented")))
        // TODO Implement adding user feed in ExploreFeeds
//        try {
//            settingsRepository.addAndPersistUserFeed(
//                userId = activeAccountStore.activeUserId(),
//                name = title,
//                directive = exploreFeedDirective,
//            )
//        } catch (error: WssException) {
//            Timber.w(error)
//            setErrorState(error = ExploreFeedError.FailedToAddToFeed(error))
//        }
    }

    private suspend fun removeFromMyFeeds() {
        setErrorState(error = ExploreFeedError.FailedToRemoveFeed(RuntimeException("Api not implemented")))
        // TODO Implement removing user feed in ExploreFeeds
//        try {
//            settingsRepository.removeAndPersistUserFeed(
//                userId = activeAccountStore.activeUserId(),
//                directive = exploreFeedDirective,
//            )
//        } catch (error: WssException) {
//            Timber.w(error)
//            setErrorState(error = ExploreFeedError.FailedToRemoveFeed(error))
//        }
    }

    private fun setErrorState(error: ExploreFeedError) {
        setState { copy(error = error) }
        viewModelScope.launch {
            delay(2.seconds)
            if (state.value.error == error) {
                setState { copy(error = null) }
            }
        }
    }
}
