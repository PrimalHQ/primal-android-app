package net.primal.android.networking.primal

import androidx.annotation.VisibleForTesting
import java.util.*
import javax.inject.Inject
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.FlowPreview
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
import net.primal.android.config.AppConfigProvider
import net.primal.android.config.dynamic.AppConfigUpdater
import net.primal.android.core.coroutines.CoroutineDispatcherProvider
import net.primal.android.networking.UserAgentProvider
import net.primal.android.networking.sockets.NostrIncomingMessage
import net.primal.android.networking.sockets.NostrSocketClient
import net.primal.android.networking.sockets.errors.NostrNoticeException
import net.primal.android.networking.sockets.errors.WssException
import net.primal.android.networking.sockets.filterBySubscriptionId
import okhttp3.OkHttpClient
import okhttp3.Request
import timber.log.Timber

class PrimalApiClient @Inject constructor(
    private val dispatcherProvider: CoroutineDispatcherProvider,
    private val okHttpClient: OkHttpClient,
    private val serverType: PrimalServerType,
    private val appConfigProvider: AppConfigProvider,
    private val appConfigUpdater: AppConfigUpdater,
) {

    private val scope = CoroutineScope(dispatcherProvider.io())

    private var socketClientInitialized: Boolean = false

    @VisibleForTesting
    lateinit var socketClient: NostrSocketClient

    private val socketClientMutex = Mutex()

    private val _connectionStatus = MutableStateFlow(PrimalServerConnectionStatus(serverType = serverType))
    val connectionStatus = _connectionStatus.asStateFlow()
    private fun updateStatus(reducer: PrimalServerConnectionStatus.() -> PrimalServerConnectionStatus) =
        scope.launch { _connectionStatus.getAndUpdate(reducer) }

    init {
        observeApiUrlAndInitializeSocketClient()
    }

    private fun observeApiUrlAndInitializeSocketClient() =
        scope.launch {
            appConfigProvider.observeApiUrlByType(type = serverType).collect { apiUrl ->
                socketClientMutex.withLock {
                    scope.launch { updateStatus { copy(url = apiUrl) } }
                    if (socketClientInitialized) {
                        scope.launch { updateStatus { copy(connected = false) } }
                        socketClient.close()
                    }
                    socketClient = buildAndInitializeSocketClient(apiUrl)
                }
            }
        }

    private suspend fun buildAndInitializeSocketClient(apiUrl: String): NostrSocketClient {
        return NostrSocketClient(
            dispatcherProvider = dispatcherProvider,
            okHttpClient = okHttpClient,
            wssRequest = Request.Builder()
                .url(apiUrl)
                .addHeader("User-Agent", UserAgentProvider.USER_AGENT)
                .build(),
            onSocketConnectionOpened = {
                scope.launch { updateStatus { copy(connected = true) } }
            },
            onSocketConnectionClosed = { _, _ ->
                scope.launch {
                    updateStatus { copy(connected = false) }
                    appConfigUpdater.updateAppConfigWithDebounce(1.minutes)
                }
            },
        ).apply {
            socketClientInitialized = true
            ensureSocketConnection()
        }
    }

    private suspend fun <T> retrySendMessage(times: Int, block: suspend () -> T): T {
        repeat(times) {
            try {
                return block()
            } catch (error: SocketSendMessageException) {
                Timber.w(error, "PrimalApiClient.retry()")
                delay(RETRY_DELAY_MILLIS)
            }
        }
        return block()
    }

    @Throws(WssException::class)
    suspend fun query(message: PrimalCacheFilter): PrimalQueryResult {
        val queryResult = runCatching {
            retrySendMessage(MAX_QUERY_RETRIES) {
                val subscriptionId = UUID.randomUUID()
                val deferredQueryResult = scope.async { collectQueryResult(subscriptionId) }
                sendMessageAndAwaitForResultOrThrow(
                    subscriptionId = subscriptionId,
                    data = message.toPrimalJsonObject(),
                    deferredQueryResult = deferredQueryResult,
                )
            }
        }
        val result = queryResult.getOrNull()
        val error = queryResult.exceptionOrNull().takeAsWssException()
        return result ?: throw error
    }

    private suspend fun sendMessageAndAwaitForResultOrThrow(
        subscriptionId: UUID,
        data: JsonObject,
        deferredQueryResult: Deferred<PrimalQueryResult>,
    ): PrimalQueryResult {
        ensureSocketClientConnection()
        return when (socketClient.sendREQ(subscriptionId = subscriptionId, data = data)) {
            true -> {
                try {
                    deferredQueryResult.await()
                } catch (error: CancellationException) {
                    throw error.cause ?: error
                }
            }
            false -> {
                deferredQueryResult.cancel(CancellationException("Unable to send socket message."))
                throw SocketSendMessageException(message = "Unable to send socket message.")
            }
        }
    }

    private fun Throwable?.takeAsWssException(): WssException {
        return when (this) {
            is WssException -> this
            is NostrNoticeException -> WssException(message = this.reason, cause = this)
            is SocketSendMessageException -> WssException(message = "Api unreachable at the moment.", cause = this)
            else -> WssException(message = this?.message, cause = this)
        }
    }

    suspend fun subscribe(subscriptionId: UUID, message: PrimalCacheFilter): Flow<NostrIncomingMessage> {
        ensureSocketClientConnection()
        val success = socketClient.sendREQ(
            subscriptionId = subscriptionId,
            data = message.toPrimalJsonObject(),
        )
        if (!success) throw WssException(message = "Api unreachable at the moment.")

        return socketClient.incomingMessages.filterBySubscriptionId(id = subscriptionId)
    }

    suspend fun closeSubscription(subscriptionId: UUID): Boolean {
        ensureSocketClientConnection()
        return socketClient.sendCLOSE(subscriptionId = subscriptionId)
    }

    @OptIn(FlowPreview::class)
    @Throws(NostrNoticeException::class)
    private suspend fun collectQueryResult(subscriptionId: UUID): PrimalQueryResult {
        val messages = socketClient.incomingMessages
            .filterBySubscriptionId(id = subscriptionId)
            .transformWhileEventsAreIncoming()
            .timeout(30.seconds)
            .toList()

        val terminationMessage = messages.last()

        if (terminationMessage is NostrIncomingMessage.NoticeMessage) {
            throw NostrNoticeException(
                reason = terminationMessage.message,
                subscriptionId = subscriptionId,
            )
        }

        val events = messages.filterIsInstance(NostrIncomingMessage.EventMessage::class.java)

        val nostrEvents = events.mapNotNull { it.nostrEvent }
        val primalEvents = events.mapNotNull { it.primalEvent }

        return PrimalQueryResult(
            terminationMessage = terminationMessage,
            nostrEvents = nostrEvents,
            primalEvents = primalEvents,
        )
    }

    private fun Flow<NostrIncomingMessage>.transformWhileEventsAreIncoming() =
        transformWhile {
            emit(it)
            it is NostrIncomingMessage.EventMessage
        }

    private suspend fun ensureSocketClientConnection() =
        socketClientMutex.withLock {
            socketClient.ensureSocketConnection()
        }

    private class SocketSendMessageException(override val message: String?) : RuntimeException()

    companion object {
        const val MAX_QUERY_RETRIES = 3
        private const val RETRY_DELAY_MILLIS = 1_000L
    }
}
