package net.primal.android.stream.subscription

import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import net.primal.android.networking.di.PrimalCacheApiClient
import net.primal.core.networking.primal.PrimalApiClient
import net.primal.core.networking.primal.PrimalCacheFilter
import net.primal.core.networking.primal.PrimalSocketSubscription
import net.primal.core.utils.serialization.encodeToJsonString
import net.primal.domain.nostr.NostrEvent
import net.primal.domain.nostr.NostrEventKind

class LiveFeedMonitor @Inject constructor(
    @PrimalCacheApiClient private val primalApiClient: PrimalApiClient,
) {
    private var monitorSubscription: PrimalSocketSubscription<NostrEvent>? = null
    private val monitorMutex = Mutex()

    fun startMonitor(
        scope: CoroutineScope,
        creatorPubkey: String,
        dTag: String,
        userPubkey: String,
        onUpdate: (NostrEvent) -> Unit,
    ) {
        scope.launch {
            monitorMutex.withLock {
                if (monitorSubscription == null) {
                    monitorSubscription = subscribeToLiveFeed(
                        scope = scope,
                        creatorPubkey = creatorPubkey,
                        dTag = dTag,
                        userPubkey = userPubkey,
                        onUpdate = onUpdate,
                    )
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

    private fun subscribeToLiveFeed(
        scope: CoroutineScope,
        creatorPubkey: String,
        dTag: String,
        userPubkey: String,
        onUpdate: (NostrEvent) -> Unit,
    ) = PrimalSocketSubscription.launch(
        scope = scope,
        primalApiClient = primalApiClient,
        cacheFilter = PrimalCacheFilter(
            primalVerb = "live_feed",
            optionsJson = LiveFeedRequestBody(
                kind = NostrEventKind.LiveActivity.value,
                pubkey = creatorPubkey,
                identifier = dTag,
                userPubkey = userPubkey,
            ).encodeToJsonString(),
        ),
        transformer = {
            if (this.nostrEvent?.kind == NostrEventKind.Zap.value) {
                this.nostrEvent
            } else {
                null
            }
        },
    ) { event ->
        onUpdate(event)
    }
}
