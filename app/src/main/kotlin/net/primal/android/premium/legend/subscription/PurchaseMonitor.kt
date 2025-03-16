package net.primal.android.premium.legend.subscription

import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import net.primal.android.core.serialization.json.NostrJson
import net.primal.android.networking.di.PrimalWalletApiClient
import net.primal.android.networking.primal.PrimalApiClient
import net.primal.android.networking.primal.PrimalCacheFilter
import net.primal.android.networking.primal.PrimalSocketSubscription
import net.primal.android.networking.primal.PrimalVerb
import net.primal.android.nostr.ext.takeContentOrNull
import net.primal.android.premium.api.model.MembershipPurchaseMonitorRequestBody
import net.primal.android.premium.api.model.MembershipPurchaseMonitorResponse
import net.primal.domain.nostr.NostrEventKind

class PurchaseMonitor @Inject constructor(
    @PrimalWalletApiClient private val walletApiClient: PrimalApiClient,
) {
    private var monitorSubscription: PrimalSocketSubscription<MembershipPurchaseMonitorResponse>? = null
    private val monitorMutex = Mutex()

    fun startMonitor(
        scope: CoroutineScope,
        quoteId: String,
        onComplete: () -> Unit,
    ) {
        scope.launch {
            monitorMutex.withLock {
                if (monitorSubscription == null) {
                    monitorSubscription = subscribeToPurchaseMonitor(scope, quoteId, onComplete)
                }
            }
        }
    }

    fun stopMonitor(scope: CoroutineScope) {
        scope.launch {
            monitorMutex.withLock {
                monitorSubscription?.unsubscribe()
                monitorSubscription = null
            }
        }
    }

    private fun subscribeToPurchaseMonitor(
        scope: CoroutineScope,
        quoteId: String,
        onComplete: () -> Unit,
    ) = PrimalSocketSubscription.launch(
        scope = scope,
        primalApiClient = walletApiClient,
        cacheFilter = PrimalCacheFilter(
            primalVerb = PrimalVerb.WALLET_MEMBERSHIP_PURCHASE_MONITOR,
            optionsJson = NostrJson.encodeToString(
                MembershipPurchaseMonitorRequestBody(membershipQuoteId = quoteId),
            ),
        ),
        transformer = {
            if (primalEvent?.kind == NostrEventKind.PrimalMembershipPurchaseMonitor.value) {
                primalEvent.takeContentOrNull<MembershipPurchaseMonitorResponse>()
            } else {
                null
            }
        },
    ) {
        if (it.completedAt != null) {
            onComplete()
            stopMonitor(scope)
        }
    }
}
