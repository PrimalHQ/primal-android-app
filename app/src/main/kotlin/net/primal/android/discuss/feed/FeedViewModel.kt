package net.primal.android.discuss.feed

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.cachedIn
import androidx.paging.map
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.getAndUpdate
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import net.primal.android.core.compose.feed.asFeedPostUi
import net.primal.android.core.compose.feed.model.FeedPostsSyncStats
import net.primal.android.core.utils.ellipsizeMiddle
import net.primal.android.discuss.feed.FeedContract.UiEvent
import net.primal.android.discuss.feed.FeedContract.UiState
import net.primal.android.feed.repository.FeedRepository
import net.primal.android.navigation.feedDirective
import net.primal.android.profile.repository.ProfileRepository
import net.primal.android.user.active.ActiveAccountStore
import net.primal.android.user.active.ActiveUserAccountState
import java.time.Instant
import javax.inject.Inject

@HiltViewModel
class FeedViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val feedRepository: FeedRepository,
    private val activeAccountStore: ActiveAccountStore,
    private val profileRepository: ProfileRepository,
) : ViewModel() {

    private val feedDirective: String = savedStateHandle.feedDirective ?: "network;trending"

    private val _state = MutableStateFlow(
        UiState(
            posts = feedRepository.feedByDirective(feedDirective = feedDirective)
                .map { pagingData ->
                    pagingData
                        .map { post ->
                            post.asFeedPostUi()
                        }
                }
                .cachedIn(viewModelScope),
        )
    )
    val state = _state.asStateFlow()
    private fun setState(reducer: UiState.() -> UiState) {
        _state.getAndUpdate { it.reducer() }
    }

    private val _event: MutableSharedFlow<UiEvent> = MutableSharedFlow()
    fun setEvent(event: UiEvent) {
        viewModelScope.launch { _event.emit(event) }
    }

    init {
        loadFeedTitle()
        subscribeToEvents()
        subscribeToFeedSyncUpdates()
        subscribeToActiveAccount()
    }

    private fun loadFeedTitle() = viewModelScope.launch {
        val feed = feedRepository.findFeedByDirective(feedDirective = feedDirective)
        setState {
            copy(feedTitle = feed?.name ?: feedDirective.ellipsizeMiddle(size = 8))
        }
    }

    private fun subscribeToFeedSyncUpdates() = viewModelScope.launch {
        feedRepository.observeNewFeedPostsSyncUpdates(
            feedDirective = feedDirective,
            since = Instant.now().epochSecond
        ).collect { syncData ->
            val limit = if (syncData.count <= 3) syncData.count else 3
            val newPosts = withContext(Dispatchers.IO) {
                feedRepository.findNewestPosts(
                    feedDirective = feedDirective,
                    limit = syncData.count
                )
                    .filter { it.author?.picture != null }
                    .distinctBy { it.author?.ownerId }
                    .take(limit)
            }
            setState {
                copy(
                    syncStats = FeedPostsSyncStats(
                        postsCount = this.syncStats.postsCount + syncData.count,
                        postIds = this.syncStats.postIds + syncData.postIds,
                        avatarUrls = newPosts.mapNotNull { feedPost ->
                            feedPost.author?.picture
                        }
                    )
                )
            }
        }
    }

    private fun subscribeToActiveAccount() = viewModelScope.launch {
        activeAccountStore.activeAccountState
            .filterIsInstance<ActiveUserAccountState.ActiveUserAccount>()
            .collect {
                setState {
                    copy(activeAccountAvatarUrl = it.data.pictureUrl)
                }
            }
    }

    private fun subscribeToEvents() = viewModelScope.launch {
        _event.collect {
            when (it) {
                UiEvent.FeedScrolledToTop -> clearSyncStats()
            }
        }
    }

    private fun clearSyncStats() {
        setState {
            copy(
                syncStats = this.syncStats.copy(
                    postIds = emptyList(),
                    postsCount = 0
                )
            )
        }
    }

}