package net.primal.android.feed.thread

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.getAndUpdate
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import net.primal.android.feed.repository.FeedRepository
import net.primal.android.feed.shared.asFeedPostUi
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
        loadInitialPost()
        observeConversation()
        fetchRepliesFromNetwork()
    }

    private fun loadInitialPost() = viewModelScope.launch {
        val rootPost = withContext(Dispatchers.IO) { repository.findPostById(postId = postId) }
        setState {
            copy(highlightPost = rootPost.asFeedPostUi())
        }
    }

    private fun observeConversation() = viewModelScope.launch {
        repository.observeConversation(postId = postId).collect { conversation ->
            setState {
                val newConversation = conversation.map { it.asFeedPostUi() }
                val highlightIndex = conversation.indexOfFirst { it.data.postId == postId }
                copy(
                    highlightPostIndex = highlightIndex,
                    conversation = newConversation,
                )
            }
        }
    }

    private fun fetchRepliesFromNetwork() = viewModelScope.launch {
        repository.fetchReplies(postId = postId)
    }

}
