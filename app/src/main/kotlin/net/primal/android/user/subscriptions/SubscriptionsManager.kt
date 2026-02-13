package net.primal.android.user.subscriptions

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.lifecycle.flowWithLifecycle
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import net.primal.android.messages.domain.MessagesUnreadCount
import net.primal.android.networking.di.PrimalCacheApiClient
import net.primal.android.nostr.ext.asMessagesTotalCount
import net.primal.android.nostr.ext.asNotificationSummary
import net.primal.android.notifications.domain.NotificationsSummary
import net.primal.android.user.accounts.active.ActiveAccountStore
import net.primal.android.user.domain.Badges
import net.primal.android.wallet.di.ActiveWalletBalanceSyncerFactory
import net.primal.core.networking.primal.PrimalApiClient
import net.primal.core.networking.primal.PrimalCacheFilter
import net.primal.core.networking.primal.PrimalSocketSubscription
import net.primal.core.utils.coroutines.DispatcherProvider
import net.primal.core.utils.serialization.encodeToJsonString
import net.primal.data.remote.api.notifications.model.PubkeyRequestBody
import net.primal.domain.streams.StreamRepository
import net.primal.domain.wallet.sync.WalletDataSyncer

@Singleton
class SubscriptionsManager @Inject constructor(
    dispatcherProvider: DispatcherProvider,
    private val activeAccountStore: ActiveAccountStore,
    private val streamRepository: StreamRepository,
    @PrimalCacheApiClient private val cacheApiClient: PrimalApiClient,
    private val activeWalletBalanceSyncerFactory: ActiveWalletBalanceSyncerFactory,
) {

    private val lifecycle: Lifecycle = ProcessLifecycleOwner.get().lifecycle
    private val scope = CoroutineScope(dispatcherProvider.io() + SupervisorJob())
    private var subscriptionsActive = false

    private var streamsFromFollowsSubscription: Job? = null
    private var notificationsSummarySubscription: PrimalSocketSubscription<NotificationsSummary>? = null
    private var messagesUnreadCountSubscription: PrimalSocketSubscription<MessagesUnreadCount>? = null

    private var activeWalletBalanceSyncer: WalletDataSyncer? = null

    private val _badges = MutableSharedFlow<Badges>(
        replay = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST,
    )
    val badges = _badges.asSharedFlow().distinctUntilChanged()

    init {
        observeActiveAccount()
    }

    private var latestBadge: Badges = Badges()

    private suspend fun emitBadgesUpdate(updateReducer: (Badges) -> Badges) {
        val updatedBadges = updateReducer(latestBadge)
        latestBadge = updatedBadges
        _badges.emit(updatedBadges)
    }

    private fun observeActiveAccount() =
        scope.launch {
            activeAccountStore.activeUserId
                .flowWithLifecycle(lifecycle = lifecycle, minActiveState = Lifecycle.State.STARTED)
                .collect { newActiveUserId ->
                    emitBadgesUpdate { Badges() }
                    unsubscribeAll()
                    when {
                        newActiveUserId.isEmpty() -> {
                            withContext(Dispatchers.Main) {
                                lifecycle.removeObserver(lifecycleEventObserver)
                            }
                        }

                        else -> {
                            subscribeAll(userId = newActiveUserId)
                            withContext(Dispatchers.Main) {
                                lifecycle.addObserver(lifecycleEventObserver)
                            }
                        }
                    }
                }
        }

    private val lifecycleEventObserver = LifecycleEventObserver { _, event ->
        when (event) {
            Lifecycle.Event.ON_RESUME -> scope.launch { resumeSubscriptions() }
            Lifecycle.Event.ON_PAUSE -> scope.launch { pauseSubscriptions() }
            else -> Unit
        }
    }

    private suspend fun resumeSubscriptions() {
        if (!subscriptionsActive) {
            unsubscribeAll()
            subscribeAll(userId = activeAccountStore.activeUserId())
        }
    }

    private suspend fun pauseSubscriptions() = unsubscribeAll()

    private fun subscribeAll(userId: String) {
        subscriptionsActive = true
        streamsFromFollowsSubscription = launchStreamsFromFollowsSubscription(userId = userId)
        notificationsSummarySubscription = launchNotificationsSummarySubscription(userId = userId)
        messagesUnreadCountSubscription = launchMessagesUnreadCountSubscription(userId = userId)
        activeWalletBalanceSyncer = activeWalletBalanceSyncerFactory.create(userId = userId).also { it.start() }
    }

    private suspend fun unsubscribeAll() {
        subscriptionsActive = false
        streamsFromFollowsSubscription?.cancel()
        notificationsSummarySubscription?.unsubscribe()
        messagesUnreadCountSubscription?.unsubscribe()
        activeWalletBalanceSyncer?.stop()
    }

    private fun launchStreamsFromFollowsSubscription(userId: String) =
        scope.launch {
            streamRepository.startLiveEventsFromFollowsSubscription(userId = userId)
        }

    private fun launchNotificationsSummarySubscription(userId: String) =
        PrimalSocketSubscription.launch(
            scope = scope,
            primalApiClient = cacheApiClient,
            cacheFilter = PrimalCacheFilter(
                primalVerb = net.primal.data.remote.PrimalVerb.NEW_NOTIFICATIONS_COUNT.id,
                optionsJson = PubkeyRequestBody(pubkey = userId).encodeToJsonString(),
            ),
            transformer = { primalEvent?.asNotificationSummary() },
        ) {
            emitBadgesUpdate { currentState ->
                currentState.copy(unreadNotificationsCount = it.count)
            }
        }

    private fun launchMessagesUnreadCountSubscription(userId: String) =
        PrimalSocketSubscription.launch(
            scope = scope,
            primalApiClient = cacheApiClient,
            cacheFilter = PrimalCacheFilter(
                primalVerb = net.primal.data.remote.PrimalVerb.NEW_DMS_COUNT.id,
                optionsJson = PubkeyRequestBody(pubkey = userId).encodeToJsonString(),
            ),
            transformer = { primalEvent?.asMessagesTotalCount() },
        ) {
            emitBadgesUpdate { currentState ->
                currentState.copy(unreadMessagesCount = it.count)
            }
        }
}
