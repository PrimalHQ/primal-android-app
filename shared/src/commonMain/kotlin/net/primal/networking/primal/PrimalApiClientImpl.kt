package net.primal.networking.primal

import androidx.annotation.VisibleForTesting
import io.github.aakira.napier.Napier
import io.ktor.client.HttpClient
import kotlin.time.Duration.Companion.seconds
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.getAndUpdate
import kotlinx.coroutines.flow.timeout
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.flow.transformWhile
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.json.JsonObject
import net.primal.core.coroutines.DispatcherProvider
import net.primal.networking.sockets.NostrIncomingMessage
import net.primal.networking.sockets.NostrSocketClient
import net.primal.networking.sockets.errors.NostrNoticeException
import net.primal.networking.sockets.errors.WssException
import net.primal.networking.sockets.filterBySubscriptionId
import net.primal.networking.sockets.toPrimalSubscriptionId

internal class PrimalApiClientImpl(
    private val dispatcherProvider: DispatcherProvider,
    private val httpClient: HttpClient,
    private val serverType: PrimalServerType,
//    private val appConfigProvider: AppConfigProvider,
//    private val appConfigHandler: AppConfigHandler,
) : PrimalApiClient {

    // TODO Remove unused SocketSendMessageException

    private val scope = CoroutineScope(dispatcherProvider.io())

    private var socketClientInitialized: Boolean = false

    @VisibleForTesting
    lateinit var socketClient: NostrSocketClient

    private val socketClientMutex = Mutex()

    private val _connectionStatus = MutableStateFlow(PrimalServerConnectionStatus(serverType = serverType))
    override val connectionStatus = _connectionStatus.asStateFlow()
    private fun updateStatus(reducer: PrimalServerConnectionStatus.() -> PrimalServerConnectionStatus) =
        scope.launch { _connectionStatus.getAndUpdate(reducer) }

    init {
        observeApiUrlAndInitializeSocketClient()
    }

    private fun observeApiUrlAndInitializeSocketClient() =
        scope.launch {
            // TODO Fix observeApiUrlByType
            val apiUrl = "wss://stage.primal.net"
//            appConfigProvider.observeApiUrlByType(type = serverType).collect { apiUrl ->
                socketClientMutex.withLock {
                    scope.launch { updateStatus { copy(url = apiUrl) } }
                    if (socketClientInitialized) {
                        scope.launch { updateStatus { copy(connected = false) } }
                        socketClient.close()
                    }
                    socketClient = buildAndInitializeSocketClient(apiUrl)
                    socketClient.ensureSocketConnection()
                    socketClientInitialized = true
                }
//            }
        }

    private fun buildAndInitializeSocketClient(apiUrl: String): NostrSocketClient {
        return NostrSocketClient(
            dispatcherProvider = dispatcherProvider,
            httpClient = httpClient,
            incomingCompressionEnabled = serverType.isIncomingCompressionSupported(),
            wssUrl = apiUrl,
            onSocketConnectionOpened = {
                scope.launch { updateStatus { copy(connected = true) } }
            },
            onSocketConnectionClosed = { _, _ ->
                scope.launch {
                    updateStatus { copy(connected = false) }
                    // TODO Fix updateAppConfigWithDebounce
//                    appConfigHandler.updateAppConfigWithDebounce(1.minutes)
                }
            },
        )
    }

    private fun PrimalServerType.isIncomingCompressionSupported(): Boolean {
        return this == PrimalServerType.Caching || this == PrimalServerType.Wallet
    }

    private suspend fun <T> retrySendMessage(times: Int, block: suspend (Int) -> T): T {
        repeat(times) {
            try {
                return block(it)
            } catch (error: SocketSendMessageException) {
                Napier.w(error) { "PrimalApiClient.retry()" }
                delay(RETRY_DELAY_MILLIS)
            }
        }
        return block(times)
    }

    @OptIn(ExperimentalUuidApi::class)
    override suspend fun query(message: PrimalCacheFilter): PrimalQueryResult {
        val queryResult = runCatching {
            retrySendMessage(MAX_RETRIES) {
                ensureSocketClientConnection()
                val subscriptionId = Uuid.random().toPrimalSubscriptionId()
                val deferredQueryResult = asyncQueryCollection(subscriptionId)

                try {
                    sendMessageOrThrow(subscriptionId = subscriptionId, data = message.toPrimalJsonObject())
                } catch (error: SocketSendMessageException) {
                    deferredQueryResult.cancel(CancellationException("Unable to send socket message."))
                    throw error
                }

                deferredQueryResult.await()
            }
        }
        val result = queryResult.getOrNull()
        val error = queryResult.exceptionOrNull().takeAsWssException()
        return result ?: throw error
    }

    private fun asyncQueryCollection(subscriptionId: String): Deferred<PrimalQueryResult> {
        // TODO Revisit try/catch; this is prevents proper coroutine cancellation
        return scope.async(SupervisorJob()) {
            try {
                collectQueryResult(subscriptionId)
            } catch (error: CancellationException) {
                throw WssException(message = "Api query timed out.", cause = error)
            }
        }
    }

    private suspend fun sendMessageOrThrow(subscriptionId: String, data: JsonObject) {
        socketClient.sendREQ(subscriptionId = subscriptionId, data = data)
    }

    private fun Throwable?.takeAsWssException(): WssException {
        return when (this) {
            is WssException -> this
            is NostrNoticeException -> WssException(message = this.reason, cause = this)
            is SocketSendMessageException -> WssException(message = "Api unreachable at the moment.", cause = this)
            else -> WssException(message = this?.message, cause = this)
        }
    }

    override suspend fun subscribe(subscriptionId: String, message: PrimalCacheFilter): Flow<NostrIncomingMessage> {
        ensureSocketClientConnection()
        try {
            retrySendMessage(MAX_RETRIES) {
                sendMessageOrThrow(subscriptionId = subscriptionId, data = message.toPrimalJsonObject())
            }
        } catch (error: SocketSendMessageException) {
            Napier.w(error) { "Unable to subscribe." }
            throw WssException(message = "Api unreachable at the moment.", cause = error)
        }
        return socketClient.incomingMessages.filterBySubscriptionId(id = subscriptionId)
    }

    override suspend fun closeSubscription(subscriptionId: String) {
        ensureSocketClientConnection()
        return socketClient.sendCLOSE(subscriptionId = subscriptionId)
    }

    @OptIn(FlowPreview::class)
    @Throws(NostrNoticeException::class, kotlin.coroutines.cancellation.CancellationException::class)
    private suspend fun collectQueryResult(subscriptionId: String): PrimalQueryResult {
        val messages = socketClient.incomingMessages
            .filterBySubscriptionId(id = subscriptionId)
            .transformWhileEventsAreIncoming()
            .timeout(20.seconds)
            .toList()

        val terminationMessage = messages.last()

        if (terminationMessage is NostrIncomingMessage.NoticeMessage) {
            throw NostrNoticeException(
                reason = terminationMessage.message,
                subscriptionId = subscriptionId,
            )
        }

        val eventMessages = messages.filterIsInstance<NostrIncomingMessage.EventMessage>()
        val eventsMessage = messages.filterIsInstance<NostrIncomingMessage.EventsMessage>()

        val allNostrEvents = eventMessages.mapNotNull { it.nostrEvent } +
            eventsMessage.map { it.nostrEvents }.flatten()

        val allPrimalEvents = eventMessages.mapNotNull { it.primalEvent } +
            eventsMessage.map { it.primalEvents }.flatten()

        return PrimalQueryResult(
            terminationMessage = terminationMessage,
            nostrEvents = allNostrEvents,
            primalEvents = allPrimalEvents,
        )
    }

    private fun Flow<NostrIncomingMessage>.transformWhileEventsAreIncoming() =
        transformWhile {
            emit(it)
            it is NostrIncomingMessage.EventMessage || it is NostrIncomingMessage.EventsMessage
        }

    private suspend fun ensureSocketClientConnection() =
        socketClientMutex.withLock {
            socketClient.ensureSocketConnection()
        }

    @Deprecated("This is no longer being thrown anywhere since Ktor implementation")
    private class SocketSendMessageException(override val message: String?) : RuntimeException()

    companion object {
        const val MAX_RETRIES = 3
        private const val RETRY_DELAY_MILLIS = 500L
    }
}
