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
import net.primal.android.bookmarks.BookmarksRepository
import net.primal.android.bookmarks.domain.BookmarkType
import net.primal.android.core.errors.UiError
import net.primal.android.events.repository.EventRepository
import net.primal.android.networking.relays.errors.MissingRelaysException
import net.primal.android.networking.relays.errors.NostrPublishException
import net.primal.android.networking.sockets.errors.WssException
import net.primal.android.nostr.model.NostrEventKind
import net.primal.android.nostr.notary.NostrReadOnlyMode
import net.primal.android.nostr.repository.RelayHintsRepository
import net.primal.android.notes.feed.note.NoteContract.UiEvent
import net.primal.android.notes.feed.note.NoteContract.UiState
import net.primal.android.profile.repository.ProfileRepository
import net.primal.android.settings.muted.repository.MutedUserRepository
import net.primal.android.user.accounts.active.ActiveAccountStore
import net.primal.android.wallet.domain.ZapTarget
import net.primal.android.wallet.zaps.InvalidZapRequestException
import net.primal.android.wallet.zaps.ZapFailureException
import net.primal.android.wallet.zaps.ZapHandler
import net.primal.android.wallet.zaps.hasWallet
import timber.log.Timber

@HiltViewModel(assistedFactory = NoteViewModel.Factory::class)
class NoteViewModel @AssistedInject constructor(
    @Assisted private val noteId: String?,
    private val activeAccountStore: ActiveAccountStore,
    private val zapHandler: ZapHandler,
    private val eventRepository: EventRepository,
    private val profileRepository: ProfileRepository,
    private val mutedUserRepository: MutedUserRepository,
    private val bookmarksRepository: BookmarksRepository,
    private val relayHintsRepository: RelayHintsRepository,
) : ViewModel() {

    @AssistedFactory
    interface Factory {
        fun create(noteId: String? = null): NoteViewModel
    }

    private val _state = MutableStateFlow(UiState())
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
                    is UiEvent.MuteAction -> mute(it)
                    is UiEvent.BookmarkAction -> handleBookmark(it)
                    is UiEvent.DismissBookmarkConfirmation -> dismissBookmarkConfirmation()
                    UiEvent.DismissError -> setState { copy(error = null) }
                }
            }
        }

    private fun likePost(postLikeAction: UiEvent.PostLikeAction) =
        viewModelScope.launch {
            try {
                eventRepository.likeEvent(
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
            } catch (error: NostrReadOnlyMode) {
                /* TODO(marko): what to do here? */
                Timber.w(error)
            }
        }

    private fun repostPost(repostAction: UiEvent.RepostAction) =
        viewModelScope.launch {
            try {
                eventRepository.repostEvent(
                    userId = activeAccountStore.activeUserId(),
                    eventId = repostAction.postId,
                    eventAuthorId = repostAction.postAuthorId,
                    eventKind = NostrEventKind.ShortTextNote,
                    eventRawNostrEvent = repostAction.postNostrEvent,
                )
            } catch (error: NostrPublishException) {
                Timber.w(error)
                setState { copy(error = UiError.FailedToPublishRepostEvent(error)) }
            } catch (error: MissingRelaysException) {
                Timber.w(error)
                setState { copy(error = UiError.MissingRelaysConfiguration(error)) }
            } catch (error: NostrReadOnlyMode) {
                /* TODO(marko): what to do here? */
                Timber.w(error)
            }
        }

    private fun zapPost(zapAction: UiEvent.ZapAction) =
        viewModelScope.launch {
            val postAuthorProfileData = profileRepository.findProfileDataOrNull(profileId = zapAction.postAuthorId)
            if (postAuthorProfileData?.lnUrlDecoded == null) {
                setState { copy(error = UiError.MissingLightningAddress(IllegalStateException("Missing ln url"))) }
                return@launch
            }

            try {
                zapHandler.zap(
                    userId = activeAccountStore.activeUserId(),
                    comment = zapAction.zapDescription,
                    amountInSats = zapAction.zapAmount,
                    target = ZapTarget.Event(
                        zapAction.postId,
                        zapAction.postAuthorId,
                        postAuthorProfileData.lnUrlDecoded,
                    ),
                )
            } catch (error: ZapFailureException) {
                Timber.w(error)
                setState { copy(error = UiError.FailedToPublishZapEvent(error)) }
            } catch (error: MissingRelaysException) {
                Timber.w(error)
                setState { copy(error = UiError.MissingRelaysConfiguration(error)) }
            } catch (error: InvalidZapRequestException) {
                Timber.w(error)
                setState { copy(error = UiError.InvalidZapRequest(error)) }
            }
        }

    private fun mute(action: UiEvent.MuteAction) =
        viewModelScope.launch {
            try {
                mutedUserRepository.muteUserAndPersistMuteList(
                    userId = activeAccountStore.activeUserId(),
                    mutedUserId = action.userId,
                )
            } catch (error: WssException) {
                Timber.w(error)
                setState { copy(error = UiError.FailedToMuteUser(error)) }
            } catch (error: NostrReadOnlyMode) {
                Timber.w(error)
            } catch (error: NostrPublishException) {
                Timber.w(error)
                setState { copy(error = UiError.FailedToMuteUser(error)) }
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
            } catch (error: NostrReadOnlyMode) {
                Timber.w(error)
            } catch (error: NostrPublishException) {
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
            } catch (error: BookmarksRepository.BookmarksListNotFound) {
                Timber.w(error)
                setState { copy(shouldApproveBookmark = true) }
            } catch (error: NostrReadOnlyMode) {
                Timber.w(error)
                /* TODO(marko): what to do here? */
            }
        }

    private fun dismissBookmarkConfirmation() =
        viewModelScope.launch {
            setState { copy(shouldApproveBookmark = false) }
        }
}
