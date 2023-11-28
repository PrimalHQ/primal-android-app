package net.primal.android.networking.sockets

import java.util.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.json.JsonObject
import net.primal.android.core.coroutines.CoroutineDispatcherProvider
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import timber.log.Timber

class NostrSocketClient constructor(
    dispatcherProvider: CoroutineDispatcherProvider,
    private val okHttpClient: OkHttpClient,
    private val wssRequest: Request,
    private val onSocketConnectionOpen: (() -> Unit)? = null,
    private val onSocketConnectionFailure: ((Throwable) -> Unit)? = null,
) {
    private val scope = CoroutineScope(dispatcherProvider.io())

    private val webSocketMutex = Mutex()

    private var webSocket: WebSocket? = null

    private val socketListener = object : WebSocketListener() {
        override fun onOpen(webSocket: WebSocket, response: Response) {
            onSocketConnectionOpen?.invoke()
        }

        override fun onMessage(webSocket: WebSocket, text: String) {
            Timber.d("<-- $text")
            println(text)
            text.parseIncomingMessage()?.let {
                scope.launch {
                    mutableIncomingMessagesSharedFlow.emit(value = it)
                }
            }
        }

        override fun onFailure(
            webSocket: WebSocket,
            t: Throwable,
            response: Response?,
        ) {
            Timber.w("WS connection failure.")
            Timber.w(t)
            this@NostrSocketClient.webSocket = null
            onSocketConnectionFailure?.invoke(t)
        }

        override fun onClosed(
            webSocket: WebSocket,
            code: Int,
            reason: String,
        ) {
            Timber.w("WS connection closed with code=$code and reason=$reason")
            this@NostrSocketClient.webSocket = null
        }
    }

    private val mutableIncomingMessagesSharedFlow = MutableSharedFlow<NostrIncomingMessage>()

    val incomingMessages = mutableIncomingMessagesSharedFlow.asSharedFlow()

    suspend fun ensureSocketConnection() =
        webSocketMutex.withLock {
            if (webSocket == null) {
                webSocket = okHttpClient.newWebSocket(
                    request = wssRequest,
                    listener = socketListener,
                )
            }
        }

    fun close() {
        webSocket?.close(code = 1000, reason = "Closed by client.")
    }

    private fun sendMessage(text: String): Boolean {
        Timber.i("--> $text")
        println(text)
        return webSocket?.send(text) == true
    }

    fun sendREQ(subscriptionId: UUID, data: JsonObject): Boolean {
        val reqMessage = data.buildNostrREQMessage(subscriptionId)
        return sendMessage(text = reqMessage)
    }

    fun sendREQ(data: JsonObject): UUID? {
        val subscriptionId: UUID = UUID.randomUUID()
        val success = sendREQ(data = data, subscriptionId = subscriptionId)
        return if (success) subscriptionId else null
    }

    fun sendCOUNT(data: JsonObject): UUID? {
        val subscriptionId: UUID = UUID.randomUUID()
        val reqMessage = data.buildNostrCOUNTMessage(subscriptionId)
        val success = sendMessage(text = reqMessage)
        return if (success) subscriptionId else null
    }

    fun sendCLOSE(subscriptionId: UUID): Boolean {
        return sendMessage(text = subscriptionId.buildNostrCLOSEMessage())
    }

    fun sendEVENT(signedEvent: JsonObject): Boolean {
        return sendMessage(text = signedEvent.buildNostrEVENTMessage())
    }

    fun sendAUTH(signedEvent: JsonObject): Boolean {
        return sendMessage(text = signedEvent.buildNostrAUTHMessage())
    }
}
