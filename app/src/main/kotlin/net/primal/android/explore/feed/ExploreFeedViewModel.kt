package net.primal.android.explore.feed

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
import kotlinx.coroutines.launch
import net.primal.android.core.coroutines.CoroutineDispatcherProvider
import net.primal.android.explore.feed.ExploreFeedContract.UiEvent
import net.primal.android.explore.feed.ExploreFeedContract.UiState
import net.primal.android.explore.feed.ExploreFeedContract.UiState.ExploreFeedError
import net.primal.android.feeds.repository.FeedsRepository
import net.primal.android.navigation.advancedSearchFeedSpec
import net.primal.android.navigation.exploreFeedSpec
import net.primal.android.navigation.renderType
import net.primal.android.nostr.notary.exceptions.MissingPrivateKey
import net.primal.android.notes.repository.FeedRepository
import net.primal.android.user.accounts.active.ActiveAccountStore
import net.primal.core.networking.sockets.errors.WssException
import net.primal.domain.FEED_KIND_SEARCH
import net.primal.domain.buildAdvancedSearchFeedSpec
import net.primal.domain.resolveFeedSpecKind
import timber.log.Timber

@HiltViewModel
class ExploreFeedViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val dispatcherProvider: CoroutineDispatcherProvider,
    private val feedRepository: FeedRepository,
    private val feedsRepository: FeedsRepository,
    private val activeAccountStore: ActiveAccountStore,
) : ViewModel() {

    private val feedSpec = savedStateHandle.exploreFeedSpec
        ?: savedStateHandle.advancedSearchFeedSpec?.buildAdvancedSearchFeedSpec()
        ?: error("no feed spec provided.")

    private val renderType = ExploreFeedContract.RenderType.valueOf(savedStateHandle.renderType)

    private val _state = MutableStateFlow(
        UiState(
            feedSpec = feedSpec,
            feedSpecKind = feedSpec.resolveFeedSpecKind(),
            renderType = renderType,
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
            feedRepository.removeFeedSpec(userId = activeAccountStore.activeUserId(), feedSpec = feedSpec)
        }
    }

    private fun observeContainsFeed() =
        viewModelScope.launch {
            feedsRepository.observeContainsFeedSpec(
                userId = activeAccountStore.activeUserId(),
                feedSpec = feedSpec,
            ).collect {
                setState { copy(existsInUserFeeds = it) }
            }
        }

    private fun observeEvents() =
        viewModelScope.launch {
            events.collect {
                when (it) {
                    is UiEvent.AddToUserFeeds -> addToMyFeeds(it)
                    UiEvent.RemoveFromUserFeeds -> removeFromMyFeeds()
                }
            }
        }

    private suspend fun addToMyFeeds(event: UiEvent.AddToUserFeeds) {
        try {
            val feedSpecKind = feedSpec.resolveFeedSpecKind()
            if (feedSpecKind != null) {
                feedsRepository.addFeedLocally(
                    userId = activeAccountStore.activeUserId(),
                    feedSpec = feedSpec,
                    title = event.title,
                    description = event.description,
                    feedSpecKind = feedSpecKind,
                    feedKind = FEED_KIND_SEARCH,
                )
                feedsRepository.persistRemotelyAllLocalUserFeeds(userId = activeAccountStore.activeUserId())
            }
        } catch (error: MissingPrivateKey) {
            Timber.w(error)
        } catch (error: WssException) {
            Timber.w(error)
            setErrorState(error = ExploreFeedError.FailedToAddToFeed(error))
        }
    }

    private suspend fun removeFromMyFeeds() {
        try {
            feedsRepository.removeFeedLocally(userId = activeAccountStore.activeUserId(), feedSpec = feedSpec)
            feedsRepository.persistRemotelyAllLocalUserFeeds(userId = activeAccountStore.activeUserId())
        } catch (error: MissingPrivateKey) {
            Timber.w(error)
        } catch (error: WssException) {
            Timber.w(error)
            setErrorState(error = ExploreFeedError.FailedToRemoveFeed(error))
        }
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
