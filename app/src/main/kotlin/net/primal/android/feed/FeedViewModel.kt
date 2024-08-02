package net.primal.android.feed

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.cachedIn
import androidx.paging.map
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlin.random.Random
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.getAndUpdate
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import net.primal.android.config.AppConfigHandler
import net.primal.android.core.compose.feed.list.FeedUi
import net.primal.android.core.compose.feed.model.FeedPostsSyncStats
import net.primal.android.core.compose.feed.model.asFeedPostUi
import net.primal.android.core.coroutines.CoroutineDispatcherProvider
import net.primal.android.core.ext.asMapByKey
import net.primal.android.core.ext.hasUpwardsPagination
import net.primal.android.core.serialization.json.NostrJson
import net.primal.android.core.serialization.json.decodeFromStringOrNull
import net.primal.android.feed.FeedContract.UiEvent
import net.primal.android.feed.FeedContract.UiState
import net.primal.android.feed.FeedContract.UiState.FeedError
import net.primal.android.feed.api.model.FeedResponse
import net.primal.android.feed.db.FeedPost
import net.primal.android.feed.repository.FeedRepository
import net.primal.android.navigation.feedDirective
import net.primal.android.networking.relays.errors.MissingRelaysException
import net.primal.android.networking.relays.errors.NostrPublishException
import net.primal.android.networking.sockets.errors.WssException
import net.primal.android.nostr.ext.findFirstEventId
import net.primal.android.nostr.ext.flatMapNotNullAsCdnResource
import net.primal.android.nostr.ext.mapAsProfileDataPO
import net.primal.android.nostr.model.NostrEvent
import net.primal.android.note.repository.NoteRepository
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
    private val noteRepository: NoteRepository,
    private val activeAccountStore: ActiveAccountStore,
    private val userDataSyncerFactory: UserDataUpdaterFactory,
    private val appConfigHandler: AppConfigHandler,
    private val profileRepository: ProfileRepository,
    private val zapHandler: ZapHandler,
    private val subscriptionsManager: SubscriptionsManager,
    private val mutedUserRepository: MutedUserRepository,
) : ViewModel() {

    private val feedDirective: String = savedStateHandle.feedDirective ?: activeAccountStore.activeUserId()

    private var userDataUpdater: UserDataUpdater? = null

    private fun buildFeedByDirective(directive: String) =
        feedRepository.feedByDirective(feedDirective = directive)
            .map { it.map { feed -> feed.asFeedPostUi() } }
            .cachedIn(viewModelScope)

    private val _state = MutableStateFlow(
        UiState(
            feedAutoRefresh = feedDirective.hasUpwardsPagination(),
            posts = buildFeedByDirective(feedDirective),
        ),
    )
    val state = _state.asStateFlow()
    private fun setState(reducer: UiState.() -> UiState) = _state.getAndUpdate { it.reducer() }

    private val events: MutableSharedFlow<UiEvent> = MutableSharedFlow()
    fun setEvent(event: UiEvent) = viewModelScope.launch { events.emit(event) }

    private var latestFeedResponse: FeedResponse? = null

    private var pollingJob: Job? = null

    init {
        subscribeToEvents()
        subscribeToActiveAccount()
        subscribeToBadgesUpdates()
        subscribeToFeeds()
    }

    private fun subscribeToFeeds() =
        viewModelScope.launch {
            feedRepository.observeFeeds()
                .distinctUntilChanged()
                .filter { it.isNotEmpty() }
                .collect { feeds ->
                    setState {
                        copy(
                            feeds = feeds.map { FeedUi(directive = it.directive, name = it.name) },
                            feedTitle = feeds.find { it.directive == feedDirective }?.name ?: "",
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

    @Suppress("CyclomaticComplexMethod")
    private fun subscribeToEvents() =
        viewModelScope.launch {
            events.collect {
                when (it) {
                    UiEvent.FeedScrolledToTop -> handleScrolledToTop()
                    UiEvent.RequestUserDataUpdate -> updateUserData()
                    UiEvent.StartPolling -> startPollingIfSupported(feedDirective = feedDirective)
                    UiEvent.StopPolling -> stopPolling()
                    UiEvent.ShowLatestNotes -> showLatestNotes()

                    is UiEvent.UpdateCurrentTopVisibleNote -> setState {
                        copy(topVisibleNote = it.noteId to it.repostId)
                    }

                    is UiEvent.PostLikeAction -> likePost(it)
                    is UiEvent.RepostAction -> repostPost(it)
                    is UiEvent.ZapAction -> zapPost(it)
                    is UiEvent.MuteAction -> mute(it)
                    is UiEvent.ReportAbuse -> reportAbuse(it)
                    is UiEvent.BookmarkAction -> handleBookmark(it)
                    UiEvent.DismissBookmarkConfirmation -> setState { copy(confirmBookmarkingNoteId = null) }
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

    private fun updateUserData() =
        viewModelScope.launch {
            withContext(dispatcherProvider.io()) {
                userDataUpdater?.updateUserDataWithDebounce(30.minutes)
                appConfigHandler.updateAppConfigWithDebounce(30.minutes)
            }
        }

    private fun startPollingIfSupported(feedDirective: String) {
        if (feedDirective.hasUpwardsPagination()) {
            pollingJob = viewModelScope.launch {
                try {
                    while (isActive) {
                        fetchLatestNotes(feedDirective = feedDirective)
                        val pollInterval = POLL_INTERVAL + Random.nextInt(from = -5, until = 5)
                        delay(pollInterval.seconds)
                    }
                } catch (error: WssException) {
                    Timber.e(error)
                }
            }
        }
    }

    private fun stopPolling() {
        pollingJob?.cancel()
    }

    private fun handleScrolledToTop() =
        viewModelScope.launch {
            if (_state.value.syncStats.isTopVisibleNoteTheLatestNote()) {
                setState { copy(syncStats = FeedPostsSyncStats()) }
            }
        }

    private fun FeedPostsSyncStats.isTopVisibleNoteTheLatestNote(): Boolean {
        val topVisibleNote = _state.value.topVisibleNote
        val topNoteId = topVisibleNote?.first

        val newestNoteId = this.latestNoteIds.firstOrNull()
        return newestNoteId == topNoteId
    }

    private suspend fun fetchLatestNotes(feedDirective: String) {
        val feedResponse = feedRepository.fetchLatestNotes(
            userId = activeAccountStore.activeUserId(),
            feedDirective = feedDirective,
        )

        latestFeedResponse = feedResponse
        feedResponse.processSyncCount(
            newestLocalNote = feedRepository
                .findNewestPosts(feedDirective = feedDirective, limit = 1)
                .firstOrNull(),
        )
    }

    private fun FeedResponse.processSyncCount(newestLocalNote: FeedPost? = null) {
        val allReferencedNotes = this.referencedPosts.mapNotNull {
            NostrJson.decodeFromStringOrNull<NostrEvent>(it.content)
        }

        val repostedNotes = this.reposts
            .mapNotNull { repostEvent ->
                val noteId = repostEvent.tags.findFirstEventId()
                allReferencedNotes.find { noteId == it.id }?.let {
                    repostEvent.createdAt to it
                }
            }

        val notes = this.posts
            .map { it.createdAt to it }

        val allNotes = (repostedNotes + notes)
            .asSequence()
            .sortedByDescending { it.first }
            .filter { it.first >= (newestLocalNote?.data?.feedCreatedAt ?: 0) }
            .distinctBy { it.second.id }
            .filter { it.second.id != newestLocalNote?.data?.postId }
            .map { it.second }
            .toMutableSet()

        val cdnResources = this.cdnResources.flatMapNotNullAsCdnResource().asMapByKey { it.url }
        val profiles = this.metadata.mapAsProfileDataPO(cdnResources = cdnResources)
        val avatarCdnImages = allNotes
            .mapNotNull { note -> profiles.find { it.ownerId == note.pubKey }?.avatarCdnImage }
            .distinct()

        val limit = if (avatarCdnImages.count() <= MAX_AVATARS) avatarCdnImages.count() else MAX_AVATARS

        val newSyncStats = FeedPostsSyncStats(
            latestNoteIds = allNotes.map { it.id },
            latestAvatarCdnImages = avatarCdnImages.take(limit),
        )

        if (newSyncStats.isTopVisibleNoteTheLatestNote()) {
            setState { copy(syncStats = FeedPostsSyncStats()) }
        } else {
            setState { copy(syncStats = newSyncStats) }
        }
    }

    private fun showLatestNotes() =
        viewModelScope.launch {
            latestFeedResponse?.let {
                feedRepository.replaceFeedDirective(
                    userId = activeAccountStore.activeUserId(),
                    feedDirective = feedDirective,
                    response = it,
                )
            }
            setState {
                copy(
                    posts = buildFeedByDirective(feedDirective),
                    syncStats = FeedPostsSyncStats(),
                )
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
                setErrorState(error = FeedError.FailedToPublishLikeEvent(error))
            } catch (error: MissingRelaysException) {
                Timber.w(error)
                setErrorState(error = FeedError.MissingRelaysConfiguration(error))
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
        private const val POLL_INTERVAL = 30 // seconds
    }
}
