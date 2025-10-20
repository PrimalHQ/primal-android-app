package net.primal.android.notes.feed.note

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.getAndUpdate
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import net.primal.android.core.errors.UiError
import net.primal.android.core.errors.UiError.FailedToPublishZapEvent
import net.primal.android.core.errors.UiError.GenericError
import net.primal.android.core.errors.UiError.InvalidZapRequest
import net.primal.android.networking.relays.errors.NostrPublishException
import net.primal.android.notes.feed.note.NoteContract.SideEffect
import net.primal.android.notes.feed.note.NoteContract.UiEvent
import net.primal.android.notes.feed.note.NoteContract.UiState
import net.primal.android.user.accounts.active.ActiveAccountStore
import net.primal.android.user.repository.UserRepository
import net.primal.android.wallet.zaps.ZapHandler
import net.primal.core.utils.CurrencyConversionUtils.formatAsString
import net.primal.domain.account.WalletAccountRepository
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
import net.primal.domain.nostr.zaps.ZapError
import net.primal.domain.nostr.zaps.ZapResult
import net.primal.domain.nostr.zaps.ZapTarget
import net.primal.domain.posts.FeedRepository
import net.primal.domain.profile.ProfileRepository
import net.primal.domain.utils.isConfigured
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
    private val userRepository: UserRepository,
    private val walletAccountRepository: WalletAccountRepository,
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

    private val _effects = Channel<SideEffect>()
    val effects = _effects.receiveAsFlow()
    private fun setEffect(effect: SideEffect) = viewModelScope.launch { _effects.send(effect) }

    init {
        observeEvents()
        observeActiveWallet()
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

    private fun observeActiveWallet() =
        viewModelScope.launch {
            walletAccountRepository.observeActiveWallet(userId = activeAccountStore.activeUserId())
                .collect { wallet ->
                    setState {
                        copy(
                            zappingState = zappingState.copy(
                                walletConnected = wallet.isConfigured(),
                                walletBalanceInBtc = wallet?.balanceInBtc?.formatAsString(),
                            ),
                        )
                    }
                }
        }

    private fun subscribeToActiveAccount() =
        viewModelScope.launch {
            activeAccountStore.activeUserAccount.collect {
                setState {
                    copy(
                        activeAccountUserId = activeAccountStore.activeUserId(),
                        zappingState = this.zappingState.copy(
                            zapDefault = it.appSettings?.zapDefault ?: this.zappingState.zapDefault,
                            zapsConfig = it.appSettings?.zapsConfig ?: this.zappingState.zapsConfig,
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
                    is UiEvent.UpdateAutoPlayVideoSoundPreference -> updateVideoSoundSettings(it.soundOn)
                    is UiEvent.RequestDeleteAction -> requestDelete(noteId = it.noteId, userId = it.userId)
                    is UiEvent.DeleteRepostAction -> requestDeleteRepost(
                        postId = it.postId,
                        repostId = it.repostId,
                        repostAuthorId = it.repostAuthorId,
                    )
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

            val walletId = walletAccountRepository.getActiveWallet(userId = activeAccountStore.activeUserId())?.walletId
                ?: return@launch

            val result = zapHandler.zap(
                userId = activeAccountStore.activeUserId(),
                walletId = walletId,
                comment = zapAction.zapDescription,
                amountInSats = zapAction.zapAmount,
                target = ZapTarget.Event(
                    eventId = zapAction.postId,
                    recipientUserId = zapAction.postAuthorId,
                    recipientLnUrlDecoded = lnUrlDecoded,
                ),
            )

            if (result is ZapResult.Failure) {
                when (result.error) {
                    is ZapError.InvalidZap, is ZapError.FailedToFetchZapPayRequest,
                    is ZapError.FailedToFetchZapInvoice,
                    -> setState { copy(error = InvalidZapRequest()) }

                    ZapError.FailedToPublishEvent, ZapError.FailedToSignEvent,
                    is ZapError.Timeout,
                    -> {
                        setState { copy(error = FailedToPublishZapEvent()) }
                    }

                    is ZapError.Unknown -> {
                        setState { copy(error = GenericError()) }
                    }
                }
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

    private fun updateVideoSoundSettings(soundOn: Boolean) =
        viewModelScope.launch {
            userRepository.updateContentDisplaySettings(userId = activeAccountStore.activeUserId()) {
                copy(autoPlayVideoSoundOn = soundOn)
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

                feedRepository.deletePostById(postId = noteId, userId = activeAccountStore.activeUserId())
                setEffect(SideEffect.NoteDeleted)
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

    private fun requestDeleteRepost(
        postId: String,
        repostId: String?,
        repostAuthorId: String?,
    ) = viewModelScope.launch {
        val (resolvedRepostId, resolvedRepostAuthorId) = if (repostId == null || repostAuthorId == null) {
            feedRepository.findRepostByPostId(postId = postId, userId = activeAccountStore.activeUserId())
                .getOrNull()
                ?.let {
                    it.repostId to (it.repostAuthorId ?: activeAccountStore.activeUserId())
                } ?: return@launch
        } else {
            repostId to repostAuthorId
        }

        try {
            eventInteractionRepository.deleteEvent(
                userId = resolvedRepostAuthorId,
                eventIdentifier = resolvedRepostId,
                eventKind = NostrEventKind.ShortTextNoteRepost,
                relayHint = state.value.relayHints.firstOrNull(),
            )

            feedRepository.deleteRepostById(
                postId = postId,
                repostId = resolvedRepostId,
                userId = resolvedRepostAuthorId,
            )
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
                setState { copy(error = UiError.FailedToBookmarkNote(error)) }
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
            } catch (error: NetworkException) {
                setState { copy(error = UiError.FailedToBookmarkNote(error)) }
                Timber.w(error)
            }
        }

    private fun dismissBookmarkConfirmation() =
        viewModelScope.launch {
            setState { copy(shouldApproveBookmark = false) }
        }
}
