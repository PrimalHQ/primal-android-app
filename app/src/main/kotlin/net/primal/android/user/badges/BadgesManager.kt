package net.primal.android.user.badges

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.getAndUpdate
import kotlinx.coroutines.flow.transform
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import net.primal.android.networking.primal.PrimalApiClient
import net.primal.android.networking.primal.PrimalCacheFilter
import net.primal.android.networking.primal.PrimalVerb
import net.primal.android.networking.sockets.NostrIncomingMessage
import net.primal.android.networking.sockets.errors.NostrNoticeException
import net.primal.android.nostr.ext.asNotificationSummary
import net.primal.android.notifications.api.model.PubkeyRequestBody
import net.primal.android.serialization.NostrJson
import net.primal.android.user.accounts.active.ActiveAccountStore
import net.primal.android.user.accounts.active.ActiveUserAccountState
import net.primal.android.user.domain.Badges
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BadgesManager @Inject constructor(
    private val activeAccountStore: ActiveAccountStore,
    private val primalApiClient: PrimalApiClient,
) : LifecycleEventObserver {

    private val scope = CoroutineScope(Dispatchers.IO)
    private var subscriptionsActive = false
    private var activeUserId: String? = null

    private var notificationsSummarySubscriptionId: UUID? = null
    private var notificationsSummarySubscriptionJob: Job? = null

    private val mutableBadges = MutableStateFlow(Badges())
    val badges = mutableBadges.asStateFlow()

    init {
        observeActiveAccount()
    }

    private fun observeActiveAccount() = scope.launch {
        activeAccountStore.activeAccountState.collect {
            when (it) {
                is ActiveUserAccountState.ActiveUserAccount -> {
                    val newActiveUserId = it.data.pubkey
                    if (newActiveUserId != activeUserId) {
                        activeUserId = newActiveUserId
                        subscribeAll(userId = newActiveUserId)
                        withContext(Dispatchers.Main) {
                            ProcessLifecycleOwner.get().lifecycle.addObserver(this@BadgesManager)
                        }
                    }
                }

                ActiveUserAccountState.NoUserAccount -> {
                    mutableBadges.update { Badges() }
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
            Lifecycle.Event.ON_RESUME -> resumeSubscriptions()
            Lifecycle.Event.ON_PAUSE -> pauseSubscriptions()
            else -> Unit
        }
    }

    private fun resumeSubscriptions() {
        if (!subscriptionsActive) {
            activeUserId?.let {
                subscribeAll(userId = it)
            }
        }
    }

    private fun pauseSubscriptions() = unsubscribeAll()

    private fun subscribeAll(userId: String) {
        unsubscribeAll()
        subscriptionsActive = true
        notificationsSummarySubscriptionJob = scope.launch {
            subscribeNotifications(userId = userId)
        }
    }

    private fun unsubscribeAll() {
        subscriptionsActive = false
        scope.launch {
            unsubscribeNotifications()
        }
    }

    private suspend fun subscribeNotifications(userId: String) {
        val newSubscriptionId = UUID.randomUUID()
        notificationsSummarySubscriptionId = newSubscriptionId
        primalApiClient.subscribe(
            subscriptionId = newSubscriptionId,
            message = PrimalCacheFilter(
                primalVerb = PrimalVerb.NEW_NOTIFICATIONS_COUNT,
                optionsJson = NostrJson.encodeToString(PubkeyRequestBody(pubkey = userId))
            )
        ).transform { it ->
            when (it) {
                is NostrIncomingMessage.EventMessage -> {
                    it.primalEvent?.asNotificationSummary()?.let { summary -> emit(summary) }
                }

                is NostrIncomingMessage.NoticeMessage -> {
                    throw NostrNoticeException(reason = it.message)
                }

                else -> Unit
            }
        }.collect {
            mutableBadges.getAndUpdate { currentState ->
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
}
