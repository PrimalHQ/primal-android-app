package net.primal.android.networking.sockets

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.launch
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.add
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import net.primal.android.networking.di.PrimalApiWS
import net.primal.android.networking.sockets.model.IncomingMessage
import net.primal.android.networking.sockets.model.OutgoingMessage
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
            val incomingMessage = text.toIncomingMessage()
            if (incomingMessage != null) {
                scope.launch {
                    mutableMessagesSharedFlow.emit(value = incomingMessage)
                }
            }
        }

        override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
            // We need to implement and start auto-reconnect process here
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

    fun messagesBySubscriptionId(id: UUID) = messagesSharedFlow.filter { it.subscriptionId == id }

    fun <T> sendRequest(message: OutgoingMessage<T>): UUID {
        val subscriptionId = UUID.randomUUID()
        val finalMessage = buildOutgoingMessage(
            verb = NostrVerb.Outgoing.REQ,
            subscriptionId = subscriptionId,
            primalVerb = message.primalVerb,
            options = message.options,
        )
        Timber.i("--> SOCKET $finalMessage")
        webSocket.send(finalMessage)
        return subscriptionId
    }

    private fun <T> buildOutgoingMessage(
        verb: NostrVerb.Outgoing,
        subscriptionId: UUID,
        primalVerb: String?,
        options: T?,
    ): String {
        return buildJsonArray {
            add(verb.toString())
            add(subscriptionId.toString())
            if (primalVerb != null) {
                add(
                    buildJsonObject {
                        put("cache", buildJsonArray {
                            add(primalVerb)
                            if (options != null) {
                                add(NostrJson.decodeFromString(options.toString()))
                            }
                        })
                    }
                )
            }
        }.toString()
    }

}