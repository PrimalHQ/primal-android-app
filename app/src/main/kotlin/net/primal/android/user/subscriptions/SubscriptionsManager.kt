package net.primal.android.user.subscriptions

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.lifecycle.flowWithLifecycle
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
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
import net.primal.android.user.repository.UserRepository
import net.primal.android.wallet.api.model.BalanceRequestBody
import net.primal.android.wallet.api.model.BalanceResponse
import net.primal.android.wallet.api.model.LastUpdatedAtResponse
import net.primal.android.wallet.api.model.WalletRequestBody
import net.primal.android.wallet.domain.SubWallet
import net.primal.core.networking.primal.PrimalApiClient
import net.primal.core.networking.primal.PrimalCacheFilter
import net.primal.core.networking.primal.PrimalSocketSubscription
import net.primal.core.utils.coroutines.DispatcherProvider
import net.primal.core.utils.serialization.encodeToJsonString
import net.primal.data.remote.api.notifications.model.PubkeyRequestBody
import net.primal.domain.PrimalEvent
import net.primal.domain.nostr.NostrEventKind

@Singleton
class SubscriptionsManager @Inject constructor(
    dispatcherProvider: DispatcherProvider,
    private val activeAccountStore: ActiveAccountStore,
    private val userRepository: UserRepository,
    private val nostrNotary: NostrNotary,
    @PrimalCacheApiClient private val cacheApiClient: PrimalApiClient,
    @PrimalWalletApiClient private val walletApiClient: PrimalApiClient,
) {

    private val lifecycle: Lifecycle = ProcessLifecycleOwner.get().lifecycle
    private val scope = CoroutineScope(dispatcherProvider.io())
    private var subscriptionsActive = false

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

    private fun subscribeAll(userId: String) {
        subscriptionsActive = true
        notificationsSummarySubscription = launchNotificationsSummarySubscription(userId = userId)
        messagesUnreadCountSubscription = launchMessagesUnreadCountSubscription(userId = userId)
        walletBalanceSubscription = launchWalletMonitorSubscription(userId = userId)
    }

    private suspend fun unsubscribeAll() {
        subscriptionsActive = false
        notificationsSummarySubscription?.unsubscribe()
        messagesUnreadCountSubscription?.unsubscribe()
        walletBalanceSubscription?.unsubscribe()
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

    private fun launchWalletMonitorSubscription(userId: String) =
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
                            ),
                        ),
                    ),
                ),
                transformer = { this.primalEvent },
            ) { event ->
                when (event.kind) {
                    NostrEventKind.PrimalWalletBalance.value -> {
                        event.takeContentOrNull<BalanceResponse>()?.let { response ->
                            userRepository.updatePrimalWalletBalance(
                                userId = userId,
                                balanceInBtc = response.amount,
                                maxBalanceInBtc = response.maxAmount,
                            )
                        }
                    }

                    NostrEventKind.PrimalWalletUpdatedAt.value -> {
                        event.takeContentOrNull<LastUpdatedAtResponse>()?.let { response ->
                            userRepository.updatePrimalWalletLastUpdatedAt(
                                userId = userId,
                                lastUpdatedAt = response.lastUpdatedAt,
                            )
                        }
                    }
                }
            }
        }.getOrNull()
}
