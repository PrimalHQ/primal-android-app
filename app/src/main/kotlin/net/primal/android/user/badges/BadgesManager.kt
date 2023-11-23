package net.primal.android.user.badges

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.transform
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import net.primal.android.core.serialization.json.NostrJson
import net.primal.android.networking.di.PrimalCacheApiClient
import net.primal.android.networking.primal.PrimalApiClient
import net.primal.android.networking.primal.PrimalCacheFilter
import net.primal.android.networking.primal.PrimalVerb
import net.primal.android.networking.sockets.NostrIncomingMessage
import net.primal.android.nostr.ext.asMessagesTotalCount
import net.primal.android.nostr.ext.asNotificationSummary
import net.primal.android.notifications.api.model.PubkeyRequestBody
import net.primal.android.user.accounts.active.ActiveAccountStore
import net.primal.android.user.accounts.active.ActiveUserAccountState
import net.primal.android.user.domain.Badges

@Singleton
class BadgesManager @Inject constructor(
    private val activeAccountStore: ActiveAccountStore,
    @PrimalCacheApiClient private val primalApiClient: PrimalApiClient,
) : LifecycleEventObserver {

    private val scope = CoroutineScope(Dispatchers.IO)
    private var subscriptionsActive = false
    private var activeUserId: String? = null

    private var notificationsSummarySubscriptionId: UUID? = null
    private var notificationsSummarySubscriptionJob: Job? = null

    private var messagesSummarySubscriptionId: UUID? = null
    private var messagesSummarySubscriptionJob: Job? = null

    private var latestBadge: Badges = Badges()
    private val mutableBadges = MutableSharedFlow<Badges>(
        replay = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST,
    )
    val badges = mutableBadges.asSharedFlow().distinctUntilChanged()

    init {
        observeActiveAccount()
    }

    private suspend fun emitBadgesUpdate(updateReducer: (Badges) -> Badges) {
        val updatedBadges = updateReducer(latestBadge)
        latestBadge = updatedBadges
        mutableBadges.emit(updatedBadges)
    }

    private fun observeActiveAccount() =
        scope.launch {
            activeAccountStore.activeAccountState.collect {
                when (it) {
                    is ActiveUserAccountState.ActiveUserAccount -> {
                        val newActiveUserId = it.data.pubkey
                        if (newActiveUserId != activeUserId) {
                            activeUserId = newActiveUserId
                            subscribeAll(userId = newActiveUserId)
                            withContext(Dispatchers.Main) {
                                ProcessLifecycleOwner.get().lifecycle.addObserver(
                                    this@BadgesManager,
                                )
                            }
                        }
                    }

                    ActiveUserAccountState.NoUserAccount -> {
                        emitBadgesUpdate { Badges() }
                        unsubscribeAll()
                        withContext(Dispatchers.Main) {
                            ProcessLifecycleOwner.get().lifecycle.removeObserver(this@BadgesManager)
                        }
                    }
                }
            }
        }

    override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
        when (event) {
            Lifecycle.Event.ON_RESUME -> scope.launch { resumeSubscriptions() }
            Lifecycle.Event.ON_PAUSE -> scope.launch { pauseSubscriptions() }
            else -> Unit
        }
    }

    private suspend fun resumeSubscriptions() {
        if (!subscriptionsActive) {
            activeUserId?.let {
                subscribeAll(userId = it)
            }
        }
    }

    private suspend fun pauseSubscriptions() = unsubscribeAll()

    private suspend fun subscribeAll(userId: String) {
        unsubscribeAll()
        subscriptionsActive = true
        notificationsSummarySubscriptionJob = scope.launch {
            subscribeNotifications(userId = userId)
        }
        messagesSummarySubscriptionJob = scope.launch {
            subscribeMessages(userId = userId)
        }
    }

    private suspend fun unsubscribeAll() {
        subscriptionsActive = false
        unsubscribeNotifications()
        unsubscribeMessages()
    }

    private suspend fun subscribeNotifications(userId: String) {
        val newSubscriptionId = UUID.randomUUID()
        notificationsSummarySubscriptionId = newSubscriptionId
        primalApiClient.subscribe(
            subscriptionId = newSubscriptionId,
            message = PrimalCacheFilter(
                primalVerb = PrimalVerb.NEW_NOTIFICATIONS_COUNT,
                optionsJson = NostrJson.encodeToString(PubkeyRequestBody(pubkey = userId)),
            ),
        ).transform { it ->
            when (it) {
                is NostrIncomingMessage.EventMessage -> {
                    it.primalEvent?.asNotificationSummary()?.let { summary -> emit(summary) }
                }
                else -> Unit
            }
        }.collect {
            emitBadgesUpdate { currentState ->
                currentState.copy(notifications = it.count)
            }
        }
    }

    private suspend fun unsubscribeNotifications() {
        notificationsSummarySubscriptionId?.let {
            primalApiClient.closeSubscription(it)
            notificationsSummarySubscriptionId = null
            notificationsSummarySubscriptionJob?.cancel()
            notificationsSummarySubscriptionJob = null
        }
    }

    private suspend fun subscribeMessages(userId: String) {
        val newSubscriptionId = UUID.randomUUID()
        messagesSummarySubscriptionId = newSubscriptionId
        primalApiClient.subscribe(
            subscriptionId = newSubscriptionId,
            message = PrimalCacheFilter(
                primalVerb = PrimalVerb.NEW_DMS_COUNT,
                optionsJson = NostrJson.encodeToString(PubkeyRequestBody(pubkey = userId)),
            ),
        ).transform { it ->
            when (it) {
                is NostrIncomingMessage.EventMessage -> {
                    it.primalEvent?.asMessagesTotalCount()?.let { summary -> emit(summary) }
                }
                else -> Unit
            }
        }.collect {
            emitBadgesUpdate { currentState ->
                currentState.copy(messages = it.count)
            }
        }
    }

    private suspend fun unsubscribeMessages() {
        messagesSummarySubscriptionId?.let {
            primalApiClient.closeSubscription(it)
            messagesSummarySubscriptionId = null
            messagesSummarySubscriptionJob?.cancel()
            messagesSummarySubscriptionJob = null
        }
    }
}
