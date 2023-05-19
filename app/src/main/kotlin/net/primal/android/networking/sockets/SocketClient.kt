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
import net.primal.android.networking.di.CachingService
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
    @CachingService private val cachingServiceRequest: Request,
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
            request = cachingServiceRequest,
            listener = socketListener
        )
    }

    fun messagesBySubscriptionId(subscriptionId: UUID) = messagesSharedFlow
        .filter { it.subscriptionId == subscriptionId }


    fun <T> sendRequest(request: OutgoingMessage<T>) {
        val message = buildOutgoingMessage(
            verb = NostrVerb.Outgoing.REQ,
            subscriptionId = request.subscriptionId,
            cacheVerb = request.command,
            options = request.options,
        )
        Timber.i("--> SOCKET $message")
        webSocket.send(message)
    }

    private fun <T> buildOutgoingMessage(
        verb: NostrVerb.Outgoing,
        subscriptionId: UUID,
        cacheVerb: String?,
        options: T?,
    ): String {
        return buildJsonArray {
            add(verb.toString())
            add(subscriptionId.toString())
            if (cacheVerb != null && options != null) {
                add(
                    buildJsonObject {
                        put("cache", buildJsonArray {
                            add(cacheVerb)
                            add(NostrJson.decodeFromString(options.toString()))
                        })
                    }
                )
            }
        }.toString()
    }

}