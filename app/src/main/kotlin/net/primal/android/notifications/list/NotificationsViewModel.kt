package net.primal.android.notifications.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.getAndUpdate
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import net.primal.android.core.compose.feed.asMediaResourceUi
import net.primal.android.core.compose.feed.asNostrResourceUi
import net.primal.android.core.compose.feed.model.FeedPostStatsUi
import net.primal.android.core.compose.feed.model.FeedPostUi
import net.primal.android.core.utils.asEllipsizedNpub
import net.primal.android.notifications.db.Notification
import net.primal.android.notifications.list.NotificationsContract.UiEvent
import net.primal.android.notifications.list.NotificationsContract.UiState
import net.primal.android.notifications.list.ui.NotificationUi
import net.primal.android.notifications.repository.NotificationRepository
import net.primal.android.profile.db.authorNameUiFriendly
import net.primal.android.profile.db.userNameUiFriendly
import net.primal.android.user.accounts.active.ActiveAccountStore
import net.primal.android.user.badges.BadgesManager
import java.time.Instant
import javax.inject.Inject

@HiltViewModel
class NotificationsViewModel @Inject constructor(
    private val activeAccountStore: ActiveAccountStore,
    private val notificationsRepository: NotificationRepository,
    private val badgesManager: BadgesManager,
) : ViewModel() {

    private val _state = MutableStateFlow(UiState())
    val state = _state.asStateFlow()
    private fun setState(reducer: UiState.() -> UiState) = _state.getAndUpdate { it.reducer() }

    private val _event: MutableSharedFlow<UiEvent> = MutableSharedFlow()
    fun setEvent(event: UiEvent) = viewModelScope.launch { _event.emit(event) }

    init {
        subscribeToEvents()
        subscribeToActiveAccount()
        observeNotifications()
        subscribeToBadgesUpdates()
    }

    private fun subscribeToEvents() = viewModelScope.launch {
        _event.collect {
            when (it) {
                UiEvent.NotificationsSeen -> handleNotificationsSeen()
            }
        }
    }

    private fun subscribeToActiveAccount() = viewModelScope.launch {
        activeAccountStore.activeUserAccount.collect {
            setState {
                copy(activeAccountAvatarUrl = it.pictureUrl)
            }
        }
    }

    private fun observeNotifications() = viewModelScope.launch {
        notificationsRepository.observeNotifications()
            .map { list -> list.map { it.asNotificationUi() } }
            .collect {
                setState { copy(notifications = it) }
            }
    }

    private fun subscribeToBadgesUpdates() = viewModelScope.launch {
        badgesManager.badges.collect {
            setState { copy(badges = it) }
            if (it.notifications > 0) {
                fetchNotifications()
            } else if (notificationsRepository.countNotifications() == 0) {
                fetchNotifications()
            }
        }
    }

    private fun fetchNotifications() = viewModelScope.launch {
        setState { copy(loading = true) }
        try {
            val activeUserId = activeAccountStore.activeUserId()
            notificationsRepository.deleteNotifications(userId = activeUserId)
            notificationsRepository.fetchNotifications(userId = activeUserId)
        } finally {
            setState { copy(loading = false) }
        }
    }

    private fun handleNotificationsSeen() = viewModelScope.launch {
        val activeUserId = activeAccountStore.activeUserId()
        notificationsRepository.updateLastSeenTimestamp(userId = activeUserId)
    }

    private fun Notification.asNotificationUi(): NotificationUi {
        return NotificationUi(
            ownerId = this.data.ownerId,
            notificationType = this.data.type,
            createdAt = Instant.ofEpochSecond(this.data.createdAt),
            actionUserId = this.data.actionUserId,
            actionUserDisplayName = this.actionByUser?.authorNameUiFriendly()
                ?: this.data.actionUserId?.asEllipsizedNpub(),
            actionUserInternetIdentifier = this.actionByUser?.internetIdentifier,
            actionUserPicture = this.actionByUser?.picture,
            actionUserSatsZapped = this.data.satsZapped,
            actionPost = this.extractFeedPostUi(),
        )
    }

    private fun Notification.extractFeedPostUi(): FeedPostUi? {
        if (this.actionPost == null) return null

        return FeedPostUi(
            postId = this.actionPost.postId,
            authorId = this.actionPost.authorId,
            authorName = this.actionByUser?.authorNameUiFriendly() ?: this.actionPost.authorId.asEllipsizedNpub(),
            authorHandle = this.actionByUser?.userNameUiFriendly() ?: this.actionPost.authorId.asEllipsizedNpub(),
            authorInternetIdentifier = this.actionByUser?.internetIdentifier,
            authorLightningAddress = this.actionByUser?.lightningAddress,
            authorAvatarUrl = this.actionByUser?.picture,
            timestamp = Instant.ofEpochSecond(this.actionPost.createdAt),
            content = this.actionPost.content,
            authorMediaResources = this.actionByUserResources.map { it.asMediaResourceUi() },
            mediaResources = this.actionPostMediaResources.map { it.asMediaResourceUi() },
            nostrResources = this.actionPostNostrUris.map { it.asNostrResourceUi() },
            stats = FeedPostStatsUi(
                repliesCount = this.actionPostStats?.replies ?: 0,
                userReplied = this.actionPostUserStats?.replied ?: false,
                zapsCount = this.actionPostStats?.zaps ?: 0,
                satsZapped = this.actionPostStats?.satsZapped ?: 0,
                userZapped = this.actionPostUserStats?.zapped ?: false,
                likesCount = this.actionPostStats?.likes ?: 0,
                userLiked = this.actionPostUserStats?.liked ?: false,
                repostsCount = this.actionPostStats?.reposts ?: 0,
                userReposted = this.actionPostUserStats?.reposted ?: false,
            ),
            hashtags = this.actionPost.hashtags,
            rawNostrEventJson = this.actionPost.raw,
        )
    }
}
