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
import net.primal.android.core.ext.asMapByKey
import net.primal.android.core.serialization.json.NostrJson
import net.primal.android.core.serialization.json.decodeFromStringOrNull
import net.primal.android.feeds.domain.isPremiumFeedSpec
import net.primal.android.feeds.domain.supportsUpwardsNotesPagination
import net.primal.android.networking.sockets.errors.WssException
import net.primal.android.nostr.ext.findFirstEventId
import net.primal.android.nostr.ext.flatMapNotNullAsCdnResource
import net.primal.android.nostr.ext.mapAsMapPubkeyToListOfBlossomServers
import net.primal.android.nostr.ext.mapAsProfileDataPO
import net.primal.android.nostr.ext.parseAndMapPrimalLegendProfiles
import net.primal.android.nostr.ext.parseAndMapPrimalPremiumInfo
import net.primal.android.nostr.ext.parseAndMapPrimalUserNames
import net.primal.android.nostr.model.NostrEvent
import net.primal.android.notes.api.model.FeedResponse
import net.primal.android.notes.db.FeedPost
import net.primal.android.notes.feed.list.NoteFeedContract.UiEvent
import net.primal.android.notes.feed.list.NoteFeedContract.UiState
import net.primal.android.notes.feed.model.FeedPostsSyncStats
import net.primal.android.notes.feed.model.asFeedPostUi
import net.primal.android.notes.repository.FeedRepository
import net.primal.android.premium.legend.asLegendaryCustomization
import net.primal.android.premium.utils.hasPremiumMembership
import net.primal.android.user.accounts.active.ActiveAccountStore
import timber.log.Timber

@HiltViewModel(assistedFactory = NoteFeedViewModel.Factory::class)
class NoteFeedViewModel @AssistedInject constructor(
    @Assisted private val feedSpec: String,
    private val feedRepository: FeedRepository,
    private val activeAccountStore: ActiveAccountStore,
) : ViewModel() {

    @AssistedFactory
    interface Factory {
        fun create(feedSpec: String): NoteFeedViewModel
    }

    private fun buildFeedByDirective(feedSpec: String) =
        feedRepository.feedBySpec(feedSpec = feedSpec)
            .map { it.map { feedNote -> feedNote.asFeedPostUi() } }
            .cachedIn(viewModelScope)

    private val _state = MutableStateFlow(UiState(notes = buildFeedByDirective(feedSpec)))
    val state = _state.asStateFlow()
    private fun setState(reducer: UiState.() -> UiState) = _state.getAndUpdate { it.reducer() }

    private val events: MutableSharedFlow<UiEvent> = MutableSharedFlow()
    fun setEvent(event: UiEvent) = viewModelScope.launch { events.emit(event) }

    private var latestFeedResponse: FeedResponse? = null
    private var topVisibleNote: Pair<String, String?>? = null

    private var pollingJob: Job? = null

    init {
        subscribeToEvents()
        observeActiveAccount()
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
            pollingJob = viewModelScope.launch {
                try {
                    while (isActive) {
                        fetchLatestNotes()
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
        val topNoteId = topVisibleNote?.first
        val newestNoteId = this.latestNoteIds.firstOrNull()
        return newestNoteId == topNoteId
    }

    private suspend fun fetchLatestNotes() {
        val feedResponse = feedRepository.fetchLatestNotes(
            userId = activeAccountStore.activeUserId(),
            feedSpec = feedSpec,
        )

        latestFeedResponse = feedResponse
        feedResponse.processSyncCount(
            newestLocalNote = feedRepository
                .findNewestPosts(feedDirective = feedSpec, limit = 1)
                .firstOrNull(),
        )
    }

    private fun FeedResponse.processSyncCount(newestLocalNote: FeedPost? = null) {
        val allReferencedNotes = this.referencedEvents.mapNotNull {
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
        val primalUserNames = this.primalUserNames.parseAndMapPrimalUserNames()
        val primalPremiumInfo = this.primalPremiumInfo.parseAndMapPrimalPremiumInfo()
        val primalLegendProfiles = this.primalLegendProfiles.parseAndMapPrimalLegendProfiles()
        val blossomServers = this.blossomServers.mapAsMapPubkeyToListOfBlossomServers()
        val profiles = this.metadata.mapAsProfileDataPO(
            cdnResources = cdnResources,
            primalUserNames = primalUserNames,
            primalPremiumInfo = primalPremiumInfo,
            primalLegendProfiles = primalLegendProfiles,
            blossomServers = blossomServers,
        )
        val avatarCdnImagesAndLegendaryCustomizations = allNotes
            .mapNotNull { note -> profiles.find { it.ownerId == note.pubKey } }
            .filter { profileData -> profileData.avatarCdnImage != null }
            .map { profileData ->
                Pair(
                    profileData.avatarCdnImage,
                    profileData.primalPremiumInfo?.legendProfile?.asLegendaryCustomization(),
                )
            }
            .distinct()

        val limit = avatarCdnImagesAndLegendaryCustomizations.count().coerceAtMost(MAX_AVATARS)

        val newSyncStats = FeedPostsSyncStats(
            latestNoteIds = allNotes.map { it.id },
            latestAvatarCdnImages = avatarCdnImagesAndLegendaryCustomizations.mapNotNull { it.first }.take(limit),
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
                feedRepository.replaceFeedSpec(
                    userId = activeAccountStore.activeUserId(),
                    feedSpec = feedSpec,
                    response = latestFeed,
                )

                delay(130.milliseconds)
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
