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
import net.primal.android.core.serialization.json.NostrJsonEncodeDefaults
import net.primal.android.messages.domain.MessagesUnreadCount
import net.primal.android.networking.di.PrimalCacheApiClient
import net.primal.android.networking.di.PrimalWalletApiClient
import net.primal.android.nostr.ext.asMessagesTotalCount
import net.primal.android.nostr.ext.asNotificationSummary
import net.primal.android.nostr.ext.takeContentOrNull
import net.primal.android.nostr.notary.NostrNotary
import net.primal.android.notifications.domain.NotificationsSummary
import net.primal.android.user.accounts.active.ActiveAccountStore
import net.primal.android.user.domain.Badges
import net.primal.core.networking.primal.PrimalApiClient
import net.primal.core.networking.primal.PrimalCacheFilter
import net.primal.core.networking.primal.PrimalSocketSubscription
import net.primal.core.utils.coroutines.DispatcherProvider
import net.primal.core.utils.serialization.encodeToJsonString
import net.primal.data.remote.api.notifications.model.PubkeyRequestBody
import net.primal.domain.common.PrimalEvent
import net.primal.domain.nostr.NostrEventKind
import net.primal.domain.nostr.cryptography.utils.unwrapOrThrow
import net.primal.domain.streams.StreamRepository
import net.primal.domain.wallet.SubWallet
import net.primal.domain.wallet.WalletRepository
import net.primal.wallet.data.remote.model.BalanceRequestBody
import net.primal.wallet.data.remote.model.BalanceResponse
import net.primal.wallet.data.remote.model.WalletRequestBody

@Singleton
class SubscriptionsManager @Inject constructor(
    dispatcherProvider: DispatcherProvider,
    private val activeAccountStore: ActiveAccountStore,
    private val walletRepository: WalletRepository,
    private val streamRepository: StreamRepository,
    private val nostrNotary: NostrNotary,
    @PrimalCacheApiClient private val cacheApiClient: PrimalApiClient,
    @PrimalWalletApiClient private val walletApiClient: PrimalApiClient,
) {

    private val lifecycle: Lifecycle = ProcessLifecycleOwner.get().lifecycle
    private val scope = CoroutineScope(dispatcherProvider.io() + SupervisorJob())
    private var subscriptionsActive = false

    private var streamsFromFollowsSubscription: Job? = null
    private var notificationsSummarySubscription: PrimalSocketSubscription<NotificationsSummary>? = null
    private var messagesUnreadCountSubscription: PrimalSocketSubscription<MessagesUnreadCount>? = null
    private var walletBalanceSubscription: PrimalSocketSubscription<PrimalEvent>? = null

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

    private suspend fun subscribeAll(userId: String) {
        subscriptionsActive = true
        streamsFromFollowsSubscription = launchStreamsFromFollowsSubscription(userId = userId)
        notificationsSummarySubscription = launchNotificationsSummarySubscription(userId = userId)
        messagesUnreadCountSubscription = launchMessagesUnreadCountSubscription(userId = userId)
        walletBalanceSubscription = launchWalletMonitorSubscription(userId = userId)
    }

    private suspend fun unsubscribeAll() {
        subscriptionsActive = false
        streamsFromFollowsSubscription?.cancel()
        notificationsSummarySubscription?.unsubscribe()
        messagesUnreadCountSubscription?.unsubscribe()
        walletBalanceSubscription?.unsubscribe()
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

    private suspend fun launchWalletMonitorSubscription(userId: String) =
        runCatching {
            PrimalSocketSubscription.launch(
                scope = scope,
                primalApiClient = walletApiClient,
                cacheFilter = PrimalCacheFilter(
                    primalVerb = net.primal.data.remote.PrimalVerb.WALLET_MONITOR.id,
                    optionsJson = NostrJsonEncodeDefaults.encodeToString(
                        WalletRequestBody(
                            event = nostrNotary.signPrimalWalletOperationNostrEvent(
                                userId = userId,
                                content = BalanceRequestBody(subWallet = SubWallet.Open).encodeToJsonString(),
                            ).unwrapOrThrow(),
                        ),
                    ),
                ),
                transformer = { this.primalEvent },
            ) { event ->
                when (event.kind) {
                    NostrEventKind.PrimalWalletBalance.value -> {
                        event.takeContentOrNull<BalanceResponse>()?.let { response ->
                            walletRepository.updateWalletBalance(
                                walletId = userId,
                                balanceInBtc = response.amount.toDouble(),
                                maxBalanceInBtc = response.maxAmount?.toDouble(),
                            )
                        }
                    }
                }
            }
        }.getOrNull()
}
