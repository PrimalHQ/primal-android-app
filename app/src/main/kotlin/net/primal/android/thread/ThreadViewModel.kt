package net.primal.android.thread

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.getAndUpdate
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import net.primal.android.feed.repository.FeedRepository
import net.primal.android.core.compose.feed.asFeedPostUi
import net.primal.android.navigation.postId
import javax.inject.Inject

@HiltViewModel
class ThreadViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val repository: FeedRepository,
) : ViewModel() {

    private val postId = savedStateHandle.postId

    private val _state = MutableStateFlow(ThreadContract.UiState())
    val state = _state.asStateFlow()
    private fun setState(reducer: ThreadContract.UiState.() -> ThreadContract.UiState) {
        _state.getAndUpdate { it.reducer() }
    }

    init {
        observeConversation()
        fetchRepliesFromNetwork()
    }

    private fun observeConversation() = viewModelScope.launch {
        loadHighlightedPost()
        delayShortlyToPropagateHighlightedPost()
        subscribeToConversationChanges()
    }

    private suspend fun loadHighlightedPost() {
        val rootPost = withContext(Dispatchers.IO) { repository.findPostById(postId = postId) }
        setState {
            copy(
                conversation = listOf(rootPost.asFeedPostUi()),
                highlightPostIndex = 0,
            )
        }
    }

    private suspend fun delayShortlyToPropagateHighlightedPost() = delay(100)

    private suspend fun subscribeToConversationChanges() {
        repository.observeConversation(postId = postId)
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
        repository.fetchReplies(postId = postId)
    }

}
