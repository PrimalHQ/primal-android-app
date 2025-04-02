package net.primal.android.networking.relays

import androidx.annotation.VisibleForTesting
import kotlin.time.Duration.Companion.milliseconds
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.getAndUpdate
import kotlinx.coroutines.flow.timeout
import kotlinx.coroutines.flow.transform
import kotlinx.coroutines.launch
import net.primal.android.core.coroutines.CoroutineDispatcherProvider
import net.primal.android.networking.relays.broadcast.BroadcastEventResponse
import net.primal.android.networking.relays.broadcast.BroadcastRequestBody
import net.primal.android.networking.relays.errors.NostrPublishException
import net.primal.android.user.domain.Relay
import net.primal.core.networking.primal.PrimalApiClient
import net.primal.core.networking.primal.PrimalCacheFilter
import net.primal.core.networking.sockets.NostrIncomingMessage
import net.primal.core.networking.sockets.NostrSocketClient
import net.primal.core.networking.sockets.NostrSocketClientFactory
import net.primal.core.networking.sockets.SocketConnectionClosedCallback
import net.primal.core.networking.sockets.SocketConnectionOpenedCallback
import net.primal.core.networking.sockets.errors.NostrNoticeException
import net.primal.core.networking.sockets.errors.WssException
import net.primal.core.networking.sockets.filterByEventId
import net.primal.core.networking.sockets.parseIncomingMessage
import net.primal.core.utils.serialization.CommonJson
import net.primal.core.utils.serialization.decodeFromStringOrNull
import net.primal.core.utils.serialization.encodeToJsonString
import net.primal.domain.nostr.NostrEvent
import net.primal.domain.nostr.NostrEventKind
import net.primal.domain.nostr.serialization.toNostrJsonObject
import timber.log.Timber

class RelayPool(
    dispatchers: CoroutineDispatcherProvider,
    private val nostrSocketClientFactory: NostrSocketClientFactory,
    private val primalApiClient: PrimalApiClient,
) {

    companion object {
        const val PUBLISH_TIMEOUT = 30_000
    }

    private val scope = CoroutineScope(dispatchers.io())

    var relays: List<Relay> = emptyList()
        private set

    @VisibleForTesting
    var socketClients = listOf<NostrSocketClient>()

    private val _relayPoolStatus = MutableStateFlow(mapOf<String, Boolean>())
    val relayPoolStatus = _relayPoolStatus.asStateFlow()
    private fun updateRelayStatus(url: String, connected: Boolean) =
        scope.launch {
            _relayPoolStatus.getAndUpdate {
                it.toMutableMap().apply { this[url] = connected }
            }
        }

    private val onSocketConnectionOpenedCallback: SocketConnectionOpenedCallback = { url ->
        updateRelayStatus(url = url, connected = true)
    }

    private val onSocketConnectionClosedCallback: SocketConnectionClosedCallback = { url, _ ->
        updateRelayStatus(url = url, connected = false)
    }

    fun changeRelays(relays: List<Relay>) {
        val existingRelayUrls = socketClients.map { it.socketUrl }
        val newRelayUrls = relays.map { it.url }

        val toAddRelayUrls = newRelayUrls.filter { it !in existingRelayUrls }
        val toAddSocketClients = relays.filter { it.url in toAddRelayUrls }.mapAsNostrSocketClient()
        val toRemoveSocketClients = socketClients.filter { it.socketUrl !in newRelayUrls }

        val newSocketClients = socketClients.toMutableList().apply {
            removeAll(toRemoveSocketClients)
            addAll(toAddSocketClients)
        }

        socketClients = newSocketClients
        toRemoveSocketClients.forEach { scope.launch { it.close() } }
        this.relays = relays
    }

    fun closePool() {
        socketClients.forEach { scope.launch { it.close() } }
        socketClients = emptyList()
        relays = emptyList()
    }

    fun hasRelays() = relays.isNotEmpty()

    suspend fun ensureAllRelaysConnected() {
        socketClients.forEach { it.ensureSocketConnection() }
    }

    suspend fun ensureRelayConnected(url: String) {
        socketClients.find { it.socketUrl == url }?.ensureSocketConnection()
    }

    private fun List<Relay>.mapAsNostrSocketClient() =
        this.map {
            nostrSocketClientFactory.create(
                wssUrl = it.url,
                onSocketConnectionOpened = onSocketConnectionOpenedCallback,
                onSocketConnectionClosed = onSocketConnectionClosedCallback,
            )
        }

    @Throws(NostrPublishException::class)
    suspend fun publishEvent(nostrEvent: NostrEvent, cachingProxyEnabled: Boolean = false) {
        if (cachingProxyEnabled) {
            handleBroadcastEventThroughCachingProxy(relays.map { it.url }, nostrEvent)
        } else {
            handlePublishEventToRelays(socketClients, nostrEvent)
        }
    }

    private suspend fun handleBroadcastEventThroughCachingProxy(relayUrls: List<String>, nostrEvent: NostrEvent) {
        val result = try {
            val queryResult = primalApiClient.query(
                message = PrimalCacheFilter(
                    primalVerb = net.primal.data.remote.PrimalVerb.BROADCAST_EVENTS.id,
                    optionsJson = BroadcastRequestBody(
                        events = listOf(nostrEvent),
                        relays = relayUrls,
                    ).encodeToJsonString(),
                ),
            )
            val broadcastEvents = queryResult.findPrimalEvent(NostrEventKind.PrimalBroadcastResult)
            CommonJson.decodeFromStringOrNull<List<BroadcastEventResponse>>(broadcastEvents?.content)
        } catch (error: WssException) {
            Timber.w(error)
            null
        } ?: throw NostrPublishException(
            cause = WssException(message = "Primal NostrEvent 10_000_149 not found or invalid."),
        )

        result.find { response -> response.eventId == nostrEvent.id }?.responses
            ?.mapNotNull { relayResponse ->
                val relay = relayResponse.firstOrNull()
                val responseMessage = relayResponse.getOrNull(index = 1)?.parseIncomingMessage()
                if (relay != null && responseMessage != null) {
                    relay to responseMessage
                } else {
                    null
                }
            }
            ?.find { (_, relayMessage) -> relayMessage is NostrIncomingMessage.OkMessage && relayMessage.success }
            ?: throw NostrPublishException(
                cause = WssException("Event broadcast failed. Could not find success response from relays."),
            )
    }

    @OptIn(FlowPreview::class)
    private suspend fun handlePublishEventToRelays(relayConnections: List<NostrSocketClient>, nostrEvent: NostrEvent) {
        val responseFlow = MutableSharedFlow<NostrPublishResult>()
        relayConnections.forEach { nostrSocketClient ->
            scope.launch {
                with(nostrSocketClient) {
                    val sendEventResult = runCatching {
                        ensureSocketConnection()
                        sendEVENT(nostrEvent.toNostrJsonObject())
                        collectPublishResponse(eventId = nostrEvent.id)
                    }
                    sendEventResult.getOrNull()?.let {
                        responseFlow.emit(NostrPublishResult(result = it))
                    }
                    sendEventResult.exceptionOrNull()?.let {
                        Timber.w(it, "sendEVENT failed to $socketUrl")
                        responseFlow.emit(NostrPublishResult(error = it))
                    }
                }
            }
        }

        var responseCount = 0
        responseFlow.timeout(PUBLISH_TIMEOUT.milliseconds)
            .catch { throw NostrPublishException(cause = it) }
            .transform {
                emit(it)
                responseCount++
                if (relayConnections.size == responseCount && !it.isSuccessful()) {
                    throw NostrPublishException(cause = null)
                }
            }
            .first { it.isSuccessful() }
    }

    @FlowPreview
    private suspend fun NostrSocketClient.collectPublishResponse(eventId: String): NostrIncomingMessage.OkMessage {
        return incomingMessages
            .filterByEventId(id = eventId)
            .transform {
                when (it) {
                    is NostrIncomingMessage.OkMessage -> emit(it)
                    is NostrIncomingMessage.NoticeMessage -> throw NostrNoticeException(reason = it.message)
                    else -> error("$it is not allowed")
                }
            }
            .timeout(PUBLISH_TIMEOUT.milliseconds)
            .first()
    }

    private fun NostrPublishResult.isSuccessful(): Boolean {
        return result is NostrIncomingMessage.OkMessage && result.success
    }
}
