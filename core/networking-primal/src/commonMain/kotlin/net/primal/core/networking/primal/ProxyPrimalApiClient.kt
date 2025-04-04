package net.primal.core.networking.primal

import io.ktor.client.HttpClient
import kotlin.time.Duration.Companion.minutes
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.getAndUpdate
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import net.primal.core.config.AppConfigHandler
import net.primal.core.config.AppConfigProvider
import net.primal.core.config.observeApiUrlByType
import net.primal.core.networking.sockets.NostrIncomingMessage
import net.primal.core.networking.sockets.NostrSocketClientImpl
import net.primal.core.utils.coroutines.DispatcherProvider
import net.primal.domain.PrimalServerType

internal class ProxyPrimalApiClient(
    private val dispatcherProvider: DispatcherProvider,
    private val httpClient: HttpClient,
    private val serverType: PrimalServerType,
    private val appConfigProvider: AppConfigProvider,
    private val appConfigHandler: AppConfigHandler,
) : PrimalApiClient {

    private val scope = CoroutineScope(dispatcherProvider.io())

    private lateinit var primalClient: BasePrimalApiClient
    private lateinit var socketClient: NostrSocketClientImpl
    private var clientInitialized: Boolean = false
    private val clientMutex = Mutex()

    private val _connectionStatus = MutableStateFlow(PrimalServerConnectionStatus(serverType = serverType))
    override val connectionStatus = _connectionStatus.asStateFlow()

    private fun updateStatus(reducer: PrimalServerConnectionStatus.() -> PrimalServerConnectionStatus) {
        _connectionStatus.getAndUpdate(reducer)
    }

    init {
        scope.launch { appConfigHandler.updateAppConfigOrFailSilently() }
        observeApiUrlAndInitializeSocketClient()
    }

    private fun observeApiUrlAndInitializeSocketClient() =
        scope.launch {
            appConfigProvider.observeApiUrlByType(type = serverType).collect { apiUrl ->
                clientMutex.withLock {
                    updateStatus { copy(url = apiUrl) }

                    if (clientInitialized) {
                        updateStatus { copy(connected = false) }
                        socketClient.close()
                    }

                    socketClient = buildAndInitializeSocketClient(apiUrl).apply {
                        primalClient = BasePrimalApiClient(socketClient = this)
                        clientInitialized = true
                    }
                }
            }
        }

    private fun buildAndInitializeSocketClient(apiUrl: String): NostrSocketClientImpl {
        return NostrSocketClientImpl(
            dispatcherProvider = dispatcherProvider,
            httpClient = httpClient,
            incomingCompressionEnabled = serverType.isIncomingCompressionSupported(),
            wssUrl = apiUrl,
            onSocketConnectionOpened = {
                updateStatus { copy(connected = true) }
            },
            onSocketConnectionClosed = { _, _ ->
                updateStatus { copy(connected = false) }
                scope.launch { appConfigHandler.updateAppConfigWithDebounce(1.minutes) }
            },
        )
    }

    private fun PrimalServerType.isIncomingCompressionSupported(): Boolean {
        return this == PrimalServerType.Caching || this == PrimalServerType.Wallet
    }

    override suspend fun query(message: PrimalCacheFilter): PrimalQueryResult {
        return primalClient.query(message = message)
    }

    override suspend fun subscribe(subscriptionId: String, message: PrimalCacheFilter): Flow<NostrIncomingMessage> {
        return primalClient.subscribe(subscriptionId = subscriptionId, message = message)
    }

    override suspend fun closeSubscription(subscriptionId: String): Boolean {
        return primalClient.closeSubscription(subscriptionId = subscriptionId)
    }
}
