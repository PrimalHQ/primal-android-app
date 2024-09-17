package net.primal.android.notes.feed.note

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.getAndUpdate
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import net.primal.android.core.coroutines.CoroutineDispatcherProvider
import net.primal.android.networking.relays.errors.MissingRelaysException
import net.primal.android.networking.relays.errors.NostrPublishException
import net.primal.android.networking.sockets.errors.WssException
import net.primal.android.note.repository.NoteRepository
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

@HiltViewModel
class NoteViewModel @Inject constructor(
    private val dispatcherProvider: CoroutineDispatcherProvider,
    private val activeAccountStore: ActiveAccountStore,
    private val noteRepository: NoteRepository,
    private val profileRepository: ProfileRepository,
    private val zapHandler: ZapHandler,
    private val mutedUserRepository: MutedUserRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(UiState())
    val state = _state.asStateFlow()
    private fun setState(reducer: UiState.() -> UiState) = _state.getAndUpdate { it.reducer() }

    private val events: MutableSharedFlow<UiEvent> = MutableSharedFlow()
    fun setEvent(event: UiEvent) = viewModelScope.launch { events.emit(event) }

    init {
        observeEvents()
        subscribeToActiveAccount()
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
                }
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
//                setErrorState(error = FeedError.FailedToPublishLikeEvent(error))
            } catch (error: MissingRelaysException) {
                Timber.w(error)
//                setErrorState(error = FeedError.MissingRelaysConfiguration(error))
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
//                setErrorState(error = FeedError.FailedToPublishRepostEvent(error))
            } catch (error: MissingRelaysException) {
                Timber.w(error)
//                setErrorState(error = FeedError.MissingRelaysConfiguration(error))
            }
        }

    private fun zapPost(zapAction: UiEvent.ZapAction) =
        viewModelScope.launch {
            val postAuthorProfileData = withContext(dispatcherProvider.io()) {
                profileRepository.findProfileDataOrNull(profileId = zapAction.postAuthorId)
            }

            if (postAuthorProfileData?.lnUrlDecoded == null) {
//                setErrorState(error = FeedError.MissingLightningAddress(IllegalStateException()))
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
//                setErrorState(error = FeedError.FailedToPublishZapEvent(error))
            } catch (error: MissingRelaysException) {
                Timber.w(error)
//                setErrorState(error = FeedError.MissingRelaysConfiguration(error))
            } catch (error: InvalidZapRequestException) {
                Timber.w(error)
//                setErrorState(error = FeedError.InvalidZapRequest(error))
            }
        }

    private fun mute(action: UiEvent.MuteAction) =
        viewModelScope.launch {
            try {
                withContext(dispatcherProvider.io()) {
                    mutedUserRepository.muteUserAndPersistMuteList(
                        userId = activeAccountStore.activeUserId(),
                        mutedUserId = action.userId,
                    )
                }
            } catch (error: WssException) {
                Timber.w(error)
//                setErrorState(error = FeedError.FailedToMuteUser(error))
            } catch (error: NostrPublishException) {
                Timber.w(error)
//                setErrorState(error = FeedError.FailedToMuteUser(error))
            } catch (error: MissingRelaysException) {
                Timber.w(error)
//                setErrorState(error = FeedError.MissingRelaysConfiguration(error))
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
                    setState { copy(shouldApproveBookmark = false) }
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
                    setState { copy(shouldApproveBookmark = true) }
                }
            }
        }

    private fun dismissBookmarkConfirmation() =
        viewModelScope.launch {
            setState { copy(shouldApproveBookmark = false) }
        }

    private fun setErrorState() {
//        setState { copy(error = error) }
        viewModelScope.launch {
            delay(2.seconds)
//            if (state.value.error == error) {
//                setState { copy(error = null) }
//            }
        }
    }
}
