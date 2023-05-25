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
import net.primal.android.nostr.ext.asEllipsizedNpub
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
                feedHex = "9a500dccc084a138330a1d1b2be0d5e86394624325d25084d3eca164e7ea698a"
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
            feedHex = "9a500dccc084a138330a1d1b2be0d5e86394624325d25084d3eca164e7ea698a"
        )
    }

    private fun FeedPost.asFeedPostUi() = FeedPostUi(
        postId = this.data.postId,
        repostId = this.data.repostId,
        repostAuthorDisplayName = this.repostAuthor?.displayNameUiFriendly() ?: this.data.repostAuthorId?.asEllipsizedNpub(),
        authorDisplayName = this.author?.displayNameUiFriendly() ?: this.data.authorId.asEllipsizedNpub(),
        authorInternetIdentifier = this.author?.internetIdentifier,
        authorAvatarUrl = this.author?.picture,
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