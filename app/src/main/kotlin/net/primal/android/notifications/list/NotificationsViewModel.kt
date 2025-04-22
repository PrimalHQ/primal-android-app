package net.primal.android.notifications.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.cachedIn
import androidx.paging.map
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.Instant
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.getAndUpdate
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.datetime.toJavaInstant
import net.primal.android.core.compose.attachment.model.asEventUriUiModel
import net.primal.android.core.utils.authorNameUiFriendly
import net.primal.android.core.utils.usernameUiFriendly
import net.primal.android.nostr.notary.NostrNotary
import net.primal.android.notes.feed.model.EventStatsUi
import net.primal.android.notes.feed.model.FeedPostUi
import net.primal.android.notes.feed.model.asNoteNostrUriUi
import net.primal.android.notifications.list.NotificationsContract.UiEvent
import net.primal.android.notifications.list.NotificationsContract.UiEvent.NotificationsSeen
import net.primal.android.notifications.list.NotificationsContract.UiState
import net.primal.android.notifications.list.ui.NotificationUi
import net.primal.android.premium.legend.domain.asLegendaryCustomization
import net.primal.android.user.accounts.active.ActiveAccountStore
import net.primal.android.user.subscriptions.SubscriptionsManager
import net.primal.core.networking.sockets.errors.WssException
import net.primal.core.utils.coroutines.DispatcherProvider
import net.primal.domain.nostr.cryptography.SignatureException
import net.primal.domain.nostr.utils.asEllipsizedNpub
import net.primal.domain.notifications.Notification
import net.primal.domain.notifications.NotificationRepository
import timber.log.Timber

@HiltViewModel
class NotificationsViewModel @Inject constructor(
    private val dispatcherProvider: DispatcherProvider,
    private val activeAccountStore: ActiveAccountStore,
    private val notificationRepository: NotificationRepository,
    private val subscriptionsManager: SubscriptionsManager,
    private val nostrNotary: NostrNotary,
) : ViewModel() {

    private val _state = MutableStateFlow(
        UiState(
            seenNotifications = notificationRepository
                .observeSeenNotifications(userId = activeAccountStore.activeUserId())
                .map { it.map { notification -> notification.asNotificationUi() } }
                .cachedIn(viewModelScope),
        ),
    )
    val state = _state.asStateFlow()
    private fun setState(reducer: UiState.() -> UiState) = _state.getAndUpdate { it.reducer() }

    private val events: MutableSharedFlow<UiEvent> = MutableSharedFlow()
    fun setEvent(event: UiEvent) = viewModelScope.launch { events.emit(event) }

    init {
        subscribeToEvents()
        subscribeToActiveAccount()
        subscribeToBadgesUpdates()
        observeUnseenNotifications()
    }

    private fun subscribeToEvents() =
        viewModelScope.launch {
            events.collect {
                when (it) {
                    NotificationsSeen -> handleNotificationsSeen()
                }
            }
        }

    private fun subscribeToActiveAccount() =
        viewModelScope.launch {
            activeAccountStore.activeUserAccount.collect {
                setState {
                    copy(
                        activeAccountAvatarCdnImage = it.avatarCdnImage,
                        activeAccountLegendaryCustomization = it.primalLegendProfile?.asLegendaryCustomization(),
                    )
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

    private fun observeUnseenNotifications() =
        viewModelScope.launch {
            notificationRepository.observeUnseenNotifications(ownerId = activeAccountStore.activeUserId())
                .collect { newUnseenNotifications ->
                    setState {
                        val unseenNotifications = mutableListOf<List<Notification>>()
                        val groupByType = newUnseenNotifications.groupBy { it.type }
                        groupByType.keys.forEach { notificationType ->
                            groupByType[notificationType]?.let { notificationsByType ->
                                when (notificationType.collapsable) {
                                    true -> {
                                        val groupByPostId = notificationsByType.groupBy { it.actionPostId }
                                        groupByPostId.keys.forEach { postId ->
                                            groupByPostId[postId]?.let {
                                                unseenNotifications.add(it)
                                            }
                                        }
                                    }

                                    false -> notificationsByType.forEach {
                                        unseenNotifications.add(listOf(it))
                                    }
                                }
                            }
                        }

                        copy(
                            unseenNotifications = unseenNotifications.map { byType ->
                                byType.map { it.asNotificationUi() }
                            },
                        )
                    }
                }
        }

    private fun handleNotificationsSeen() {
        // Launching in a new scope to survive view model destruction
        CoroutineScope(dispatcherProvider.io()).launch {
            try {
                val authorization = nostrNotary.signAuthorizationNostrEvent(
                    userId = activeAccountStore.activeUserId(),
                    description = "Update notifications last seen timestamp.",
                )
                notificationRepository.markAllNotificationsAsSeen(authorization)
            } catch (error: WssException) {
                Timber.w(error)
            } catch (error: SignatureException) {
                // If user logs out on notifications screen local account gets cleared
                // and when this is called there is no authenticated user and signing fails
                Timber.w(error)
            }
        }
    }

    private fun Notification.asNotificationUi(): NotificationUi {
        return NotificationUi(
            notificationId = this.notificationId,
            ownerId = this.ownerId,
            notificationType = this.type,
            createdAt = Instant.ofEpochSecond(this.createdAt),
            actionUserId = this.actionUserId,
            actionUserDisplayName = this.actionByUser?.authorNameUiFriendly()
                ?: this.actionUserId?.asEllipsizedNpub(),
            actionUserInternetIdentifier = this.actionByUser?.internetIdentifier,
            actionUserAvatarCdnImage = this.actionByUser?.avatarCdnImage,
            actionUserLegendaryCustomization = this.actionByUser?.primalPremiumInfo
                ?.legendProfile?.asLegendaryCustomization(),
            actionUserSatsZapped = this.satsZapped,
            actionPost = this.extractFeedPostUi(),
        )
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
            )
        }
    }
}
