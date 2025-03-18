package net.primal.networking.sockets

import io.github.aakira.napier.Napier
import io.ktor.client.HttpClient
import io.ktor.client.plugins.websocket.DefaultClientWebSocketSession
import io.ktor.client.plugins.websocket.webSocket
import io.ktor.client.request.url
import io.ktor.websocket.CloseReason
import io.ktor.websocket.Frame
import io.ktor.websocket.WebSocketSession
import io.ktor.websocket.close
import io.ktor.websocket.readReason
import io.ktor.websocket.readText
import kotlin.time.Duration.Companion.milliseconds
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.json.JsonObject
import net.primal.PrimalLib
import net.primal.core.utils.coroutines.DispatcherProvider
import okio.Buffer
import okio.GzipSink
import okio.GzipSource
import okio.buffer
import okio.use

internal class NostrSocketClientImpl(
    dispatcherProvider: DispatcherProvider,
    wssUrl: String,
    private val httpClient: HttpClient,
    private val incomingCompressionEnabled: Boolean = false,
    private val onSocketConnectionOpened: SocketConnectionOpenedCallback? = null,
    private val onSocketConnectionClosed: SocketConnectionClosedCallback? = null,
) : NostrSocketClient {

    override val socketUrl = wssUrl.cleanWebSocketUrl()

    private val scope = CoroutineScope(dispatcherProvider.io())

    private val webSocketMutex = Mutex()
    private var webSocket: DefaultClientWebSocketSession? = null

    private val _incomingMessages = MutableSharedFlow<NostrIncomingMessage>()
    override val incomingMessages = _incomingMessages.asSharedFlow()

    @OptIn(ExperimentalUuidApi::class)
    override suspend fun ensureSocketConnection() =
        webSocketMutex.withLock {
            if (webSocket == null) {
                openWebSocketConnection(url = socketUrl)
                if (incomingCompressionEnabled) {
                    val id = Uuid.random().toPrimalSubscriptionId()
                    sendMessage("""["REQ","$id",{"cache":["set_primal_protocol",{"compression":"zlib"}]}]""")
                }
            }
        }

    private fun openWebSocketConnection(url: String) = scope.launch {
        try {
            httpClient.webSocket(
                request = {
                    url(url)
                    PrimalLib.userAgent?.let {
                        headers.append("User-Agent", it)
                    }
                },
            ) {
                webSocket = this
                onSocketConnectionOpened?.invoke(url)
                receiveSocketMessages()
            }
        } catch (error: Exception) {
            Napier.w("NostrSocketClient::openWebSocketConnection($socketUrl) failed.", error)
            this@NostrSocketClientImpl.webSocket = null
            onSocketConnectionClosed?.invoke(socketUrl, error)
        }
    }

    private suspend fun WebSocketSession.receiveSocketMessages() {
        try {
            for (frame in incoming) {
                when (frame) {
                    is Frame.Text -> {
                        val text = frame.readText()
                        logLargeText(text = text, url = socketUrl, incoming = true)
                        processIncomingMessage(text = text)
                    }

                    is Frame.Binary -> {
                        val decompressedMessage = decompressMessage(frame.data)
                        logLargeText(text = decompressedMessage, url = socketUrl, incoming = true)
                        processIncomingMessage(text = decompressedMessage)
                    }

                    is Frame.Close -> {
                        val closeReason = frame.readReason()
                        Napier.w {
                            "WS $socketUrl closed with code=${closeReason?.code} and reason=${closeReason?.message}"
                        }
                        this@NostrSocketClientImpl.webSocket = null
                        onSocketConnectionClosed?.invoke(socketUrl, null)
                    }

                    else -> Unit
                }
            }
        } catch (error: Exception) {
            Napier.w("NostrSocketClient::receiveSocketMessages() on $socketUrl failed.", error)
            this@NostrSocketClientImpl.webSocket = null
            onSocketConnectionClosed?.invoke(socketUrl, error)
        }
    }

    override suspend fun close() {
        webSocket?.close(
            reason = CloseReason(
                code = CloseReason.Codes.NORMAL,
                message = "Closed by client.",
            ),
        )
    }

    private fun processIncomingMessage(text: String) {
        text.parseIncomingMessage()?.let {
            scope.launch {
                if (it is NostrIncomingMessage.EoseMessage) {
                    delay(75.milliseconds)
                }
                _incomingMessages.emit(value = it)
            }
        }
    }

    private suspend fun sendMessage(text: String) {
        logLargeText(text = text, url = socketUrl, incoming = false)
        webSocket?.send(Frame.Text(text = text))
    }

    override suspend fun sendREQ(subscriptionId: String, data: JsonObject) {
        val reqMessage = data.buildNostrREQMessage(subscriptionId)
        return sendMessage(text = reqMessage)
    }

    @OptIn(ExperimentalUuidApi::class)
    override suspend fun sendCOUNT(data: JsonObject): String {
        val subscriptionId: String = Uuid.random().toPrimalSubscriptionId()
        val reqMessage = data.buildNostrCOUNTMessage(subscriptionId)
        sendMessage(text = reqMessage)
        return subscriptionId
    }

    override suspend fun sendCLOSE(subscriptionId: String) = sendMessage(text = subscriptionId.buildNostrCLOSEMessage())

    override suspend fun sendEVENT(signedEvent: JsonObject) = sendMessage(text = signedEvent.buildNostrEVENTMessage())

    override suspend fun sendAUTH(signedEvent: JsonObject) = sendMessage(text = signedEvent.buildNostrAUTHMessage())

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
            Napier.d(
                tag = if (incoming) "NostrSocketClientIncoming" else "NostrSocketClientOutgoing",
                message = "$prefix $chunk $suffix",
            )
        }
    }

    @Suppress("unused")
    private fun compressMessage(message: String): ByteArray {
        val buffer = Buffer()
        GzipSink(buffer).buffer().use { sink ->
            sink.writeUtf8(message)
            sink.flush() // Ensure all data is written
        }
        return buffer.readByteArray()
    }

    private fun decompressMessage(compressedMessage: ByteArray): String {
        val buffer = Buffer().write(compressedMessage)
        GzipSource(buffer).buffer().use { source ->
            return source.readUtf8()
        }
    }

    private fun String.cleanWebSocketUrl(): String {
        return replace("https://", "wss://", ignoreCase = true)
            .replace("http://", "ws://", ignoreCase = true)
            .let { if (it.endsWith("/")) it.dropLast(1) else it }
    }

}
