package net.primal.android.networking.sockets

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.add
import kotlinx.serialization.json.buildJsonArray
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import timber.log.Timber
import java.util.UUID

class NostrSocketClient constructor(
    private val okHttpClient: OkHttpClient,
    private val wssRequest: Request,
) {

    private val scope = CoroutineScope(Dispatchers.IO)

    private val webSocketMutex = Mutex()

    private var webSocket: WebSocket? = null

    private val socketListener = object : WebSocketListener() {
        override fun onMessage(webSocket: WebSocket, text: String) {
            Timber.d("<-- $text")
            text.parseIncomingMessage()?.let {
                scope.launch {
                    mutableIncomingMessagesSharedFlow.emit(value = it)
                }
            }
        }

        override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
            Timber.w("WS connection failure.", t, response)
            this@NostrSocketClient.webSocket = null
        }

        override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
            Timber.w("WS connection closed with code=$code and reason=$reason")
            this@NostrSocketClient.webSocket = null
        }
    }

    private val mutableIncomingMessagesSharedFlow = MutableSharedFlow<NostrIncomingMessage>()

    val incomingMessages = mutableIncomingMessagesSharedFlow.asSharedFlow()
    suspend fun ensureSocketConnection() = webSocketMutex.withLock {
        if (webSocket == null) {
            webSocket = okHttpClient.newWebSocket(
                request = wssRequest,
                listener = socketListener
            )
        }
    }

    fun close() {
        webSocket?.close(code = 1000, reason = "Closed by client.")
    }

    private fun sendMessage(text: String): Boolean {
        Timber.i("--> $text")
        return webSocket?.send(text) == true
    }

    private fun sendSubscriptionMessage(
        verb: NostrVerb.Outgoing,
        data: JsonObject
    ): UUID? {
        val subscriptionId: UUID = UUID.randomUUID()
        val reqMessage = buildJsonArray {
            add(verb.toString())
            add(subscriptionId.toString())
            add(data)
        }.toString()

        val success = sendMessage(text = reqMessage)
        return if (success) subscriptionId else null
    }

    fun sendREQ(data: JsonObject): UUID? = sendSubscriptionMessage(
        verb = NostrVerb.Outgoing.REQ,
        data = data,
    )

    fun sendCOUNT(data: JsonObject): UUID? = sendSubscriptionMessage(
        verb = NostrVerb.Outgoing.COUNT,
        data = data,
    )

    fun sendCLOSE(uuid: UUID) {
        val reqMessage = buildJsonArray {
            add(NostrVerb.Outgoing.CLOSE.toString())
            add(uuid.toString())
        }.toString()
        sendMessage(text = reqMessage)
    }

    fun sendEVENT(signedEvent: JsonObject): Boolean {
        val reqMessage = buildJsonArray {
            add(NostrVerb.Outgoing.EVENT.toString())
            add(signedEvent)
        }.toString()
        return sendMessage(text = reqMessage)
    }

    fun sendAUTH(signedEvent: JsonObject): Boolean {
        val reqMessage = buildJsonArray {
            add(NostrVerb.Outgoing.AUTH.toString())
            add(signedEvent)
        }.toString()
        return sendMessage(text = reqMessage)
    }

}
