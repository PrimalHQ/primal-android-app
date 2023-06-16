package net.primal.android.feed

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.cachedIn
import androidx.paging.map
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.getAndUpdate
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import net.primal.android.core.utils.ellipsizeMiddle
import net.primal.android.feed.FeedContract.SideEffect
import net.primal.android.feed.FeedContract.UiEvent
import net.primal.android.feed.FeedContract.UiState
import net.primal.android.feed.db.FeedPost
import net.primal.android.feed.repository.FeedRepository
import net.primal.android.feed.ui.model.FeedPostStatsUi
import net.primal.android.feed.ui.model.FeedPostUi
import net.primal.android.feed.ui.model.FeedPostsSyncStats
import net.primal.android.feed.ui.model.PostResource
import net.primal.android.navigation.feedDirective
import net.primal.android.nostr.ext.asEllipsizedNpub
import net.primal.android.nostr.ext.displayNameUiFriendly
import net.primal.android.settings.SettingsRepository
import java.time.Instant
import javax.inject.Inject

@HiltViewModel
class FeedViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    settingsRepository: SettingsRepository,
    private val feedRepository: FeedRepository,
) : ViewModel() {

    private val feedDirective: String = savedStateHandle.feedDirective
        ?: settingsRepository.defaultFeed

    private val _state = MutableStateFlow(
        UiState(
            posts = feedRepository.feedByDirective(feedDirective = feedDirective)
                .map { it.map { feed -> feed.asFeedPostUi() } }
                .cachedIn(viewModelScope),
        )
    )
    val state = _state.asStateFlow()
    private fun setState(reducer: UiState.() -> UiState) {
        _state.getAndUpdate { it.reducer() }
    }

    private val _effect: Channel<SideEffect> = Channel()
    val effect = _effect.receiveAsFlow()
    private fun setEffect(effect: SideEffect) = viewModelScope.launch {
        _effect.send(effect)
    }

    private val _event: MutableSharedFlow<UiEvent> = MutableSharedFlow()
    fun setEvent(event: UiEvent) {
        viewModelScope.launch { _event.emit(event) }
    }

    init {
        loadFeedTitle()
        subscribeToEvents()
        subscribeToFeedSyncUpdates()
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

    private fun FeedPost.asFeedPostUi() = FeedPostUi(
        postId = this.data.postId,
        repostId = this.data.repostId,
        repostAuthorDisplayName = this.repostAuthor?.displayNameUiFriendly()
            ?: this.data.repostAuthorId?.asEllipsizedNpub(),
        authorDisplayName = this.author?.displayNameUiFriendly()
            ?: this.data.authorId.asEllipsizedNpub(),
        authorInternetIdentifier = this.author?.internetIdentifier,
        authorAvatarUrl = this.author?.picture,
        timestamp = Instant.ofEpochSecond(this.data.createdAt),
        content = this.data.content,
        resources = this.resources.map {
            PostResource(
                url = it.url,
                mimeType = it.contentType,
                variants = it.variants ?: emptyList(),
            )
        },
        stats = FeedPostStatsUi(
            repliesCount = this.postStats?.replies ?: 0,
            userReplied = false,
            zapsCount = this.postStats?.zaps ?: 0,
            satsZapped = this.postStats?.satsZapped ?: 0,
            userZapped = false,
            likesCount = this.postStats?.likes ?: 0,
            userLiked = false,
            repostsCount = this.postStats?.reposts ?: 0,
            userReposted = false,
        )
    )

}