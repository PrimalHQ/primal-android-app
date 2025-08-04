package net.primal.data.remote.api.stream

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import net.primal.core.networking.primal.PrimalApiClient
import net.primal.core.networking.primal.PrimalCacheFilter
import net.primal.core.networking.primal.PrimalSocketSubscription
import net.primal.core.utils.serialization.encodeToJsonString
import net.primal.data.remote.PrimalVerb
import net.primal.data.remote.api.stream.model.LiveFeedRequestBody
import net.primal.domain.nostr.NostrEvent
import net.primal.domain.nostr.NostrEventKind

class StreamMonitorImpl(
    private val primalApiClient: PrimalApiClient,
) : StreamMonitor {
    private var monitorSubscription: PrimalSocketSubscription<NostrEvent>? = null
    private val monitorMutex = Mutex()

    override fun start(
        scope: CoroutineScope,
        creatorPubkey: String,
        dTag: String,
        userPubkey: String,
        onZapEvent: (NostrEvent) -> Unit,
    ) {
        scope.launch {
            monitorMutex.withLock {
                if (monitorSubscription == null) {
                    monitorSubscription = subscribeToLiveFeed(
                        scope = scope,
                        creatorPubkey = creatorPubkey,
                        dTag = dTag,
                        userPubkey = userPubkey,
                        onUpdate = onZapEvent,
                    )
                }
            }
        }
    }

    override fun stop(scope: CoroutineScope) {
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
            primalVerb = PrimalVerb.LIVE_FEED.id,
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
