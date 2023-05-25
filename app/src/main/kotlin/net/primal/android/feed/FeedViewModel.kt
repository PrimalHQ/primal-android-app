package net.primal.android.feed

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.map
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.getAndUpdate
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import net.primal.android.feed.FeedContract.SideEffect
import net.primal.android.feed.FeedContract.UiEvent
import net.primal.android.feed.FeedContract.UiState
import net.primal.android.feed.db.FeedPost
import net.primal.android.feed.repository.FeedRepository
import net.primal.android.feed.ui.model.FeedPostStatsUi
import net.primal.android.feed.ui.model.FeedPostUi
import net.primal.android.nostr.ext.displayNameUiFriendly
import java.time.Instant
import javax.inject.Inject

@HiltViewModel
class FeedViewModel @Inject constructor(
    private val feedRepository: FeedRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(
        UiState(
            posts = feedRepository.feedByFeedHexPaged(
                feedHex = "82341f882b6eabcd2ba7f1ef90aad961cf074af15b9ef44a09f9d2a8fbfbe6a2"
            ).map {
                it.map { feed -> feed.asFeedPostUi() }
            }
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
        fetchLatestPosts()
    }

    private fun fetchLatestPosts() = viewModelScope.launch {
        feedRepository.fetchDefaultAppSettings()
        feedRepository.fetchLatestPosts(
            feedHex = "82341f882b6eabcd2ba7f1ef90aad961cf074af15b9ef44a09f9d2a8fbfbe6a2"
        )
    }

    private fun FeedPost.asFeedPostUi() = FeedPostUi(
        postId = this.data.postId,
        repostId = this.data.repostId,
        repostAuthorDisplayName = this.repostAuthor?.displayNameUiFriendly(),
        authorDisplayName = this.author.displayNameUiFriendly(),
        authorInternetIdentifier = this.author.internetIdentifier,
        authorAvatarUrl = this.author.picture,
        timestamp = Instant.ofEpochSecond(this.data.createdAt),
        content = this.data.content,
        urls = this.data.urls,
        stats = FeedPostStatsUi(
            repliesCount = this.postStats?.replies ?: 0,
            userReplied = false,
            zapsCount = this.postStats?.zaps ?: 0,
            userZapped = false,
            likesCount = this.postStats?.likes ?: 0,
            userLiked = false,
            repostsCount = this.postStats?.reposts ?: 0,
            userReposted = false,
        )
    )
}