package net.primal.android.thread.notes

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
import net.primal.android.navigation.noteIdOrThrow
import net.primal.android.networking.relays.errors.MissingRelaysException
import net.primal.android.networking.relays.errors.NostrPublishException
import net.primal.android.networking.sockets.errors.WssException
import net.primal.android.note.repository.NoteRepository
import net.primal.android.note.ui.asNoteZapUiModel
import net.primal.android.profile.repository.ProfileRepository
import net.primal.android.settings.muted.repository.MutedUserRepository
import net.primal.android.thread.notes.ThreadContract.UiEvent
import net.primal.android.thread.notes.ThreadContract.UiState
import net.primal.android.thread.notes.ThreadContract.UiState.ThreadError
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
    private val noteRepository: NoteRepository,
    private val profileRepository: ProfileRepository,
    private val zapHandler: ZapHandler,
    private val mutedUserRepository: MutedUserRepository,
) : ViewModel() {

    private val highlightPostId = savedStateHandle.noteIdOrThrow

    private val _state = MutableStateFlow(UiState(highlightPostId = highlightPostId))
    val state = _state.asStateFlow()
    private fun setState(reducer: UiState.() -> UiState) = _state.getAndUpdate { it.reducer() }

    private val events: MutableSharedFlow<UiEvent> = MutableSharedFlow()
    fun setEvent(event: UiEvent) = viewModelScope.launch { events.emit(event) }

    init {
        observeEvents()
        observeConversation()
        observeTopZappers()
        observeActiveAccount()
    }

    private fun observeEvents() =
        viewModelScope.launch {
            events.collect {
                when (it) {
                    is UiEvent.PostLikeAction -> likePost(it)
                    is UiEvent.RepostAction -> repostPost(it)
                    is UiEvent.ZapAction -> zapPost(it)
                    is UiEvent.MuteAction -> mute(it)
                    UiEvent.UpdateConversation -> fetchData()
                    is UiEvent.ReportAbuse -> reportAbuse(it)
                    is UiEvent.BookmarkAction -> handleBookmark(it)
                    UiEvent.DismissBookmarkConfirmation -> setState { copy(confirmBookmarkingNoteId = null) }
                }
            }
        }

    private fun observeConversation() =
        viewModelScope.launch {
            loadHighlightedPost()
            delayShortlyToPropagateHighlightedPost()
            subscribeToConversationChanges()
        }

    private fun observeTopZappers() =
        viewModelScope.launch {
            noteRepository.observeTopZappers(postId = highlightPostId).collect {
                setState {
                    copy(
                        topZap = it.firstOrNull()?.asNoteZapUiModel() ?: this.topZap,
                        otherZaps = it.drop(n = 1).map { it.asNoteZapUiModel() },
                    )
                }
            }
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
                                walletBalanceInBtc = it.data.primalWalletState.balanceInBtc,
                            ),
                        )
                    }
                }
        }

    private suspend fun loadHighlightedPost() {
        val rootPost = withContext(dispatcherProvider.io()) { feedRepository.findPostById(postId = highlightPostId) }
        if (rootPost != null) {
            setState {
                copy(
                    conversation = listOf(rootPost.asFeedPostUi()),
                    highlightPostId = this@ThreadViewModel.highlightPostId,
                    highlightPostIndex = 0,
                )
            }
        }
    }

    private suspend fun delayShortlyToPropagateHighlightedPost() = delay(100.milliseconds)

    private suspend fun subscribeToConversationChanges() {
        feedRepository.observeConversation(postId = highlightPostId)
            .filter { it.isNotEmpty() }
            .map { posts -> posts.map { it.asFeedPostUi() } }
            .collect { conversation ->
                val highlightPostIndex = conversation.indexOfFirst { it.postId == highlightPostId }
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

    private fun fetchData() {
        fetchNoteReplies()
        fetchTopNoteZaps()
    }

    private fun fetchNoteReplies() =
        viewModelScope.launch {
            setState { copy(fetching = true) }
            try {
                withContext(dispatcherProvider.io()) {
                    feedRepository.fetchReplies(postId = highlightPostId)
                }
            } catch (error: WssException) {
                Timber.w(error)
            } finally {
                setState { copy(fetching = false) }
            }
        }

    private fun fetchTopNoteZaps() =
        viewModelScope.launch {
            try {
                withContext(dispatcherProvider.io()) {
                    noteRepository.fetchTopNoteZaps(noteId = highlightPostId)
                }
            } catch (error: WssException) {
                Timber.w(error)
            }
        }

    private fun likePost(postLikeAction: UiEvent.PostLikeAction) =
        viewModelScope.launch {
            try {
                noteRepository.likePost(
                    postId = postLikeAction.postId,
                    postAuthorId = postLikeAction.postAuthorId,
                )
            } catch (error: NostrPublishException) {
                Timber.w(error)
                setErrorState(error = ThreadError.FailedToPublishLikeEvent(error))
            } catch (error: MissingRelaysException) {
                Timber.w(error)
                setErrorState(error = ThreadError.MissingRelaysConfiguration(error))
            }
        }

    private fun repostPost(repostAction: UiEvent.RepostAction) =
        viewModelScope.launch {
            try {
                noteRepository.repostPost(
                    postId = repostAction.postId,
                    postAuthorId = repostAction.postAuthorId,
                    postRawNostrEvent = repostAction.postNostrEvent,
                )
            } catch (error: NostrPublishException) {
                Timber.w(error)
                setErrorState(error = ThreadError.FailedToPublishRepostEvent(error))
            } catch (error: MissingRelaysException) {
                Timber.w(error)
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
                withContext(dispatcherProvider.io()) {
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
                }
            } catch (error: ZapFailureException) {
                Timber.w(error)
                setErrorState(error = ThreadError.FailedToPublishZapEvent(error))
            } catch (error: MissingRelaysException) {
                Timber.w(error)
                setErrorState(error = ThreadError.MissingRelaysConfiguration(error))
            } catch (error: InvalidZapRequestException) {
                Timber.w(error)
                setErrorState(error = ThreadError.InvalidZapRequest(error))
            }
        }

    private fun mute(action: UiEvent.MuteAction) =
        viewModelScope.launch {
            try {
                withContext(dispatcherProvider.io()) {
                    mutedUserRepository.muteUserAndPersistMuteList(
                        userId = activeAccountStore.activeUserId(),
                        mutedUserId = action.postAuthorId,
                    )
                }
            } catch (error: WssException) {
                setErrorState(error = ThreadError.FailedToMuteUser(error))
            } catch (error: NostrPublishException) {
                setErrorState(error = ThreadError.FailedToMuteUser(error))
            }
        }

    private fun reportAbuse(event: UiEvent.ReportAbuse) =
        viewModelScope.launch {
            try {
                withContext(dispatcherProvider.io()) {
                    profileRepository.reportAbuse(
                        userId = activeAccountStore.activeUserId(),
                        reportType = event.reportType,
                        profileId = event.profileId,
                        noteId = event.noteId,
                    )
                }
            } catch (error: NostrPublishException) {
                Timber.w(error)
            }
        }

    private fun handleBookmark(event: UiEvent.BookmarkAction) =
        viewModelScope.launch {
            val userId = activeAccountStore.activeUserId()
            withContext(dispatcherProvider.io()) {
                try {
                    setState { copy(confirmBookmarkingNoteId = null) }
                    val isBookmarked = noteRepository.isBookmarked(noteId = event.noteId)
                    when (isBookmarked) {
                        true -> noteRepository.removeFromBookmarks(
                            userId = userId,
                            forceUpdate = event.forceUpdate,
                            noteId = event.noteId,
                        )

                        false -> noteRepository.addToBookmarks(
                            userId = userId,
                            forceUpdate = event.forceUpdate,
                            noteId = event.noteId,
                        )
                    }
                } catch (error: NostrPublishException) {
                    Timber.w(error)
                } catch (error: ProfileRepository.BookmarksListNotFound) {
                    Timber.w(error)
                    setState { copy(confirmBookmarkingNoteId = event.noteId) }
                }
            }
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
