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
import net.primal.android.feed.ui.FeedPostUi
import javax.inject.Inject

@HiltViewModel
class FeedViewModel @Inject constructor(
    private val feedRepository: FeedRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(
        UiState(
            posts = feedRepository.feedPaged().map {
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
        subscribeToEventCount()
        fetchLatestPosts()
    }

    private fun subscribeToEventCount() = viewModelScope.launch {
        feedRepository.observeEventsCount().collect {
            setState { copy(feedPostsCount = it) }
        }
    }

    private fun fetchLatestPosts() = viewModelScope.launch {
        feedRepository.fetchLatestPosts()
    }

    private fun FeedPost.asFeedPostUi() = FeedPostUi(
        postId = this.data.postId,
        name = this.author?.displayName,
        content = this.data.content,
    )
}