package net.primal.core.networking.sockets

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.onSubscription
import kotlinx.coroutines.flow.transform
import kotlinx.serialization.json.JsonObject
import net.primal.core.networking.sockets.errors.NostrNoticeException
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

/**
 * Publishes [event] and observes its OK, surviving socket rebuilds: on each reconnect the EVENT is
 * re-sent on the fresh socket, so a rebuild triggered by a concurrent send can't strand the publish.
 * The EVENT is sent only once the collector is attached, so a fast OK cannot be missed. Re-sending is
 * idempotent (relays dedup by event id). Callers apply their own timeout.
 */
@OptIn(ExperimentalCoroutinesApi::class)
fun NostrSocketClient.publishEventAndAwaitResponse(
    eventId: String,
    event: JsonObject,
): Flow<NostrIncomingMessage.OkMessage> =
    connectionGeneration
        .flatMapLatest {
            incomingMessages
                .onSubscription { sendEVENT(event) }
                .filterByEventId(id = eventId)
                .transform {
                    when (it) {
                        is NostrIncomingMessage.OkMessage -> emit(it)
                        is NostrIncomingMessage.NoticeMessage -> throw NostrNoticeException(reason = it.message)
                        else -> error("$it is not allowed")
                    }
                }
        }
        .onStart { ensureSocketConnectionOrThrow() }
