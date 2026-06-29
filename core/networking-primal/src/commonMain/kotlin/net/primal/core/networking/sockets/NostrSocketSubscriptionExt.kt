package net.primal.core.networking.sockets

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onStart
import kotlinx.serialization.json.JsonObject
import net.primal.core.utils.runCatching

/**
 * A long-lived subscription that survives socket rebuilds: [connectionGeneration] re-emits after
 * each reconnect, so [flatMapLatest] drops the stale collection and re-sends the REQ. The trailing
 * [onStart] connects before the first generation is collected, so the initial REQ fires once.
 */
@OptIn(ExperimentalCoroutinesApi::class)
fun NostrSocketClient.subscription(subscriptionId: String, data: JsonObject): Flow<NostrIncomingMessage> =
    connectionGeneration
        .flatMapLatest {
            incomingMessages
                .filterBySubscriptionId(id = subscriptionId)
                .onStart { sendREQ(subscriptionId = subscriptionId, data = data) }
        }
        .onStart { ensureSocketConnectionOrThrow() }
        .onCompletion { runCatching { sendCLOSE(subscriptionId = subscriptionId) } }
