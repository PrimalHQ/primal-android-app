package net.primal.android.main.notifications

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.map
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.aakira.napier.Napier
import java.time.Instant
import javax.inject.Inject
import kotlin.time.toJavaInstant
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.getAndUpdate
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.launch
import net.primal.android.core.compose.attachment.model.asEventUriUiModel
import net.primal.android.core.utils.authorNameUiFriendly
import net.primal.android.core.utils.isOnlyEmoji
import net.primal.android.core.utils.usernameUiFriendly
import net.primal.android.events.polls.votes.asPollUi
import net.primal.android.main.notifications.NotificationsContract.UiEvent
import net.primal.android.main.notifications.NotificationsContract.UiState
import net.primal.android.nostr.notary.NostrNotary
import net.primal.android.notes.feed.model.EventStatsUi
import net.primal.android.notes.feed.model.FeedPostUi
import net.primal.android.notes.feed.model.asNoteNostrUriUi
import net.primal.android.notifications.list.ui.NotificationUi
import net.primal.android.premium.legend.domain.asLegendaryCustomization
import net.primal.android.user.accounts.active.ActiveAccountStore
import net.primal.android.user.subscriptions.SubscriptionsManager
import net.primal.core.utils.coroutines.DispatcherProvider
import net.primal.domain.common.exception.NetworkException
import net.primal.domain.links.ReferencedStream
import net.primal.domain.nostr.cryptography.SignResult
import net.primal.domain.nostr.utils.asEllipsizedNpub
import net.primal.domain.notifications.Notification
import net.primal.domain.notifications.NotificationGroup
import net.primal.domain.notifications.NotificationRepository
import net.primal.domain.notifications.NotificationType
import net.primal.domain.streams.mappers.asReferencedStream

@HiltViewModel
class NotificationsViewModel @Inject constructor(
    private val dispatcherProvider: DispatcherProvider,
    private val activeAccountStore: ActiveAccountStore,
    private val notificationRepository: NotificationRepository,
    private val subscriptionsManager: SubscriptionsManager,
    private val nostrNotary: NostrNotary,
) : ViewModel() {

    private val seenPagerCache: Map<NotificationGroup, Flow<PagingData<NotificationUi>>> =
        NotificationGroup.entries.associateWith { group ->
            notificationRepository
                .observeSeenNotifications(userId = activeAccountStore.activeUserId(), group = group)
                .map { it.map { notification -> notification.asNotificationUi() } }
                .cachedIn(viewModelScope)
        }

    private val unseenCache: Map<NotificationGroup, Flow<List<List<NotificationUi>>>> =
        NotificationGroup.entries.associateWith { group ->
            notificationRepository
                .observeUnseenNotifications(ownerId = activeAccountStore.activeUserId(), group = group)
                .map { notifications -> groupUnseenNotifications(notifications) }
                .shareIn(
                    scope = viewModelScope,
                    started = SharingStarted.WhileSubscribed(stopTimeoutMillis = 5_000),
                    replay = 1,
                )
        }

    internal fun seenNotificationsForGroup(group: NotificationGroup): Flow<PagingData<NotificationUi>> =
        seenPagerCache.getValue(group)

    internal fun unseenNotificationsForGroup(group: NotificationGroup): Flow<List<List<NotificationUi>>> =
        unseenCache.getValue(group)

    private val _state = MutableStateFlow(UiState())
    val state = _state.asStateFlow()
    private fun setState(reducer: UiState.() -> UiState) = _state.getAndUpdate { it.reducer() }

    private val events: MutableSharedFlow<UiEvent> = MutableSharedFlow()
    fun setEvent(event: UiEvent) = viewModelScope.launch { events.emit(event) }

    init {
        subscribeToEvents()
        subscribeToBadgesUpdates()
    }

    private fun subscribeToEvents() =
        viewModelScope.launch {
            events.collect {
                when (it) {
                    is UiEvent.NotificationsSeen -> handleNotificationsSeen(it.group)
                }
            }
        }

    private fun subscribeToBadgesUpdates() =
        viewModelScope.launch {
            subscriptionsManager.badges.collect {
                setState { copy(badges = it) }
            }
        }

    private fun handleNotificationsSeen(group: NotificationGroup) {
        if (group != NotificationGroup.ALL) return
        // Launching in a new scope to survive view model destruction
        CoroutineScope(dispatcherProvider.io()).launch {
            try {
                val signResult = nostrNotary.signAuthorizationNostrEvent(
                    userId = activeAccountStore.activeUserId(),
                    description = "Update notifications last seen timestamp.",
                )

                when (signResult) {
                    is SignResult.Rejected -> Napier.w(throwable = signResult.error) {
                        "Sign rejected while updating notifications last seen."
                    }
                    is SignResult.Signed -> notificationRepository.markAllNotificationsAsSeen(signResult.event)
                }
            } catch (error: NetworkException) {
                Napier.w(throwable = error) { "Failed to mark notifications as seen due to network error." }
            }
        }
    }

    @Suppress("NestedBlockDepth")
    private fun groupUnseenNotifications(notifications: List<Notification>): List<List<NotificationUi>> {
        val unseenNotifications = mutableListOf<List<Notification>>()
        val groupByType = notifications.groupBy { it.type }
        groupByType.keys.forEach { notificationType ->
            groupByType[notificationType]?.let { notificationsByType ->
                when (notificationType.collapsable) {
                    true -> {
                        val groupByPostId = notificationsByType.groupBy { it.actionPostId }
                        groupByPostId.keys.forEach { postId ->
                            groupByPostId[postId]?.let {
                                if (notificationType.isLike()) {
                                    it.map {
                                        it.copy(
                                            reaction = if (it.reaction?.isOnlyEmoji() == true) {
                                                it.reaction
                                            } else {
                                                "+"
                                            },
                                        )
                                    }.groupBy { it.reaction }
                                        .onEach { (_, notifications) ->
                                            unseenNotifications.add(notifications)
                                        }
                                } else {
                                    unseenNotifications.add(it)
                                }
                            }
                        }
                    }

                    false -> notificationsByType.forEach {
                        unseenNotifications.add(listOf(it))
                    }
                }
            }
        }

        return unseenNotifications.map { byType -> byType.map { it.asNotificationUi() } }
    }

    private fun NotificationType.isLike() =
        this == NotificationType.YOUR_POST_WAS_LIKED ||
            this == NotificationType.POST_YOU_WERE_MENTIONED_IN_WAS_LIKED ||
            this == NotificationType.POST_YOUR_POST_WAS_MENTIONED_IN_WAS_LIKED

    private fun Notification.asNotificationUi(): NotificationUi {
        return NotificationUi(
            notificationId = this.notificationId,
            ownerId = this.ownerId,
            notificationType = this.type,
            createdAt = Instant.ofEpochSecond(this.createdAt),
            actionUserId = this.actionUserId,
            reaction = this.reaction,
            actionUserDisplayName = this.actionByUser?.authorNameUiFriendly()
                ?: this.actionUserId?.asEllipsizedNpub(),
            actionUserInternetIdentifier = this.actionByUser?.internetIdentifier,
            actionUserAvatarCdnImage = this.actionByUser?.avatarCdnImage,
            actionUserLegendaryCustomization = this.actionByUser?.primalPremiumInfo
                ?.legendProfile?.asLegendaryCustomization(),
            actionUserSatsZapped = this.satsZapped,
            actionPost = this.extractFeedPostUi(),
            referencedStream = this.extractReferencedStream(),
        )
    }

    private fun Notification.extractReferencedStream(): ReferencedStream? {
        return this.liveActivity.let {
            this.liveActivity?.asReferencedStream()
        }
    }

    private fun Notification.extractFeedPostUi(): FeedPostUi? {
        return this.actionOnPost?.let { actionOnPost ->
            FeedPostUi(
                postId = actionOnPost.eventId,
                authorId = actionOnPost.author.authorId,
                authorName = this.actionByUser?.authorNameUiFriendly()
                    ?: actionOnPost.author.displayName,
                authorHandle = this.actionByUser?.usernameUiFriendly()
                    ?: actionOnPost.author.handle,
                authorInternetIdentifier = this.actionByUser?.internetIdentifier,
                authorAvatarCdnImage = this.actionByUser?.avatarCdnImage,
                timestamp = actionOnPost.timestamp.toJavaInstant(),
                content = actionOnPost.content,
                uris = actionOnPost.links.map { it.asEventUriUiModel() },
                nostrUris = actionOnPost.nostrUris.map { it.asNoteNostrUriUi() },
                stats = EventStatsUi.from(actionOnPost.stats),
                hashtags = actionOnPost.hashtags,
                rawNostrEventJson = actionOnPost.rawNostrEvent,
                poll = actionOnPost.pollInfo?.asPollUi(),
            )
        }
    }
}
