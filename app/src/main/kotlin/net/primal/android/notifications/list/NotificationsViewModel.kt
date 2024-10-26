package net.primal.android.notifications.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.cachedIn
import androidx.paging.map
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.Instant
import javax.inject.Inject
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.getAndUpdate
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import net.primal.android.core.compose.attachment.model.asNoteAttachmentUi
import net.primal.android.core.compose.feed.model.FeedPostStatsUi
import net.primal.android.core.compose.feed.model.FeedPostUi
import net.primal.android.core.compose.feed.model.asNoteNostrUriUi
import net.primal.android.core.coroutines.CoroutineDispatcherProvider
import net.primal.android.core.utils.asEllipsizedNpub
import net.primal.android.core.utils.authorNameUiFriendly
import net.primal.android.core.utils.usernameUiFriendly
import net.primal.android.networking.relays.errors.MissingRelaysException
import net.primal.android.networking.relays.errors.NostrPublishException
import net.primal.android.networking.sockets.errors.WssException
import net.primal.android.nostr.notary.NostrSignUnauthorized
import net.primal.android.note.repository.NoteRepository
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
import net.primal.android.profile.repository.ProfileRepository
import net.primal.android.user.accounts.active.ActiveAccountStore
import net.primal.android.user.subscriptions.SubscriptionsManager
import net.primal.android.wallet.domain.ZapTarget
import net.primal.android.wallet.zaps.InvalidZapRequestException
import net.primal.android.wallet.zaps.ZapFailureException
import net.primal.android.wallet.zaps.ZapHandler
import net.primal.android.wallet.zaps.hasWallet
import timber.log.Timber

@HiltViewModel
class NotificationsViewModel @Inject constructor(
    private val dispatcherProvider: CoroutineDispatcherProvider,
    private val activeAccountStore: ActiveAccountStore,
    private val notificationsRepository: NotificationRepository,
    private val noteRepository: NoteRepository,
    private val profileRepository: ProfileRepository,
    private val zapHandler: ZapHandler,
    private val subscriptionsManager: SubscriptionsManager,
) : ViewModel() {

    private val _state = MutableStateFlow(
        UiState(
            seenNotifications = notificationsRepository.observeSeenNotifications()
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
                    is PostLikeAction -> likePost(it)
                    is RepostAction -> repostPost(it)
                    is UiEvent.ZapAction -> zapPost(it)
                }
            }
        }

    private fun subscribeToActiveAccount() =
        viewModelScope.launch {
            activeAccountStore.activeUserAccount.collect {
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
                        },
                    )
                }
            }
        }

    private fun handleNotificationsSeen() {
        // Launching in a new scope to survive view model destruction
        CoroutineScope(dispatcherProvider.io()).launch {
            try {
                notificationsRepository.markAllNotificationsAsSeen()
            } catch (error: WssException) {
                Timber.w(error)
            } catch (error: NostrSignUnauthorized) {
                // If user logs out on notifications screen local account gets cleared
                // and when this is called there is no authenticated user and signing fails
                Timber.w(error)
            }
        }
    }

    private fun likePost(postLikeAction: PostLikeAction) =
        viewModelScope.launch {
            try {
                noteRepository.likePost(
                    postId = postLikeAction.postId,
                    postAuthorId = postLikeAction.postAuthorId,
                )
            } catch (error: NostrPublishException) {
                setErrorState(error = FailedToPublishLikeEvent(error))
            } catch (error: MissingRelaysException) {
                setErrorState(error = MissingRelaysConfiguration(error))
            }
        }

    private fun repostPost(repostAction: RepostAction) =
        viewModelScope.launch {
            try {
                noteRepository.repostPost(
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

    private fun zapPost(zapAction: UiEvent.ZapAction) =
        viewModelScope.launch {
            val postAuthorProfileData = withContext(Dispatchers.IO) {
                profileRepository.findProfileDataOrNull(profileId = zapAction.postAuthorId)
            }

            if (postAuthorProfileData?.lnUrlDecoded == null) {
                setErrorState(error = MissingLightningAddress(IllegalStateException()))
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
                setErrorState(error = FailedToPublishZapEvent(error))
            } catch (error: MissingRelaysException) {
                setErrorState(error = MissingRelaysConfiguration(error))
            } catch (error: InvalidZapRequestException) {
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
            notificationId = this.data.notificationId,
            ownerId = this.data.ownerId,
            notificationType = this.data.type,
            createdAt = Instant.ofEpochSecond(this.data.createdAt),
            actionUserId = this.data.actionUserId,
            actionUserDisplayName = this.actionByUser?.authorNameUiFriendly()
                ?: this.data.actionUserId?.asEllipsizedNpub(),
            actionUserInternetIdentifier = this.actionByUser?.internetIdentifier,
            actionUserAvatarCdnImage = this.actionByUser?.avatarCdnImage,
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
            authorAvatarCdnImage = this.actionByUser?.avatarCdnImage,
            timestamp = Instant.ofEpochSecond(this.actionPost.createdAt),
            content = this.actionPost.content,
            attachments = this.actionPostNoteAttachments.map { it.asNoteAttachmentUi() },
            nostrUris = this.actionPostNostrUris.map { it.asNoteNostrUriUi() },
            stats = FeedPostStatsUi.from(
                noteStats = this.actionNoteStats,
                userStats = this.actionPostUserStats,
            ),
            hashtags = this.actionPost.hashtags,
            rawNostrEventJson = this.actionPost.raw,
            replyToAuthorHandle = "alex",
        )
    }
}
