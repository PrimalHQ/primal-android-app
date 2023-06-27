package net.primal.android.networking.sockets

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.flow.transformWhile
import kotlinx.coroutines.launch
import kotlinx.serialization.decodeFromString
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

    private lateinit var webSocket: WebSocket

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

        override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
            // We need to implement and start auto-reconnect process here
            Timber.e("WS connection closed. code=$code, reason=$reason")
        }
    }

    private val mutableMessagesSharedFlow = MutableSharedFlow<IncomingMessage>()
    private val messagesSharedFlow = mutableMessagesSharedFlow.asSharedFlow()

    init {
        connect()
    }

    private fun connect() {
        webSocket = okHttpClient.newWebSocket(
            request = primalApiRequest,
            listener = socketListener
        )
    }

    @Throws(NostrNoticeException::class)
    suspend fun query(message: OutgoingMessage): SocketQueryResult {
        val subscriptionId = sendRequest(message)
        return collectRequestResultBySubscriptionId(id = subscriptionId)
    }

    private fun sendRequest(message: OutgoingMessage): UUID {
        val subscriptionId = UUID.randomUUID()
        val finalMessage = buildOutgoingMessage(
            verb = NostrVerb.Outgoing.REQ,
            subscriptionId = subscriptionId,
            primalVerb = message.primalVerb,
            optionsJson = message.optionsJson,
        )
        Timber.i("--> $finalMessage")
        val success = webSocket.send(finalMessage)
        if (success) {
            Timber.i("Socket message was sent successfully.")
        } else {
            Timber.w("Socket message was not sent.")
        }
        return subscriptionId
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

}
