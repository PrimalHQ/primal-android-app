package net.primal.android.notifications.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.cachedIn
import androidx.paging.map
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
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
import net.primal.android.core.utils.authorNameUiFriendly
import net.primal.android.core.utils.usernameUiFriendly
import net.primal.android.feed.repository.PostRepository
import net.primal.android.networking.relays.errors.MissingRelaysException
import net.primal.android.networking.relays.errors.NostrPublishException
import net.primal.android.notifications.db.Notification
import net.primal.android.notifications.list.NotificationsContract.UiEvent
import net.primal.android.notifications.list.NotificationsContract.UiEvent.NotificationsSeen
import net.primal.android.notifications.list.NotificationsContract.UiEvent.PostLikeAction
import net.primal.android.notifications.list.NotificationsContract.UiEvent.RepostAction
import net.primal.android.notifications.list.NotificationsContract.UiState
import net.primal.android.notifications.list.NotificationsContract.UiState.NotificationsError.FailedToPublishLikeEvent
import net.primal.android.notifications.list.NotificationsContract.UiState.NotificationsError.FailedToPublishRepostEvent
import net.primal.android.notifications.list.NotificationsContract.UiState.NotificationsError.FailedToPublishZapEvent
import net.primal.android.notifications.list.NotificationsContract.UiState.NotificationsError.InvalidZapRequest
import net.primal.android.notifications.list.NotificationsContract.UiState.NotificationsError.MissingLightningAddress
import net.primal.android.notifications.list.NotificationsContract.UiState.NotificationsError.MissingRelaysConfiguration
import net.primal.android.notifications.list.ui.NotificationUi
import net.primal.android.notifications.repository.NotificationRepository
import net.primal.android.user.accounts.active.ActiveAccountStore
import net.primal.android.user.badges.BadgesManager
import net.primal.android.wallet.model.ZapTarget
import net.primal.android.wallet.repository.ZapRepository
import java.time.Instant
import javax.inject.Inject
import kotlin.time.Duration.Companion.seconds

@HiltViewModel
class NotificationsViewModel @Inject constructor(
    private val activeAccountStore: ActiveAccountStore,
    private val notificationsRepository: NotificationRepository,
    private val postRepository: PostRepository,
    private val zapRepository: ZapRepository,
    private val badgesManager: BadgesManager,
) : ViewModel() {

    private val _state = MutableStateFlow(
        UiState(
            seenNotifications = notificationsRepository.observeSeenNotifications()
                .map { it.map { notification -> notification.asNotificationUi() } }
                .cachedIn(viewModelScope)
        )
    )
    val state = _state.asStateFlow()
    private fun setState(reducer: UiState.() -> UiState) = _state.getAndUpdate { it.reducer() }

    private val _event: MutableSharedFlow<UiEvent> = MutableSharedFlow()
    fun setEvent(event: UiEvent) = viewModelScope.launch { _event.emit(event) }

    init {
        subscribeToEvents()
        subscribeToActiveAccount()
        subscribeToBadgesUpdates()
        observeUnseenNotifications()
    }

    private fun subscribeToEvents() = viewModelScope.launch {
        _event.collect {
            when (it) {
                NotificationsSeen -> handleNotificationsSeen()
                is PostLikeAction -> likePost(it)
                is RepostAction -> repostPost(it)
                is UiEvent.ZapAction -> zapPost(it)
            }
        }
    }

    private fun subscribeToActiveAccount() = viewModelScope.launch {
        activeAccountStore.activeUserAccount.collect {
            setState {
                copy(
                    activeAccountAvatarUrl = it.pictureUrl,
                    walletConnected = it.nostrWallet != null,
                    defaultZapAmount = it.appSettings?.defaultZapAmount,
                    zapOptions = it.appSettings?.zapOptions ?: emptyList(),
                )
            }
        }
    }

    private fun subscribeToBadgesUpdates() = viewModelScope.launch {
        badgesManager.badges.collect {
            setState { copy(badges = it) }
        }
    }

    private fun observeUnseenNotifications() = viewModelScope.launch {
        notificationsRepository.observeUnseenNotifications().collect { newUnseenNotifications ->
            setState {
                val unseenNotifications = mutableListOf<List<Notification>>()
                val groupByType = newUnseenNotifications.groupBy { it.data.type }
                groupByType.keys.forEach { notificationType ->
                    groupByType[notificationType]?.let { notificationsByType ->
                        when (notificationType.collapsable) {
                            true -> {
                                val groupByPostId = notificationsByType.groupBy { it.data.actionPostId }
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
                    }
                )
            }
        }
    }

    private fun handleNotificationsSeen() = viewModelScope.launch {
        notificationsRepository.markAllNotificationsAsSeen()
    }

    private fun likePost(postLikeAction: PostLikeAction) = viewModelScope.launch {
        try {
            postRepository.likePost(
                postId = postLikeAction.postId,
                postAuthorId = postLikeAction.postAuthorId,
            )
        } catch (error: NostrPublishException) {
            setErrorState(error = FailedToPublishLikeEvent(error))
        } catch (error: MissingRelaysException) {
            setErrorState(error = MissingRelaysConfiguration(error))
        }
    }

    private fun repostPost(repostAction: RepostAction) = viewModelScope.launch {
        try {
            postRepository.repostPost(
                postId = repostAction.postId,
                postAuthorId = repostAction.postAuthorId,
                postRawNostrEvent = repostAction.postNostrEvent,
            )
        } catch (error: NostrPublishException) {
            setErrorState(error = FailedToPublishRepostEvent(error))
        } catch (error: MissingRelaysException) {
            setErrorState(error = MissingRelaysConfiguration(error))
        }
    }

    private fun zapPost(zapAction: UiEvent.ZapAction) = viewModelScope.launch {
        if (zapAction.postAuthorLightningAddress == null) {
            setErrorState(error = MissingLightningAddress(IllegalStateException()))
            return@launch
        }

        try {
            zapRepository.zap(
                userId = activeAccountStore.activeUserId(),
                comment = zapAction.zapDescription,
                amountInSats = zapAction.zapAmount,
                target = ZapTarget.Note(
                    zapAction.postId,
                    zapAction.postAuthorId,
                    zapAction.postAuthorLightningAddress
                ),
            )
        } catch (error: ZapRepository.ZapFailureException) {
            setErrorState(error = FailedToPublishZapEvent(error))
        } catch (error: NostrPublishException) {
            setErrorState(error = FailedToPublishZapEvent(error))
        } catch (error: MissingRelaysException) {
            setErrorState(error = MissingRelaysConfiguration(error))
        } catch (error: ZapRepository.InvalidZapRequestException) {
            setErrorState(error = InvalidZapRequest(error))
        }
    }

    private fun setErrorState(error: UiState.NotificationsError) {
        setState { copy(error = error) }
        viewModelScope.launch {
            delay(2.seconds)
            if (state.value.error == error) {
                setState { copy(error = null) }
            }
        }
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
            authorName = this.actionByUser?.authorNameUiFriendly()
                ?: this.actionPost.authorId.asEllipsizedNpub(),
            authorHandle = this.actionByUser?.usernameUiFriendly()
                ?: this.actionPost.authorId.asEllipsizedNpub(),
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
