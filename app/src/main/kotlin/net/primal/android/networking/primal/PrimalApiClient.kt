package net.primal.android.networking.primal

import java.util.*
import javax.inject.Inject
import kotlin.time.Duration.Companion.minutes
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.getAndUpdate
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.flow.transformWhile
import kotlinx.coroutines.launch
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

class PrimalApiClient @Inject constructor(
    private val dispatcherProvider: CoroutineDispatcherProvider,
    private val okHttpClient: OkHttpClient,
    private val serverType: PrimalServerType,
    private val appConfigProvider: AppConfigProvider,
    private val appConfigUpdater: AppConfigUpdater,
) {

    private val scope = CoroutineScope(dispatcherProvider.io())

    private var socketClientInitialized: Boolean = false

    private lateinit var socketClient: NostrSocketClient

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
                scope.launch { updateStatus { copy(url = apiUrl) } }
                if (socketClientInitialized) {
                    scope.launch { updateStatus { copy(connected = false) } }
                    socketClient.close()
                }

                socketClient = NostrSocketClient(
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
        }

    private suspend fun sendREQWithRetry(data: JsonObject): UUID? {
        var queryAttempts = 0
        while (queryAttempts < MAX_QUERY_ATTEMPTS) {
            socketClient.ensureSocketConnection()
            val subscriptionId = socketClient.sendREQ(data = data)
            if (subscriptionId != null) return subscriptionId

            queryAttempts++
            if (queryAttempts < MAX_QUERY_ATTEMPTS) {
                delay(RETRY_DELAY_MILLIS)
            }
        }
        return null
    }

    @Throws(WssException::class)
    suspend fun query(message: PrimalCacheFilter): PrimalQueryResult {
        val subscriptionId = sendREQWithRetry(data = message.toPrimalJsonObject())
            ?: throw WssException(message = "Api unreachable at the moment.")

        return try {
            collectQueryResult(subscriptionId = subscriptionId)
        } catch (error: NostrNoticeException) {
            throw WssException(message = error.reason, cause = error)
        }
    }

    suspend fun subscribe(subscriptionId: UUID, message: PrimalCacheFilter): Flow<NostrIncomingMessage> {
        socketClient.ensureSocketConnection()
        val success = socketClient.sendREQ(
            subscriptionId = subscriptionId,
            data = message.toPrimalJsonObject(),
        )
        if (!success) throw WssException(message = "Api unreachable at the moment.")

        return socketClient.incomingMessages.filterBySubscriptionId(id = subscriptionId)
    }

    suspend fun closeSubscription(subscriptionId: UUID): Boolean {
        socketClient.ensureSocketConnection()
        return socketClient.sendCLOSE(subscriptionId = subscriptionId)
    }

    @Throws(NostrNoticeException::class)
    private suspend fun collectQueryResult(subscriptionId: UUID): PrimalQueryResult {
        val messages = socketClient.incomingMessages
            .transformWhileEventsAreIncoming(subscriptionId)
            .toList()

        val terminationMessage = messages.last()

        if (terminationMessage is NostrIncomingMessage.NoticeMessage) {
            throw NostrNoticeException(reason = terminationMessage.message)
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

    private fun Flow<NostrIncomingMessage>.transformWhileEventsAreIncoming(subscriptionId: UUID) =
        this.filterBySubscriptionId(id = subscriptionId).transformWhile {
            emit(it)
            it is NostrIncomingMessage.EventMessage
        }

    companion object {
        private const val MAX_QUERY_ATTEMPTS = 3
        private const val RETRY_DELAY_MILLIS = 1_000L
    }
}
