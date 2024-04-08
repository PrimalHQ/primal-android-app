package net.primal.android.explore.feed

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.cachedIn
import androidx.paging.map
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.getAndUpdate
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import net.primal.android.core.compose.feed.model.asFeedPostUi
import net.primal.android.core.coroutines.CoroutineDispatcherProvider
import net.primal.android.core.ext.isBookmarkFeed
import net.primal.android.core.ext.isSearchFeed
import net.primal.android.core.ext.removeSearchPrefix
import net.primal.android.explore.feed.ExploreFeedContract.UiEvent
import net.primal.android.explore.feed.ExploreFeedContract.UiState
import net.primal.android.explore.feed.ExploreFeedContract.UiState.ExploreFeedError
import net.primal.android.feed.repository.FeedRepository
import net.primal.android.feed.repository.PostRepository
import net.primal.android.navigation.exploreFeedDirectiveOrThrow
import net.primal.android.networking.relays.errors.MissingRelaysException
import net.primal.android.networking.relays.errors.NostrPublishException
import net.primal.android.networking.sockets.errors.WssException
import net.primal.android.profile.repository.ProfileRepository
import net.primal.android.settings.muted.repository.MutedUserRepository
import net.primal.android.settings.repository.SettingsRepository
import net.primal.android.user.accounts.active.ActiveAccountStore
import net.primal.android.user.accounts.active.ActiveUserAccountState
import net.primal.android.wallet.domain.ZapTarget
import net.primal.android.wallet.zaps.InvalidZapRequestException
import net.primal.android.wallet.zaps.ZapFailureException
import net.primal.android.wallet.zaps.ZapHandler
import net.primal.android.wallet.zaps.hasWallet
import timber.log.Timber

@HiltViewModel
class ExploreFeedViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val dispatcherProvider: CoroutineDispatcherProvider,
    private val activeAccountStore: ActiveAccountStore,
    private val feedRepository: FeedRepository,
    private val postRepository: PostRepository,
    private val profileRepository: ProfileRepository,
    private val zapHandler: ZapHandler,
    private val settingsRepository: SettingsRepository,
    private val mutedUserRepository: MutedUserRepository,
) : ViewModel() {

    private val exploreFeedDirective = savedStateHandle.exploreFeedDirectiveOrThrow

    private val _state = MutableStateFlow(
        UiState(
            title = exploreFeedDirective.resolveTitle(),
            posts = feedRepository.feedByDirective(feedDirective = exploreFeedDirective)
                .map { it.map { feed -> feed.asFeedPostUi() } }
                .cachedIn(viewModelScope),
        ),
    )
    val state = _state.asStateFlow()
    private fun setState(reducer: UiState.() -> UiState) {
        _state.getAndUpdate { it.reducer() }
    }

    private val events: MutableSharedFlow<UiEvent> = MutableSharedFlow()
    fun setEvent(event: UiEvent) {
        viewModelScope.launch { events.emit(event) }
    }

    init {
        observeContainsFeed()
        observeEvents()
        observeActiveAccount()
    }

    private fun observeContainsFeed() =
        viewModelScope.launch {
            feedRepository.observeContainsFeed(directive = exploreFeedDirective).collect {
                setState {
                    copy(existsInUserFeeds = it)
                }
            }
        }

    private fun observeEvents() =
        viewModelScope.launch {
            events.collect {
                when (it) {
                    UiEvent.AddToUserFeeds -> addToMyFeeds()
                    UiEvent.RemoveFromUserFeeds -> removeFromMyFeeds()
                    is UiEvent.PostLikeAction -> likePost(it)
                    is UiEvent.RepostAction -> repostPost(it)
                    is UiEvent.ZapAction -> zapPost(it)
                    is UiEvent.MuteAction -> mute(it)
                    is UiEvent.ReportAbuse -> reportAbuse(it)
                    is UiEvent.BookmarkAction -> handleBookmark(it)
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

    private suspend fun addToMyFeeds() {
        try {
            settingsRepository.addAndPersistUserFeed(
                userId = activeAccountStore.activeUserId(),
                name = state.value.title,
                directive = exploreFeedDirective,
            )
        } catch (error: WssException) {
            Timber.w(error)
            setErrorState(error = ExploreFeedError.FailedToAddToFeed(error))
        }
    }

    private suspend fun removeFromMyFeeds() {
        try {
            settingsRepository.removeAndPersistUserFeed(
                userId = activeAccountStore.activeUserId(),
                directive = exploreFeedDirective,
            )
        } catch (error: WssException) {
            Timber.w(error)
            setErrorState(error = ExploreFeedError.FailedToRemoveFeed(error))
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
                Timber.w(error)
                setErrorState(error = ExploreFeedError.FailedToPublishLikeEvent(error))
            } catch (error: MissingRelaysException) {
                Timber.w(error)
                setErrorState(error = ExploreFeedError.MissingRelaysConfiguration(error))
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
                Timber.w(error)
                setErrorState(error = ExploreFeedError.FailedToPublishRepostEvent(error))
            } catch (error: MissingRelaysException) {
                Timber.w(error)
                setErrorState(error = ExploreFeedError.MissingRelaysConfiguration(error))
            }
        }

    private fun zapPost(zapAction: UiEvent.ZapAction) =
        viewModelScope.launch {
            val postAuthorProfileData = withContext(Dispatchers.IO) {
                profileRepository.findProfileDataOrNull(profileId = zapAction.postAuthorId)
            }

            if (postAuthorProfileData?.lnUrlDecoded == null) {
                setErrorState(
                    error = ExploreFeedError.MissingLightningAddress(IllegalStateException()),
                )
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
                setErrorState(error = ExploreFeedError.FailedToPublishZapEvent(error))
            } catch (error: MissingRelaysException) {
                Timber.w(error)
                setErrorState(error = ExploreFeedError.MissingRelaysConfiguration(error))
            } catch (error: InvalidZapRequestException) {
                Timber.w(error)
                setErrorState(error = ExploreFeedError.InvalidZapRequest(error))
            }
        }

    private fun mute(action: UiEvent.MuteAction) =
        viewModelScope.launch {
            try {
                withContext(dispatcherProvider.io()) {
                    mutedUserRepository.muteUserAndPersistMuteList(
                        userId = activeAccountStore.activeUserId(),
                        mutedUserId = action.profileId,
                    )
                }
            } catch (error: WssException) {
                Timber.w(error)
                setErrorState(error = ExploreFeedError.FailedToMuteUser(error))
            } catch (error: NostrPublishException) {
                Timber.w(error)
                setErrorState(error = ExploreFeedError.FailedToMuteUser(error))
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
                val isBookmarked = postRepository.isBookmarked(noteId = event.noteId)
                when (isBookmarked) {
                    true -> postRepository.removeFromBookmarks(userId = userId, noteId = event.noteId)
                    false -> postRepository.addToBookmarks(userId = userId, noteId = event.noteId)
                }
            }
        }

    private fun setErrorState(error: ExploreFeedError) {
        setState { copy(error = error) }
        viewModelScope.launch {
            delay(2.seconds)
            if (state.value.error == error) {
                setState { copy(error = null) }
            }
        }
    }

    private fun String.resolveTitle(): String {
        return when {
            this.isSearchFeed() -> removeSearchPrefix()
            this.isBookmarkFeed() -> "Bookmarks"
            else -> "Feed"
        }
    }
}
