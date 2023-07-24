package net.primal.android.thread

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.getAndUpdate
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import net.primal.android.core.compose.feed.asFeedPostUi
import net.primal.android.feed.repository.FeedRepository
import net.primal.android.feed.repository.PostRepository
import net.primal.android.navigation.postId
import net.primal.android.networking.sockets.errors.WssException
import net.primal.android.thread.ThreadContract.UiEvent
import javax.inject.Inject

@HiltViewModel
class ThreadViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val feedRepository: FeedRepository,
    private val postRepository: PostRepository,
) : ViewModel() {

    private val postId = savedStateHandle.postId

    private val _state = MutableStateFlow(ThreadContract.UiState())
    val state = _state.asStateFlow()
    private fun setState(reducer: ThreadContract.UiState.() -> ThreadContract.UiState) {
        _state.getAndUpdate { it.reducer() }
    }

    private val _event: MutableSharedFlow<UiEvent> = MutableSharedFlow()
    fun setEvent(event: UiEvent) {
        viewModelScope.launch { _event.emit(event) }
    }

    init {
        observeEvents()
        observeConversation()
        fetchRepliesFromNetwork()
    }

    private fun observeEvents() = viewModelScope.launch {
        _event.collect {
            when (it) {
                is UiEvent.PostLikeAction -> likePost(it)
            }
        }
    }

    private fun observeConversation() = viewModelScope.launch {
        loadHighlightedPost()
        delayShortlyToPropagateHighlightedPost()
        subscribeToConversationChanges()
    }

    private suspend fun loadHighlightedPost() {
        val rootPost = withContext(Dispatchers.IO) { feedRepository.findPostById(postId = postId) }
        setState {
            copy(
                conversation = listOf(rootPost.asFeedPostUi()),
                highlightPostIndex = 0,
            )
        }
    }

    private suspend fun delayShortlyToPropagateHighlightedPost() = delay(100)

    private suspend fun subscribeToConversationChanges() {
        feedRepository.observeConversation(postId = postId)
            .filter { it.isNotEmpty() }
            .collect { conversation ->
                setState {
                    copy(
                        conversation = conversation.map { it.asFeedPostUi() },
                        highlightPostIndex = conversation.indexOfFirst { it.data.postId == postId },
                    )
                }
            }
    }

    private fun fetchRepliesFromNetwork() = viewModelScope.launch {
        try {
            feedRepository.fetchReplies(postId = postId)
        } catch (error: WssException) {
            // Ignore
        }
    }

    private fun likePost(postLikeAction: UiEvent.PostLikeAction) = viewModelScope.launch {
        try {
            postRepository.likePost(
                postId = postLikeAction.postId,
                postAuthorId = postLikeAction.postAuthorId,
            )
        } catch (error: PostRepository.FailedToPublishLikeEvent) {
            // Propagate error to the UI
        }
    }
}
