package net.primal.android.networking.sockets

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.flow.transformWhile
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.json.add
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonPrimitive
import net.primal.android.networking.di.PrimalApiWS
import net.primal.android.networking.sockets.model.IncomingMessage
import net.primal.android.networking.sockets.model.OutgoingMessage
import net.primal.android.nostr.ext.asNostrEventOrNull
import net.primal.android.nostr.ext.asPrimalEventOrNull
import net.primal.android.nostr.ext.isNotPrimalEventKind
import net.primal.android.nostr.ext.isNotUnknown
import net.primal.android.nostr.ext.isPrimalEventKind
import net.primal.android.nostr.model.NostrEventKind
import net.primal.android.nostr.model.NostrVerb
import net.primal.android.serialization.NostrJson
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import timber.log.Timber
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SocketClient @Inject constructor(
    private val okHttpClient: OkHttpClient,
    @PrimalApiWS private val primalApiRequest: Request,
) : WebSocketListener() {

    private val scope = CoroutineScope(Dispatchers.IO)

    private val webSocketMutex = Mutex()

    private var webSocket: WebSocket? = null

    private val socketListener = object : WebSocketListener() {
        override fun onMessage(webSocket: WebSocket, text: String) {
            Timber.d("<-- $text")
            val incomingMessage = text.toIncomingMessage()
            if (incomingMessage != null) {
                scope.launch {
                    mutableMessagesSharedFlow.emit(value = incomingMessage)
                }
            }
        }

        override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
            Timber.w("WS connection failure.", t, response)
            this@SocketClient.webSocket = null
        }

        override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
            Timber.w("WS connection closed with code=$code and reason=$reason")
            this@SocketClient.webSocket = null
        }
    }

    private val mutableMessagesSharedFlow = MutableSharedFlow<IncomingMessage>()
    private val messagesSharedFlow = mutableMessagesSharedFlow.asSharedFlow()


    private suspend fun ensureSocketConnection() = webSocketMutex.withLock {
        if (webSocket == null) {
            webSocket = okHttpClient.newWebSocket(
                request = primalApiRequest,
                listener = socketListener
            )
        }
    }

    @Throws(NostrNoticeException::class)
    suspend fun query(message: OutgoingMessage): SocketQueryResult {
        var queryAttempts = 0
        var subscriptionId: UUID? = null
        while (queryAttempts < MAX_QUERY_ATTEMPTS) {
            ensureSocketConnection()
            subscriptionId = sendRequest(message)
            queryAttempts++

            if (subscriptionId != null) break

            if (queryAttempts < MAX_QUERY_ATTEMPTS) {
                delay(RETRY_DELAY_MILLIS)
            }
        }

        return if (subscriptionId != null) {
            try {
                collectRequestResultBySubscriptionId(id = subscriptionId)
            } catch (error: NostrNoticeException) {
                throw WssException(
                    message = error.reason,
                    cause = error,
                )
            }
        } else {
            throw WssException(message = "Api unavailable at the moment.")
        }
    }

    private fun sendRequest(message: OutgoingMessage): UUID? {
        val subscriptionId = UUID.randomUUID()
        val finalMessage = buildOutgoingMessage(
            verb = NostrVerb.Outgoing.REQ,
            subscriptionId = subscriptionId,
            primalVerb = message.primalVerb,
            optionsJson = message.optionsJson,
        )
        Timber.i("--> $finalMessage")
        val success = webSocket?.send(finalMessage) == true
        return if (success) subscriptionId else null
    }

    private fun buildOutgoingMessage(
        verb: NostrVerb.Outgoing,
        subscriptionId: UUID,
        primalVerb: String?,
        optionsJson: String?,
    ): String {
        return buildJsonArray {
            add(verb.toString())
            add(subscriptionId.toString())
            if (primalVerb != null) {
                add(
                    buildJsonObject {
                        put("cache", buildJsonArray {
                            add(primalVerb)
                            if (optionsJson != null) {
                                add(NostrJson.decodeFromString(optionsJson))
                            }
                        })
                    }
                )
            }
        }.toString()
    }

    @Throws(NostrNoticeException::class)
    private suspend fun collectRequestResultBySubscriptionId(id: UUID): SocketQueryResult {
        val result = messagesBySubscriptionIdWhileEventsIncoming(id).toList()
        val terminationMessage = result.last()

        if (terminationMessage.type == NostrVerb.Incoming.NOTICE) {
            val reason = terminationMessage.data?.jsonPrimitive?.content
            throw NostrNoticeException(reason = reason)
        }

        val allEvents = result.dropLast(1)

        val nostrEvents = allEvents
            .filter {
                val kind = it.getMessageNostrEventKind()
                kind.isNotUnknown() && kind.isNotPrimalEventKind()
            }
            .mapNotNull { it.data.asNostrEventOrNull() }

        val primalEvents = allEvents
            .filter { it.getMessageNostrEventKind().isPrimalEventKind() }
            .mapNotNull { it.data.asPrimalEventOrNull() }

        return SocketQueryResult(
            terminationMessage = terminationMessage,
            nostrEvents = nostrEvents,
            primalEvents = primalEvents,
        )
    }

    private fun messagesBySubscriptionIdWhileEventsIncoming(id: UUID) = messagesSharedFlow
        .filter { it.subscriptionId == id }
        .transformWhile {
            emit(it)
            it.type == NostrVerb.Incoming.EVENT
        }

    private fun IncomingMessage.getMessageNostrEventKind(): NostrEventKind {
        val kind = data?.get("kind")?.jsonPrimitive?.content?.toIntOrNull()
        return if (kind != null) NostrEventKind.valueOf(kind) else NostrEventKind.Unknown
    }


    companion object {
        private const val MAX_QUERY_ATTEMPTS = 3
        private const val RETRY_DELAY_MILLIS = 500L
    }
}
