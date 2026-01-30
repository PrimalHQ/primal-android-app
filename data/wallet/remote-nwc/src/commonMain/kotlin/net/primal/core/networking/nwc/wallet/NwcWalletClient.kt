package net.primal.core.networking.nwc.wallet

import io.github.aakira.napier.Napier
import kotlin.uuid.Uuid
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.json.add
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import net.primal.core.networking.nwc.wallet.model.WalletNwcRequest
import net.primal.core.networking.nwc.wallet.model.WalletNwcRequestException
import net.primal.core.networking.sockets.NostrIncomingMessage
import net.primal.core.networking.sockets.NostrSocketClientFactory
import net.primal.core.networking.sockets.SocketConnectionClosedCallback
import net.primal.core.networking.sockets.SocketConnectionOpenedCallback
import net.primal.core.networking.sockets.filterBySubscriptionId
import net.primal.core.networking.sockets.toPrimalSubscriptionId
import net.primal.core.utils.coroutines.DispatcherProvider
import net.primal.core.utils.fold
import net.primal.domain.connections.nostr.model.NwcConnection
import net.primal.domain.nostr.NostrEvent
import net.primal.domain.nostr.NostrEventKind
import net.primal.domain.nostr.findFirstProfileId
import net.primal.domain.nostr.serialization.toNostrJsonObject

class NwcWalletClient(
    relayUrl: String,
    dispatchers: DispatcherProvider,
    private val requestParser: NwcWalletRequestParser,
    onSocketConnectionOpened: SocketConnectionOpenedCallback? = null,
    onSocketConnectionClosed: SocketConnectionClosedCallback? = null,
) {
    private val scope = CoroutineScope(dispatchers.io() + SupervisorJob())

    private val subscriptionId = Uuid.random().toPrimalSubscriptionId()

    private val connectionMapMutex = Mutex()
    private var connectionMap: Map<String, NwcConnection> = emptyMap()

    private var subscriptionJob: Job? = null

    private val nostrSocketClient = NostrSocketClientFactory.create(
        wssUrl = relayUrl,
        onSocketConnectionOpened = { url ->
            onSocketConnectionOpened?.invoke(url)
            scope.launch {
                val servicePubKeys = connectionMapMutex.withLock { connectionMap.keys.toList() }
                if (servicePubKeys.isNotEmpty()) {
                    runCatching { sendFilterRequest(servicePubKeys) }
                        .onFailure {
                            Napier.w(it, tag = "NwcWalletClient") { "Failed to resubscribe on reconnect." }
                        }
                }
            }
        },
        onSocketConnectionClosed = onSocketConnectionClosed,
    )

    private val _incomingRequests: Channel<WalletNwcRequest> = Channel(Channel.BUFFERED)
    val incomingRequests = _incomingRequests.receiveAsFlow()

    private val _errors: Channel<WalletNwcRequestException> = Channel(Channel.BUFFERED)
    val errors = _errors.receiveAsFlow()

    suspend fun connect() {
        nostrSocketClient.ensureSocketConnectionOrThrow()
        startSubscriptionListener()
    }

    suspend fun updateConnections(connections: List<NwcConnection>) {
        val servicePubKeys = connectionMapMutex.withLock {
            connectionMap = connections.associateBy { it.serviceKeyPair.pubKey }
            connectionMap.keys.toList()
        }

        if (servicePubKeys.isNotEmpty()) {
            sendFilterRequest(servicePubKeys)
        }
    }

    private suspend fun sendFilterRequest(servicePubKeys: List<String>) {
        nostrSocketClient.sendREQ(
            subscriptionId = subscriptionId,
            data = buildJsonObject {
                put("kinds", buildJsonArray { add(NostrEventKind.NwcRequest.value) })
                put(
                    "#p",
                    buildJsonArray {
                        servicePubKeys.forEach { add(it) }
                    },
                )
            },
        )
    }

    private fun startSubscriptionListener() {
        subscriptionJob?.cancel()
        subscriptionJob = scope.launch {
            nostrSocketClient.incomingMessages
                .filterBySubscriptionId(id = subscriptionId)
                .collect { message ->
                    if (message is NostrIncomingMessage.EventMessage) {
                        message.nostrEvent?.let { event ->
                            if (event.kind == NostrEventKind.NwcRequest.value) {
                                processNwcRequestEvent(event)
                            }
                        }
                    }
                }
        }
    }

    private suspend fun processNwcRequestEvent(event: NostrEvent) {
        val targetPtag = event.tags.findFirstProfileId()
        val connection = connectionMapMutex.withLock { connectionMap[targetPtag] }

        if (connection != null) {
            requestParser.parseNostrEvent(
                event = event,
                connection = connection,
            ).fold(
                onSuccess = { request ->
                    scope.launch { _incomingRequests.send(request) }
                },
                onFailure = { error ->
                    Napier.w(tag = "NwcWalletClient", throwable = error) { "Failed to parse NWC request." }
                    scope.launch {
                        _errors.send(
                            WalletNwcRequestException(
                                nostrEvent = event,
                                connection = connection,
                                cause = error,
                            ),
                        )
                    }
                },
            )
        }
    }

    suspend fun destroy() {
        nostrSocketClient.close()
        scope.cancel()
    }

    suspend fun publishEvent(event: NostrEvent) {
        nostrSocketClient.ensureSocketConnectionOrThrow()
        nostrSocketClient.sendEVENT(signedEvent = event.toNostrJsonObject())
    }
}
