package net.primal.android.discuss.feed

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.cachedIn
import androidx.paging.map
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.Instant
import javax.inject.Inject
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.getAndUpdate
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import net.primal.android.config.dynamic.AppConfigUpdater
import net.primal.android.core.compose.feed.model.FeedPostsSyncStats
import net.primal.android.core.compose.feed.model.asFeedPostUi
import net.primal.android.core.coroutines.CoroutineDispatcherProvider
import net.primal.android.core.utils.ellipsizeMiddle
import net.primal.android.discuss.feed.FeedContract.UiEvent
import net.primal.android.discuss.feed.FeedContract.UiState
import net.primal.android.discuss.feed.FeedContract.UiState.FeedError
import net.primal.android.feed.repository.FeedRepository
import net.primal.android.feed.repository.PostRepository
import net.primal.android.navigation.feedDirective
import net.primal.android.networking.relays.errors.MissingRelaysException
import net.primal.android.networking.relays.errors.NostrPublishException
import net.primal.android.networking.sockets.errors.WssException
import net.primal.android.profile.repository.ProfileRepository
import net.primal.android.settings.muted.repository.MutedUserRepository
import net.primal.android.user.accounts.active.ActiveAccountStore
import net.primal.android.user.subscriptions.SubscriptionsManager
import net.primal.android.user.updater.UserDataUpdater
import net.primal.android.user.updater.UserDataUpdaterFactory
import net.primal.android.wallet.domain.ZapTarget
import net.primal.android.wallet.zaps.InvalidZapRequestException
import net.primal.android.wallet.zaps.ZapFailureException
import net.primal.android.wallet.zaps.ZapHandler
import net.primal.android.wallet.zaps.hasWallet
import timber.log.Timber

@HiltViewModel
class FeedViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val dispatcherProvider: CoroutineDispatcherProvider,
    private val feedRepository: FeedRepository,
    private val postRepository: PostRepository,
    private val activeAccountStore: ActiveAccountStore,
    private val userDataSyncerFactory: UserDataUpdaterFactory,
    private val appConfigUpdater: AppConfigUpdater,
    private val profileRepository: ProfileRepository,
    private val zapHandler: ZapHandler,
    private val subscriptionsManager: SubscriptionsManager,
    private val mutedUserRepository: MutedUserRepository,
) : ViewModel() {

    private val feedDirective: String = savedStateHandle.feedDirective ?: activeAccountStore.activeUserId()

    private var userDataUpdater: UserDataUpdater? = null

    private val _state = MutableStateFlow(
        UiState(
            posts = feedRepository.feedByDirective(feedDirective = feedDirective)
                .map { it.map { feed -> feed.asFeedPostUi() } }
                .cachedIn(viewModelScope),
        ),
    )
    val state = _state.asStateFlow()
    private fun setState(reducer: UiState.() -> UiState) = _state.getAndUpdate { it.reducer() }

    private val events: MutableSharedFlow<UiEvent> = MutableSharedFlow()
    fun setEvent(event: UiEvent) = viewModelScope.launch { events.emit(event) }

    init {
        subscribeToFeedTitle()
        subscribeToEvents()
        subscribeToFeedSyncUpdates()
        subscribeToActiveAccount()
        subscribeToBadgesUpdates()
    }

    private fun subscribeToFeedTitle() =
        viewModelScope.launch {
            feedRepository.observeFeedByDirective(feedDirective = feedDirective).collect {
                setState {
                    copy(feedTitle = it?.name ?: feedDirective.ellipsizeMiddle(size = 8))
                }
            }
        }

    private fun subscribeToFeedSyncUpdates() =
        viewModelScope.launch {
            feedRepository.observeNewFeedPostsSyncUpdates(
                feedDirective = feedDirective,
                since = Instant.now().epochSecond,
            ).collect { syncData ->
                val limit = if (syncData.count <= MAX_AVATARS) syncData.count else MAX_AVATARS
                val newPosts = withContext(dispatcherProvider.io()) {
                    feedRepository.findNewestPosts(
                        feedDirective = feedDirective,
                        limit = syncData.count,
                    )
                        .filter { it.author?.avatarCdnImage != null }
                        .distinctBy { it.author?.ownerId }
                        .take(limit)
                }
                setState {
                    copy(
                        syncStats = FeedPostsSyncStats(
                            postsCount = this.syncStats.postsCount + syncData.count,
                            postIds = this.syncStats.postIds + syncData.postIds,
                            avatarCdnImages = newPosts.mapNotNull { it.author?.avatarCdnImage },
                        ),
                    )
                }
            }
        }

    private fun subscribeToActiveAccount() =
        viewModelScope.launch {
            activeAccountStore.activeUserAccount.collect {
                val activeUserId = it.pubkey
                userDataUpdater = if (userDataUpdater?.userId != activeUserId) {
                    userDataSyncerFactory.create(userId = activeUserId)
                } else {
                    userDataUpdater
                }

                setState {
                    copy(
                        activeAccountAvatarCdnImage = it.avatarCdnImage,
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

    private fun subscribeToEvents() =
        viewModelScope.launch {
            events.collect {
                when (it) {
                    UiEvent.FeedScrolledToTop -> clearSyncStats()
                    UiEvent.RequestUserDataUpdate -> updateUserData()
                    is UiEvent.PostLikeAction -> likePost(it)
                    is UiEvent.RepostAction -> repostPost(it)
                    is UiEvent.ZapAction -> zapPost(it)
                    is UiEvent.MuteAction -> mute(it)
                    is UiEvent.ReportAbuse -> reportAbuse(it)
                    is UiEvent.BookmarkAction -> handleBookmark(it)
                }
            }
        }

    private fun subscribeToBadgesUpdates() =
        viewModelScope.launch {
            subscriptionsManager.badges.collect {
                setState {
                    copy(badges = it)
                }
            }
        }

    private fun clearSyncStats() {
        setState {
            copy(
                syncStats = this.syncStats.copy(
                    postIds = emptyList(),
                    postsCount = 0,
                ),
            )
        }
    }

    private fun updateUserData() =
        viewModelScope.launch {
            withContext(dispatcherProvider.io()) {
                userDataUpdater?.updateUserDataWithDebounce(30.minutes)
                appConfigUpdater.updateAppConfigWithDebounce(30.minutes)
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
                setErrorState(error = FeedError.FailedToPublishLikeEvent(error))
            } catch (error: MissingRelaysException) {
                Timber.w(error)
                setErrorState(error = FeedError.MissingRelaysConfiguration(error))
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
                setErrorState(error = FeedError.FailedToPublishRepostEvent(error))
            } catch (error: MissingRelaysException) {
                Timber.w(error)
                setErrorState(error = FeedError.MissingRelaysConfiguration(error))
            }
        }

    private fun zapPost(zapAction: UiEvent.ZapAction) =
        viewModelScope.launch {
            val postAuthorProfileData = withContext(dispatcherProvider.io()) {
                profileRepository.findProfileDataOrNull(profileId = zapAction.postAuthorId)
            }

            if (postAuthorProfileData?.lnUrlDecoded == null) {
                setErrorState(error = FeedError.MissingLightningAddress(IllegalStateException()))
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
                setErrorState(error = FeedError.FailedToPublishZapEvent(error))
            } catch (error: MissingRelaysException) {
                Timber.w(error)
                setErrorState(error = FeedError.MissingRelaysConfiguration(error))
            } catch (error: InvalidZapRequestException) {
                Timber.w(error)
                setErrorState(error = FeedError.InvalidZapRequest(error))
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
                setErrorState(error = FeedError.FailedToMuteUser(error))
            } catch (error: NostrPublishException) {
                Timber.w(error)
                setErrorState(error = FeedError.FailedToMuteUser(error))
            } catch (error: MissingRelaysException) {
                Timber.w(error)
                setErrorState(error = FeedError.MissingRelaysConfiguration(error))
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
                    val isBookmarked = postRepository.isBookmarked(noteId = event.noteId)
                    when (isBookmarked) {
                        true -> postRepository.removeFromBookmarks(
                            userId = userId,
                            forceUpdate = event.firstBookmarkConfirmed,
                            noteId = event.noteId,
                        )

                        false -> postRepository.addToBookmarks(
                            userId = userId,
                            forceUpdate = event.firstBookmarkConfirmed,
                            noteId = event.noteId,
                        )
                    }
                } catch (error: NostrPublishException) {
                    Timber.w(error)
                } catch (error: ProfileRepository.BookmarksListNotFound) {
                    Timber.w(error)
                }
            }
        }

    private fun setErrorState(error: FeedError) {
        setState { copy(error = error) }
        viewModelScope.launch {
            delay(2.seconds)
            if (state.value.error == error) {
                setState { copy(error = null) }
            }
        }
    }

    companion object {
        private const val MAX_AVATARS = 3
    }
}
