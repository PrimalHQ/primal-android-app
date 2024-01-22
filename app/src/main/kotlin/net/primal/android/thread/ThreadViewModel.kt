package net.primal.android.thread

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds
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
import net.primal.android.core.compose.feed.model.asFeedPostUi
import net.primal.android.core.coroutines.CoroutineDispatcherProvider
import net.primal.android.feed.repository.FeedRepository
import net.primal.android.feed.repository.PostRepository
import net.primal.android.navigation.noteIdOrThrow
import net.primal.android.networking.relays.errors.MissingRelaysException
import net.primal.android.networking.relays.errors.NostrPublishException
import net.primal.android.networking.sockets.errors.WssException
import net.primal.android.profile.repository.ProfileRepository
import net.primal.android.settings.muted.repository.MutedUserRepository
import net.primal.android.thread.ThreadContract.UiEvent
import net.primal.android.thread.ThreadContract.UiState.ThreadError
import net.primal.android.user.accounts.active.ActiveAccountStore
import net.primal.android.user.accounts.active.ActiveUserAccountState
import net.primal.android.wallet.domain.ZapTarget
import net.primal.android.wallet.zaps.InvalidZapRequestException
import net.primal.android.wallet.zaps.ZapFailureException
import net.primal.android.wallet.zaps.ZapHandler
import net.primal.android.wallet.zaps.hasWallet
import timber.log.Timber

@HiltViewModel
class ThreadViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val dispatcherProvider: CoroutineDispatcherProvider,
    private val activeAccountStore: ActiveAccountStore,
    private val feedRepository: FeedRepository,
    private val postRepository: PostRepository,
    private val profileRepository: ProfileRepository,
    private val zapHandler: ZapHandler,
    private val mutedUserRepository: MutedUserRepository,
) : ViewModel() {

    private val postId = savedStateHandle.noteIdOrThrow

    private val _state = MutableStateFlow(ThreadContract.UiState(highlightPostId = postId))
    val state = _state.asStateFlow()
    private fun setState(reducer: ThreadContract.UiState.() -> ThreadContract.UiState) {
        _state.getAndUpdate { it.reducer() }
    }

    private val events: MutableSharedFlow<UiEvent> = MutableSharedFlow()
    fun setEvent(event: UiEvent) {
        viewModelScope.launch { events.emit(event) }
    }

    init {
        observeEvents()
        observeConversation()
        observeActiveAccount()
    }

    private fun observeEvents() =
        viewModelScope.launch {
            events.collect {
                when (it) {
                    is UiEvent.PostLikeAction -> likePost(it)
                    is UiEvent.RepostAction -> repostPost(it)
                    is UiEvent.ReplyToAction -> publishReply(it)
                    is UiEvent.UpdateReply -> updateReply(it)
                    is UiEvent.ZapAction -> zapPost(it)
                    is UiEvent.MuteAction -> mute(it)
                    UiEvent.UpdateConversation -> fetchRepliesFromNetwork()
                }
            }
        }

    private fun observeConversation() =
        viewModelScope.launch {
            loadHighlightedPost()
            delayShortlyToPropagateHighlightedPost()
            subscribeToConversationChanges()
        }

    private fun observeActiveAccount() =
        viewModelScope.launch {
            activeAccountStore.activeAccountState
                .filterIsInstance<ActiveUserAccountState.ActiveUserAccount>()
                .collect {
                    setState {
                        copy(
                            zappingState = this.zappingState.copy(
                                walletConnected = it.data.hasWallet(),
                                walletPreference = it.data.walletPreference,
                                zapDefault = it.data.appSettings?.zapDefault ?: this.zappingState.zapDefault,
                                zapsConfig = it.data.appSettings?.zapsConfig ?: this.zappingState.zapsConfig,
                                walletBalanceInBtc = it.data.primalWalletBalanceInBtc,
                            ),
                        )
                    }
                }
        }

    private suspend fun loadHighlightedPost() {
        val rootPost = withContext(dispatcherProvider.io()) { feedRepository.findPostById(postId = postId) }
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

    private fun fetchRepliesFromNetwork() =
        viewModelScope.launch {
            try {
                withContext(dispatcherProvider.io()) {
                    feedRepository.fetchReplies(postId = postId)
                }
            } catch (error: WssException) {
                Timber.e(error)
            }
        }

    private fun likePost(postLikeAction: UiEvent.PostLikeAction) =
        viewModelScope.launch {
            try {
                postRepository.likePost(
                    postId = postLikeAction.postId,
                    postAuthorId = postLikeAction.postAuthorId,
                )
            } catch (error: NostrPublishException) {
                setErrorState(error = ThreadError.FailedToPublishLikeEvent(error))
            } catch (error: MissingRelaysException) {
                setErrorState(error = ThreadError.MissingRelaysConfiguration(error))
            }
        }

    private fun repostPost(repostAction: UiEvent.RepostAction) =
        viewModelScope.launch {
            try {
                postRepository.repostPost(
                    postId = repostAction.postId,
                    postAuthorId = repostAction.postAuthorId,
                    postRawNostrEvent = repostAction.postNostrEvent,
                )
            } catch (error: NostrPublishException) {
                setErrorState(error = ThreadError.FailedToPublishRepostEvent(error))
            } catch (error: MissingRelaysException) {
                setErrorState(error = ThreadError.MissingRelaysConfiguration(error))
            }
        }

    private fun zapPost(zapAction: UiEvent.ZapAction) =
        viewModelScope.launch {
            val postAuthorProfileData = withContext(dispatcherProvider.io()) {
                profileRepository.findProfileDataOrNull(profileId = zapAction.postAuthorId)
            }

            if (postAuthorProfileData?.lnUrlDecoded == null) {
                setErrorState(error = ThreadError.MissingLightningAddress(IllegalStateException()))
                return@launch
            }

            try {
                zapHandler.zap(
                    userId = activeAccountStore.activeUserId(),
                    comment = zapAction.zapDescription,
                    amountInSats = zapAction.zapAmount,
                    target = ZapTarget.Note(
                        zapAction.postId,
                        zapAction.postAuthorId,
                        postAuthorProfileData.lnUrlDecoded,
                    ),
                )
            } catch (error: ZapFailureException) {
                setErrorState(error = ThreadError.FailedToPublishZapEvent(error))
            } catch (error: MissingRelaysException) {
                setErrorState(error = ThreadError.MissingRelaysConfiguration(error))
            } catch (error: InvalidZapRequestException) {
                setErrorState(error = ThreadError.InvalidZapRequest(error))
            }
        }

    private fun updateReply(updateReplyEvent: UiEvent.UpdateReply) {
        setState { copy(replyText = updateReplyEvent.newReply) }
    }

    private fun publishReply(replyToAction: UiEvent.ReplyToAction) =
        viewModelScope.launch {
            setState { copy(publishingReply = true) }
            try {
                val publishedAndImported = postRepository.publishShortTextNote(
                    content = state.value.replyText,
                    attachments = emptyList(),
                    rootPostId = replyToAction.rootPostId,
                    replyToPostId = replyToAction.replyToPostId,
                    replyToAuthorId = replyToAction.replyToAuthorId,
                )

                if (publishedAndImported) {
                    fetchRepliesFromNetwork()
                } else {
                    scheduleFetchReplies()
                }

                setState { copy(replyText = "") }
            } catch (error: NostrPublishException) {
                setErrorState(error = ThreadError.FailedToPublishReplyEvent(error))
            } catch (error: MissingRelaysException) {
                setErrorState(error = ThreadError.MissingRelaysConfiguration(error))
            } finally {
                setState { copy(publishingReply = false) }
            }
        }

    private fun mute(action: UiEvent.MuteAction) =
        viewModelScope.launch {
            try {
                mutedUserRepository.muteUserAndPersistMuteList(
                    userId = activeAccountStore.activeUserId(),
                    mutedUserId = action.postAuthorId,
                )
            } catch (error: WssException) {
                setErrorState(error = ThreadError.FailedToMuteUser(error))
            } catch (error: NostrPublishException) {
                setErrorState(error = ThreadError.FailedToMuteUser(error))
            }
        }

    private fun scheduleFetchReplies() =
        viewModelScope.launch {
            delay(750.milliseconds)
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
