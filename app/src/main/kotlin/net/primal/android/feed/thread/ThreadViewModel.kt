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

    private val _state = MutableStateFlow(
        ThreadContract.UiState(
            loading = true,
        )
    )
    val state = _state.asStateFlow()
    private fun setState(reducer: ThreadContract.UiState.() -> ThreadContract.UiState) {
        _state.getAndUpdate { it.reducer() }
    }

    init {
        observeConversation()
        fetchRepliesFromNetwork()
    }

    private fun observeConversation() = viewModelScope.launch {
        val rootPost = withContext(Dispatchers.IO) { repository.findPostById(postId =  postId) }
        setState {
            copy(rootPost = rootPost.asFeedPostUi())
        }

        repository.observeConversation(postId = postId).collect { conversation ->
            val posts = conversation.map { it.asFeedPostUi() }
            val rootIndex = posts.indexOfFirst { it.postId == postId }
            val precedingReplies = posts
                .subList(0, rootIndex.coerceAtLeast(0))
                .sortedBy { it.timestamp }

            val succeedingReplies = posts
                .subList(rootIndex.coerceAtLeast(0), posts.size)
                .drop(1)
                .sortedByDescending { it.timestamp }

            setState {
                copy(
                    precedingReplies = precedingReplies,
                    succeedingReplies = succeedingReplies,
                )
            }
        }
    }

    private fun fetchRepliesFromNetwork() = viewModelScope.launch {
        repository.fetchReplies(postId = postId)
    }

}
