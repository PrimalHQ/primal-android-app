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
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.getAndUpdate
import kotlinx.coroutines.flow.map
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
import net.primal.android.nostr.ext.parseHashtagTags
import net.primal.android.nostr.ext.parsePubkeyTags
import net.primal.android.thread.ThreadContract.UiEvent
import net.primal.android.thread.ThreadContract.UiState.ThreadError
import net.primal.android.user.accounts.active.ActiveAccountStore
import net.primal.android.user.accounts.active.ActiveUserAccountState
import net.primal.android.wallet.model.ZapTarget
import net.primal.android.wallet.repository.ZapRepository
import javax.inject.Inject
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

@HiltViewModel
class ThreadViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val activeAccountStore: ActiveAccountStore,
    private val feedRepository: FeedRepository,
    private val postRepository: PostRepository,
    private val zapRepository: ZapRepository
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
        observeActiveAccount()
        fetchRepliesFromNetwork()
    }

    private fun observeEvents() = viewModelScope.launch {
        _event.collect {
            when (it) {
                is UiEvent.PostLikeAction -> likePost(it)
                is UiEvent.RepostAction -> repostPost(it)
                is UiEvent.ReplyToAction -> publishReply(it)
                is UiEvent.UpdateReply -> updateReply(it)
                is UiEvent.ZapAction -> zapPost(it)
            }
        }
    }

    private fun observeConversation() = viewModelScope.launch {
        loadHighlightedPost()
        delayShortlyToPropagateHighlightedPost()
        subscribeToConversationChanges()
    }

    private fun observeActiveAccount() = viewModelScope.launch {
        activeAccountStore.activeAccountState
            .filterIsInstance<ActiveUserAccountState.ActiveUserAccount>()
            .collect {
                setState {
                    copy(walletConnected = it.data.nostrWallet != null)
                }
            }
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
            .map { posts -> posts.map { it.asFeedPostUi() } }
            .collect { conversation ->
                val highlightPostIndex = conversation.indexOfFirst { it.postId == postId }
                val thread = conversation.subList(0, highlightPostIndex + 1)
                val replies = conversation.subList(highlightPostIndex + 1, conversation.size)
                setState {
                    copy(
                        conversation = thread + replies.sortedByDescending { it.timestamp },
                        highlightPostIndex = highlightPostIndex,
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
            setErrorState(error = ThreadError.FailedToPublishLikeEvent(error))
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
            setErrorState(error = ThreadError.FailedToPublishRepostEvent(error))
        }
    }

    private fun zapPost(zapAction: UiEvent.ZapAction) = viewModelScope.launch {
        if (zapAction.postAuthorLightningAddress == null) {
            setErrorState(error = ThreadError.MissingLightningAddress(IllegalStateException()))
            return@launch
        }

        try {
            zapRepository.zap(
                userId = activeAccountStore.activeUserId(),
                comment = zapAction.zapDescription ?: "",
                amountInSats = zapAction.zapAmount ?: 42,
                target = ZapTarget.Note(
                    zapAction.postId,
                    zapAction.postAuthorId,
                    zapAction.postAuthorLightningAddress
                ),
            )
        } catch (error: ZapRepository.ZapFailureException) {
            setErrorState(error = ThreadError.FailedToPublishZapEvent(error))
        } catch (error: NostrPublishException) {
            setErrorState(error = ThreadError.FailedToPublishZapEvent(error))
        } catch (error: ZapRepository.InvalidZapRequestException) {
            setErrorState(error = ThreadError.InvalidZapRequest(error))
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
            val existingPubkeyTags =
                replyPostData?.tags?.filter { it.isPubKeyTag() }?.toSet() ?: setOf()
            val replyAuthorPubkeyTag = replyToAction.replyToAuthorId.asPubkeyTag()
            val mentionedPubkeyTags = content.parsePubkeyTags(marker = "mention").toSet()
            val pubkeyTags = existingPubkeyTags + setOf(replyAuthorPubkeyTag) + mentionedPubkeyTags

            val rootEventTag = replyToAction.rootPostId.asEventIdTag(marker = "root")
            val replyEventTag = if (replyToAction.rootPostId != replyToAction.replyToPostId) {
                replyToAction.replyToPostId.asEventIdTag(marker = "reply")
            } else null
            val mentionEventTags = content.parseEventTags(marker = "mention")
            val eventTags = setOfNotNull(rootEventTag, replyEventTag) + mentionEventTags

            val hashtagTags = content.parseHashtagTags().toSet()

            postRepository.publishShortTextNote(
                content = content,
                tags = pubkeyTags + eventTags + hashtagTags,
            )
            scheduleFetchReplies()
            setState { copy(replyText = "") }
        } catch (error: NostrPublishException) {
            setErrorState(error = ThreadError.FailedToPublishReplyEvent(error))
        } finally {
            setState { copy(publishingReply = false) }
        }
    }

    private fun scheduleFetchReplies() = viewModelScope.launch {
        delay(500.milliseconds)
        fetchRepliesFromNetwork()
    }

    private fun setErrorState(error: ThreadError) {
        setState { copy(error = error) }
        viewModelScope.launch {
            delay(2.seconds)
            if (state.value.error == error) {
                setState { copy(error = null) }
            }
        }
    }
}
