package net.primal.android.notes.feed.note

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.getAndUpdate
import kotlinx.coroutines.launch
import net.primal.android.core.errors.UiError
import net.primal.android.networking.relays.errors.NostrPublishException
import net.primal.android.notes.feed.note.NoteContract.UiEvent
import net.primal.android.notes.feed.note.NoteContract.UiState
import net.primal.android.user.accounts.active.ActiveAccountStore
import net.primal.android.wallet.zaps.ZapHandler
import net.primal.android.wallet.zaps.hasWallet
import net.primal.domain.bookmarks.BookmarkType
import net.primal.domain.bookmarks.PublicBookmarksRepository
import net.primal.domain.common.exception.NetworkException
import net.primal.domain.events.EventInteractionRepository
import net.primal.domain.events.EventRelayHintsRepository
import net.primal.domain.mutes.MutedItemRepository
import net.primal.domain.nostr.NostrEventKind
import net.primal.domain.nostr.PublicBookmarksNotFoundException
import net.primal.domain.nostr.cryptography.SigningKeyNotFoundException
import net.primal.domain.nostr.cryptography.SigningRejectedException
import net.primal.domain.nostr.publisher.MissingRelaysException
import net.primal.domain.nostr.zaps.ZapFailureException
import net.primal.domain.nostr.zaps.ZapRequestException
import net.primal.domain.nostr.zaps.ZapTarget
import net.primal.domain.posts.FeedRepository
import net.primal.domain.profile.ProfileRepository
import timber.log.Timber

@HiltViewModel(assistedFactory = NoteViewModel.Factory::class)
class NoteViewModel @AssistedInject constructor(
    @Assisted private val noteId: String?,
    private val activeAccountStore: ActiveAccountStore,
    private val zapHandler: ZapHandler,
    private val eventInteractionRepository: EventInteractionRepository,
    private val profileRepository: ProfileRepository,
    private val feedRepository: FeedRepository,
    private val mutedItemRepository: MutedItemRepository,
    private val bookmarksRepository: PublicBookmarksRepository,
    private val relayHintsRepository: EventRelayHintsRepository,
) : ViewModel() {

    @AssistedFactory
    interface Factory {
        fun create(noteId: String? = null): NoteViewModel
    }

    private val _state = MutableStateFlow(UiState(activeAccountUserId = activeAccountStore.activeUserId()))
    val state = _state.asStateFlow()
    private fun setState(reducer: UiState.() -> UiState) = _state.getAndUpdate { it.reducer() }

    private val events: MutableSharedFlow<UiEvent> = MutableSharedFlow()
    fun setEvent(event: UiEvent) = viewModelScope.launch { events.emit(event) }

    init {
        observeEvents()
        subscribeToActiveAccount()
        if (noteId != null) {
            prepareRelayHints(noteId = noteId)
        }
    }

    private fun prepareRelayHints(noteId: String) =
        viewModelScope.launch {
            val hints = relayHintsRepository.findRelaysByIds(eventIds = listOf(noteId))
            hints.firstOrNull()?.let { relayHints ->
                setState { copy(relayHints = relayHints.relays.take(n = 2)) }
            }
        }

    private fun subscribeToActiveAccount() =
        viewModelScope.launch {
            activeAccountStore.activeUserAccount.collect {
                setState {
                    copy(
                        zappingState = this.zappingState.copy(
                            walletConnected = it.hasWallet(),
                            walletPreference = it.walletPreference,
                            zapDefault = it.appSettings?.zapDefault ?: this.zappingState.zapDefault,
                            zapsConfig = it.appSettings?.zapsConfig ?: this.zappingState.zapsConfig,
                            walletBalanceInBtc = it.primalWalletState.balanceInBtc,
                        ),
                    )
                }
            }
        }

    private fun observeEvents() =
        viewModelScope.launch {
            events.collect {
                when (it) {
                    is UiEvent.PostLikeAction -> likePost(it)
                    is UiEvent.ReportAbuse -> reportAbuse(it)
                    is UiEvent.RepostAction -> repostPost(it)
                    is UiEvent.ZapAction -> zapPost(it)
                    is UiEvent.MuteUserAction -> muteUser(it)
                    is UiEvent.MuteThreadAction -> toggleMuteThread(postId = it.postId, isThreadMuted = false)
                    is UiEvent.UnmuteThreadAction -> toggleMuteThread(postId = it.postId, isThreadMuted = true)
                    is UiEvent.BookmarkAction -> handleBookmark(it)
                    is UiEvent.DismissBookmarkConfirmation -> dismissBookmarkConfirmation()
                    UiEvent.DismissError -> setState { copy(error = null) }
                    is UiEvent.RequestDeleteAction -> requestDelete(noteId = it.noteId, userId = it.userId)
                }
            }
        }

    private fun likePost(postLikeAction: UiEvent.PostLikeAction) =
        viewModelScope.launch {
            try {
                eventInteractionRepository.likeEvent(
                    userId = activeAccountStore.activeUserId(),
                    eventId = postLikeAction.postId,
                    eventAuthorId = postLikeAction.postAuthorId,
                )
            } catch (error: NostrPublishException) {
                Timber.w(error)
                setState { copy(error = UiError.FailedToPublishLikeEvent(error)) }
            } catch (error: MissingRelaysException) {
                Timber.w(error)
                setState { copy(error = UiError.MissingRelaysConfiguration(error)) }
            } catch (error: SigningKeyNotFoundException) {
                setState { copy(error = UiError.MissingPrivateKey) }
                Timber.w(error)
            } catch (error: SigningRejectedException) {
                setState { copy(error = UiError.NostrSignUnauthorized) }
                Timber.w(error)
            }
        }

    private fun repostPost(repostAction: UiEvent.RepostAction) =
        viewModelScope.launch {
            try {
                eventInteractionRepository.repostEvent(
                    userId = activeAccountStore.activeUserId(),
                    eventId = repostAction.postId,
                    eventAuthorId = repostAction.postAuthorId,
                    eventKind = NostrEventKind.ShortTextNote.value,
                    eventRawNostrEvent = repostAction.postNostrEvent,
                )
            } catch (error: NostrPublishException) {
                Timber.w(error)
                setState { copy(error = UiError.FailedToPublishRepostEvent(error)) }
            } catch (error: MissingRelaysException) {
                Timber.w(error)
                setState { copy(error = UiError.MissingRelaysConfiguration(error)) }
            } catch (error: SigningKeyNotFoundException) {
                setState { copy(error = UiError.MissingPrivateKey) }
                Timber.w(error)
            } catch (error: SigningRejectedException) {
                setState { copy(error = UiError.NostrSignUnauthorized) }
                Timber.w(error)
            }
        }

    private fun zapPost(zapAction: UiEvent.ZapAction) =
        viewModelScope.launch {
            val postAuthorProfileData = profileRepository.findProfileDataOrNull(profileId = zapAction.postAuthorId)
            val lnUrlDecoded = postAuthorProfileData?.lnUrlDecoded
            if (lnUrlDecoded == null) {
                setState { copy(error = UiError.MissingLightningAddress(IllegalStateException("Missing ln url"))) }
                return@launch
            }

            try {
                zapHandler.zap(
                    userId = activeAccountStore.activeUserId(),
                    comment = zapAction.zapDescription,
                    amountInSats = zapAction.zapAmount,
                    target = ZapTarget.Event(
                        eventId = zapAction.postId,
                        eventAuthorId = zapAction.postAuthorId,
                        eventAuthorLnUrlDecoded = lnUrlDecoded,
                    ),
                )
            } catch (error: ZapFailureException) {
                Timber.w(error)
                setState { copy(error = UiError.FailedToPublishZapEvent(error)) }
            } catch (error: MissingRelaysException) {
                Timber.w(error)
                setState { copy(error = UiError.MissingRelaysConfiguration(error)) }
            } catch (error: ZapRequestException) {
                Timber.w(error)
                setState { copy(error = UiError.InvalidZapRequest(error)) }
            }
        }

    private fun muteUser(action: UiEvent.MuteUserAction) =
        viewModelScope.launch {
            try {
                mutedItemRepository.muteUserAndPersistMuteList(
                    userId = activeAccountStore.activeUserId(),
                    mutedUserId = action.userId,
                )
            } catch (error: NetworkException) {
                Timber.w(error)
                setState { copy(error = UiError.FailedToMuteUser(error)) }
            } catch (error: SigningKeyNotFoundException) {
                Timber.w(error)
                setState { copy(error = UiError.MissingPrivateKey) }
            } catch (error: SigningRejectedException) {
                Timber.w(error)
                setState { copy(error = UiError.NostrSignUnauthorized) }
            } catch (error: NostrPublishException) {
                Timber.w(error)
                setState { copy(error = UiError.FailedToMuteUser(error)) }
            } catch (error: MissingRelaysException) {
                Timber.w(error)
                setState { copy(error = UiError.MissingRelaysConfiguration(error)) }
            }
        }

    private fun toggleMuteThread(postId: String, isThreadMuted: Boolean) =
        viewModelScope.launch {
            try {
                if (isThreadMuted) {
                    mutedItemRepository.unmuteThreadAndPersistMuteList(
                        postId = postId,
                        userId = activeAccountStore.activeUserId(),
                    )
                } else {
                    mutedItemRepository.muteThreadAndPersistMuteList(
                        postId = postId,
                        userId = activeAccountStore.activeUserId(),
                    )
                }
            } catch (error: NetworkException) {
                Timber.w(error)
                if (isThreadMuted) {
                    setState { copy(error = UiError.FailedToUnmuteThread(error)) }
                } else {
                    setState { copy(error = UiError.FailedToMuteThread(error)) }
                }
            } catch (error: SigningKeyNotFoundException) {
                Timber.w(error)
                setState { copy(error = UiError.MissingPrivateKey) }
            } catch (error: SigningRejectedException) {
                Timber.w(error)
                setState { copy(error = UiError.NostrSignUnauthorized) }
            } catch (error: NostrPublishException) {
                Timber.w(error)
                if (isThreadMuted) {
                    setState { copy(error = UiError.FailedToUnmuteThread(error)) }
                } else {
                    setState { copy(error = UiError.FailedToMuteThread(error)) }
                }
            } catch (error: MissingRelaysException) {
                Timber.w(error)
                setState { copy(error = UiError.MissingRelaysConfiguration(error)) }
            }
        }

    private fun reportAbuse(event: UiEvent.ReportAbuse) =
        viewModelScope.launch {
            try {
                profileRepository.reportAbuse(
                    userId = activeAccountStore.activeUserId(),
                    reportType = event.reportType,
                    profileId = event.profileId,
                    eventId = event.noteId,
                )
            } catch (error: SigningKeyNotFoundException) {
                Timber.w(error)
                setState { copy(error = UiError.MissingPrivateKey) }
            } catch (error: SigningRejectedException) {
                Timber.w(error)
                setState { copy(error = UiError.NostrSignUnauthorized) }
            } catch (error: NostrPublishException) {
                Timber.w(error)
            }
        }

    private fun requestDelete(noteId: String, userId: String) =
        viewModelScope.launch {
            if (userId != activeAccountStore.activeUserId()) return@launch

            try {
                eventInteractionRepository.deleteEvent(
                    userId = userId,
                    eventIdentifier = noteId,
                    eventKind = NostrEventKind.ShortTextNote,
                    relayHint = state.value.relayHints.firstOrNull(),
                )

                feedRepository.deletePostById(postId = noteId)
            } catch (error: NostrPublishException) {
                Timber.w(error)
                setState { copy(error = UiError.FailedToPublishDeleteEvent(error)) }
            } catch (error: MissingRelaysException) {
                Timber.w(error)
                setState { copy(error = UiError.MissingRelaysConfiguration(error)) }
            } catch (error: SigningKeyNotFoundException) {
                setState { copy(error = UiError.MissingPrivateKey) }
                Timber.w(error)
            } catch (error: SigningRejectedException) {
                setState { copy(error = UiError.NostrSignUnauthorized) }
                Timber.w(error)
            }
        }

    private fun handleBookmark(event: UiEvent.BookmarkAction) =
        viewModelScope.launch {
            val userId = activeAccountStore.activeUserId()
            try {
                setState { copy(shouldApproveBookmark = false) }
                val isBookmarked = bookmarksRepository.isBookmarked(tagValue = event.noteId)
                when (isBookmarked) {
                    true -> bookmarksRepository.removeFromBookmarks(
                        userId = userId,
                        forceUpdate = event.forceUpdate,
                        bookmarkType = BookmarkType.Note,
                        tagValue = event.noteId,
                    )

                    false -> bookmarksRepository.addToBookmarks(
                        userId = userId,
                        forceUpdate = event.forceUpdate,
                        bookmarkType = BookmarkType.Note,
                        tagValue = event.noteId,
                    )
                }
            } catch (error: NostrPublishException) {
                Timber.w(error)
            } catch (error: PublicBookmarksNotFoundException) {
                Timber.w(error)
                setState { copy(shouldApproveBookmark = true) }
            } catch (error: SigningKeyNotFoundException) {
                setState { copy(error = UiError.MissingPrivateKey) }
                Timber.w(error)
            } catch (error: SigningRejectedException) {
                setState { copy(error = UiError.NostrSignUnauthorized) }
                Timber.w(error)
            }
        }

    private fun dismissBookmarkConfirmation() =
        viewModelScope.launch {
            setState { copy(shouldApproveBookmark = false) }
        }
}
