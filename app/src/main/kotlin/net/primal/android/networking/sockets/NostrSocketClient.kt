package net.primal.android.networking.sockets

import java.io.ByteArrayOutputStream
import java.util.*
import java.util.zip.Deflater
import java.util.zip.Inflater
import kotlin.time.Duration.Companion.milliseconds
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.json.JsonObject
import net.primal.android.core.coroutines.CoroutineDispatcherProvider
import net.primal.android.user.domain.cleanWebSocketUrl
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import okio.ByteString
import timber.log.Timber

class NostrSocketClient(
    dispatcherProvider: CoroutineDispatcherProvider,
    private val okHttpClient: OkHttpClient,
    private val wssRequest: Request,
    private val incomingCompressionEnabled: Boolean = false,
    private val onSocketConnectionOpened: SocketConnectionOpenedCallback? = null,
    private val onSocketConnectionClosed: SocketConnectionClosedCallback? = null,
) {

    val socketUrl = wssRequest.url.toString().cleanWebSocketUrl()
    private var webSocket: WebSocket? = null

    private val scope = CoroutineScope(dispatcherProvider.io())
    private val webSocketMutex = Mutex()

    private val socketListener = object : WebSocketListener() {
        override fun onOpen(webSocket: WebSocket, response: Response) {
            onSocketConnectionOpened?.invoke(socketUrl)
        }

        override fun onMessage(webSocket: WebSocket, text: String) {
            logLargeText(text = text, url = socketUrl, incoming = true)
            processIncomingMessage(text = text)
        }

        override fun onMessage(webSocket: WebSocket, bytes: ByteString) {
            val decompressedMessage = decompressMessage(bytes.toByteArray())
            logLargeText(text = decompressedMessage, url = socketUrl, incoming = true)
            processIncomingMessage(text = decompressedMessage)
        }

        override fun onFailure(
            webSocket: WebSocket,
            t: Throwable,
            response: Response?,
        ) {
            Timber.w("WS $socketUrl failure.", t)
            this@NostrSocketClient.webSocket = null
            onSocketConnectionClosed?.invoke(socketUrl, t)
        }

        override fun onClosing(
            webSocket: WebSocket,
            code: Int,
            reason: String,
        ) {
            Timber.w("WS $socketUrl closing with code=$code and reason=$reason")
            this@NostrSocketClient.webSocket = null
            onSocketConnectionClosed?.invoke(socketUrl, null)
        }

        override fun onClosed(
            webSocket: WebSocket,
            code: Int,
            reason: String,
        ) {
            Timber.w("WS $socketUrl closed with code=$code and reason=$reason")
            this@NostrSocketClient.webSocket = null
            onSocketConnectionClosed?.invoke(socketUrl, null)
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
                if (incomingCompressionEnabled) {
                    val id = UUID.randomUUID()
                    sendMessage("""["REQ","$id",{"cache":["set_primal_protocol",{"compression":"zlib"}]}]""")
                }
            }
        }

    fun close() {
        webSocket?.close(code = 1000, reason = "Closed by client.")
    }

    private fun processIncomingMessage(text: String) {
        text.parseIncomingMessage()?.let {
            scope.launch {
                if (it is NostrIncomingMessage.EoseMessage) {
                    delay(50.milliseconds)
                }
                mutableIncomingMessagesSharedFlow.emit(value = it)
            }
        }
    }

    private fun sendMessage(text: String): Boolean {
        logLargeText(text = text, url = socketUrl, incoming = false)
        return webSocket?.send(text) == true
    }

    fun sendREQ(subscriptionId: UUID, data: JsonObject): Boolean {
        val reqMessage = data.buildNostrREQMessage(subscriptionId)
        return sendMessage(text = reqMessage)
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

    private fun logLargeText(
        text: String,
        url: String,
        incoming: Boolean,
    ) {
        val chunks = text.chunked(size = 3_500)
        val chunksCount = chunks.size
        chunks.forEachIndexed { index, chunk ->
            val prefix = if (incoming) "<--" else "-->"
            val suffix = if (index == chunksCount - 1) "[$url]" else ""
            Timber.d("$prefix $chunk $suffix")
        }
    }

    @Suppress("unused")
    private fun compressMessage(message: String): ByteArray {
        val outputStream = ByteArrayOutputStream()
        val deflater = Deflater()
        deflater.setInput(message.toByteArray())
        deflater.finish()
        val buffer = ByteArray(size = 1_024)
        while (!deflater.finished()) {
            val count = deflater.deflate(buffer)
            outputStream.write(buffer, 0, count)
        }
        return outputStream.toByteArray()
    }

    private fun decompressMessage(compressedMessage: ByteArray): String {
        val inflater = Inflater()
        inflater.setInput(compressedMessage)
        val outputStream = ByteArrayOutputStream()
        val buffer = ByteArray(size = 1_024)
        while (!inflater.finished()) {
            val count = inflater.inflate(buffer)
            outputStream.write(buffer, 0, count)
        }
        return outputStream.toString()
    }
}
