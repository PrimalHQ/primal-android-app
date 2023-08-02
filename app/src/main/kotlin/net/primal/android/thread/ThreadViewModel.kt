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
import net.primal.android.networking.relays.errors.NostrPublishException
import net.primal.android.networking.sockets.errors.WssException
import net.primal.android.nostr.ext.asEventIdTag
import net.primal.android.nostr.ext.asPubkeyTag
import net.primal.android.nostr.ext.isPubKeyTag
import net.primal.android.nostr.ext.parseEventTags
import net.primal.android.thread.ThreadContract.UiEvent
import javax.inject.Inject
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

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
                is UiEvent.RepostAction -> repostPost(it)
                is UiEvent.ReplyToAction -> publishReply(it)
                is UiEvent.UpdateReply -> updateReply(it)
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
        if (rootPost != null) {
            setState {
                copy(
                    conversation = listOf(rootPost.asFeedPostUi()),
                    highlightPostId = postId,
                    highlightPostIndex = 0,
                )
            }
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
        } catch (error: NostrPublishException) {
            // Propagate error to the UI
        }
    }

    private fun repostPost(repostAction: UiEvent.RepostAction) = viewModelScope.launch {
        try {
            postRepository.repostPost(
                postId = repostAction.postId,
                postAuthorId = repostAction.postAuthorId,
                postRawNostrEvent = repostAction.postNostrEvent,
            )
        } catch (error: NostrPublishException) {
            // Propagate error to the UI
        }
    }

    private fun updateReply(updateReplyEvent: UiEvent.UpdateReply) {
        setState { copy(replyText = updateReplyEvent.newReply) }
    }

    private fun publishReply(replyToAction: UiEvent.ReplyToAction) = viewModelScope.launch {
        setState { copy(publishingReply = true) }
        try {
            val content = state.value.replyText

            val replyPostData = withContext(Dispatchers.IO) {
                postRepository.findPostDataById(postId = replyToAction.replyToPostId)
            }
            val existingPubkeyTags = replyPostData?.tags?.filter { it.isPubKeyTag() }?.toSet() ?: setOf()
            val replyAuthorPubkeyTag = replyToAction.replyToAuthorId.asPubkeyTag()

            val rootEventTag = replyToAction.rootPostId.asEventIdTag(marker = "root")
            val replyEventTag = if (replyToAction.rootPostId != replyToAction.replyToPostId) {
                replyToAction.replyToPostId.asEventIdTag(marker = "reply")
            } else null
            val mentionEventTags = content.parseEventTags(marker = "mention")

            postRepository.publishShortTextNote(
                content = content,
                eventTags = setOfNotNull(rootEventTag, replyEventTag) + mentionEventTags,
                pubkeyTags = existingPubkeyTags + setOf(replyAuthorPubkeyTag),
            )
            scheduleFetchReplies()
            setState { copy(replyText = "") }
        } catch (error: NostrPublishException) {
            setState { copy(publishingError = ThreadContract.UiState.PublishError(cause = error.cause)) }
            scheduleErrorClear()
        } finally {
            setState { copy(publishingReply = false) }
        }
    }

    private fun scheduleErrorClear() = viewModelScope.launch {
        delay(2.seconds)
        setState { copy(publishingError = null) }
    }

    private fun scheduleFetchReplies() = viewModelScope.launch {
        delay(500.milliseconds)
        fetchRepliesFromNetwork()
    }
}
