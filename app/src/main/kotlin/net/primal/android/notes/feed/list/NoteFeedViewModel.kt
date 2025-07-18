package net.primal.android.notes.feed.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.cachedIn
import androidx.paging.map
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlin.random.Random
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.getAndUpdate
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import net.primal.android.notes.feed.list.NoteFeedContract.UiEvent
import net.primal.android.notes.feed.list.NoteFeedContract.UiState
import net.primal.android.notes.feed.model.FeedPostsSyncStats
import net.primal.android.notes.feed.model.asFeedPostUi
import net.primal.android.premium.legend.domain.asLegendaryCustomization
import net.primal.android.premium.repository.mapAsProfileDataDO
import net.primal.android.premium.utils.hasPremiumMembership
import net.primal.android.user.accounts.active.ActiveAccountStore
import net.primal.core.utils.coroutines.DispatcherProvider
import net.primal.core.utils.serialization.decodeFromJsonStringOrNull
import net.primal.data.remote.mapper.flatMapNotNullAsCdnResource
import net.primal.data.remote.mapper.mapAsMapPubkeyToListOfBlossomServers
import net.primal.data.repository.mappers.remote.parseAndMapPrimalLegendProfiles
import net.primal.data.repository.mappers.remote.parseAndMapPrimalPremiumInfo
import net.primal.data.repository.mappers.remote.parseAndMapPrimalUserNames
import net.primal.domain.common.exception.NetworkException
import net.primal.domain.feeds.isPremiumFeedSpec
import net.primal.domain.feeds.supportsUpwardsNotesPagination
import net.primal.domain.mutes.MutedItemRepository
import net.primal.domain.nostr.NostrEvent
import net.primal.domain.nostr.findFirstEventId
import net.primal.domain.posts.FeedPageSnapshot
import net.primal.domain.posts.FeedPost
import net.primal.domain.posts.FeedRepository
import timber.log.Timber

@HiltViewModel(assistedFactory = NoteFeedViewModel.Factory::class)
class NoteFeedViewModel @AssistedInject constructor(
    @Assisted private val feedSpec: String,
    @Assisted private val allowMutedThreads: Boolean,
    private val feedRepository: FeedRepository,
    private val activeAccountStore: ActiveAccountStore,
    private val mutedItemRepository: MutedItemRepository,
    private val dispatcherProvider: DispatcherProvider,
) : ViewModel() {

    @AssistedFactory
    interface Factory {
        fun create(feedSpec: String, allowMutedThreads: Boolean): NoteFeedViewModel
    }

    private fun buildFeedByDirective(feedSpec: String) =
        feedRepository.feedBySpec(
            userId = activeAccountStore.activeUserId(),
            feedSpec = feedSpec,
            allowMutedThreads = allowMutedThreads,
        )
            .map { it.map { feedNote -> feedNote.asFeedPostUi() } }
            .cachedIn(viewModelScope)

    private val _state = MutableStateFlow(UiState(notes = buildFeedByDirective(feedSpec)))
    val state = _state.asStateFlow()
    private fun setState(reducer: UiState.() -> UiState) = _state.getAndUpdate { it.reducer() }

    private val events: MutableSharedFlow<UiEvent> = MutableSharedFlow()
    fun setEvent(event: UiEvent) = viewModelScope.launch { events.emit(event) }

    private var latestFeedResponse: FeedPageSnapshot? = null
    private var topVisibleNote: Pair<String, String?>? = null

    private var pollingJob: Job? = null

    init {
        subscribeToEvents()
        observeActiveAccount()
        observeMutedUsers()
    }

    private fun observeMutedUsers() =
        viewModelScope.launch {
            mutedItemRepository.observeMutedProfileIdsByOwnerId(ownerId = activeAccountStore.activeUserId())
                .collect {
                    setState { copy(mutedProfileIds = it) }
                }
        }

    private fun observeActiveAccount() {
        viewModelScope.launch {
            activeAccountStore.activeUserAccount.collect {
                setState {
                    copy(paywall = feedSpec.isPremiumFeedSpec() && !it.hasPremiumMembership())
                }
            }
        }
    }

    private fun subscribeToEvents() =
        viewModelScope.launch {
            events.collect {
                when (it) {
                    UiEvent.FeedScrolledToTop -> handleScrolledToTop()
                    UiEvent.StartPolling -> startPollingIfSupported()
                    UiEvent.StopPolling -> stopPolling()
                    UiEvent.ShowLatestNotes -> showLatestNotes()
                    is UiEvent.UpdateCurrentTopVisibleNote -> {
                        topVisibleNote = it.noteId to it.repostId
                    }
                }
            }
        }

    private fun startPollingIfSupported() {
        if (feedSpec.supportsUpwardsNotesPagination()) {
            pollingJob = viewModelScope.launch(dispatcherProvider.io()) {
                try {
                    while (isActive) {
                        fetchLatestNotes()
                        val pollInterval = POLL_INTERVAL + Random.nextInt(from = -5, until = 5)
                        delay(pollInterval.seconds)
                    }
                } catch (error: NetworkException) {
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

    private fun FeedPostsSyncStats.isTopVisibleNoteTheLatestNote(): Boolean =
        topVisibleNote?.let { topVisibleNote ->
            latestNoteIds.firstOrNull()?.let { newestNoteId ->
                val (noteId, repostId) = topVisibleNote

                newestNoteId == noteId || newestNoteId == repostId
            }
        } == true

    private suspend fun fetchLatestNotes() {
        val latestFeedPageResponse = feedRepository.fetchFeedPageSnapshot(
            userId = activeAccountStore.activeUserId(),
            feedSpec = feedSpec,
        )

        latestFeedResponse = latestFeedPageResponse
        latestFeedPageResponse.processSyncCount(
            newestLocalNote = feedRepository
                .findNewestPosts(
                    userId = activeAccountStore.activeUserId(),
                    feedDirective = feedSpec,
                    limit = 1,
                )
                .firstOrNull(),
        )
    }

    private fun FeedPageSnapshot.processSyncCount(newestLocalNote: FeedPost? = null) {
        val allReferencedNotes = this.referencedEvents.mapNotNull {
            it.content.decodeFromJsonStringOrNull<NostrEvent>()
        }

        val repostedNotes = this.reposts
            .mapNotNull { repostEvent ->
                val noteId = repostEvent.tags.findFirstEventId()
                allReferencedNotes.find { noteId == it.id }?.let {
                    repostEvent.createdAt to it
                }
            }

        val notes = this.notes.map { it.createdAt to it }
        val latestTimestamp = (
            (newestLocalNote?.reposts?.mapNotNull { it.repostCreatedAt } ?: emptyList<Long>()) +
                listOfNotNull(newestLocalNote?.timestamp?.epochSeconds)
            ).maxOrNull()

        val allNotes = (repostedNotes + notes)
            .asSequence()
            .sortedByDescending { it.first }
            .filter { it.first >= (latestTimestamp ?: 0) }
            .distinctBy { it.second.id }
            .filter { it.second.id != newestLocalNote?.eventId }
            .map { it.second }
            .toMutableSet()

        val cdnResources = this.cdnResources.flatMapNotNullAsCdnResource()
        val primalUserNames = this.primalUserNames.parseAndMapPrimalUserNames()
        val primalPremiumInfo = this.primalPremiumInfo.parseAndMapPrimalPremiumInfo()
        val primalLegendProfiles = this.primalLegendProfiles.parseAndMapPrimalLegendProfiles()
        val blossomServers = this.blossomServers.mapAsMapPubkeyToListOfBlossomServers()
        val profiles = this.metadata.mapAsProfileDataDO(
            cdnResources = cdnResources,
            primalUserNames = primalUserNames,
            primalPremiumInfo = primalPremiumInfo,
            primalLegendProfiles = primalLegendProfiles,
            blossomServers = blossomServers,
        )

        val allNotesFromNotMutedProfiles = allNotes.filter { note -> note.pubKey !in state.value.mutedProfileIds }

        val avatarCdnImagesAndLegendaryCustomizations = allNotesFromNotMutedProfiles
            .mapNotNull { note -> profiles.find { it.profileId == note.pubKey } }
            .map { profileData ->
                Pair(
                    profileData.avatarCdnImage,
                    profileData.primalPremiumInfo?.legendProfile?.asLegendaryCustomization(),
                )
            }
            .distinct()

        val limit = avatarCdnImagesAndLegendaryCustomizations.count().coerceAtMost(MAX_AVATARS)

        val newSyncStats = FeedPostsSyncStats(
            latestNoteIds = allNotesFromNotMutedProfiles.map { it.id },
            latestAvatarCdnImages = avatarCdnImagesAndLegendaryCustomizations
                .map { it.first }
                .take(limit),
        )

        if (newSyncStats.isTopVisibleNoteTheLatestNote()) {
            setState { copy(syncStats = FeedPostsSyncStats()) }
        } else {
            setState { copy(syncStats = newSyncStats) }
        }
    }

    private fun showLatestNotes() =
        viewModelScope.launch {
            latestFeedResponse?.let { latestFeed ->
                feedRepository.replaceFeed(
                    userId = activeAccountStore.activeUserId(),
                    feedSpec = feedSpec,
                    snapshot = latestFeed,
                )

                delay(187.milliseconds)
                setState { copy(syncStats = FeedPostsSyncStats(), shouldAnimateScrollToTop = true) }

                viewModelScope.launch {
                    delay(1.seconds)
                    setState { copy(shouldAnimateScrollToTop = false) }
                }
            }
        }

    companion object {
        private const val MAX_AVATARS = 3
        private const val POLL_INTERVAL = 30 // seconds
    }
}
