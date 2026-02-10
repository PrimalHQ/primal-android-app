package net.primal.wallet.data.nwc.service

import io.github.aakira.napier.Napier
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import net.primal.core.networking.nwc.nip47.NwcError
import net.primal.core.networking.nwc.wallet.NwcWalletClient
import net.primal.core.networking.nwc.wallet.NwcWalletRequestParser
import net.primal.core.networking.nwc.wallet.model.WalletNwcRequest
import net.primal.core.networking.nwc.wallet.model.WalletNwcRequestException
import net.primal.core.networking.nwc.wallet.signNwcErrorResponseNostrEvent
import net.primal.core.networking.nwc.wallet.signNwcInfoNostrEvent
import net.primal.core.networking.nwc.wallet.signNwcResponseNostrEvent
import net.primal.core.nips.encryption.service.NostrEncryptionService
import net.primal.core.utils.batchOnInactivity
import net.primal.core.utils.cache.LruSeenCache
import net.primal.core.utils.coroutines.DispatcherProvider
import net.primal.domain.connections.nostr.NwcRepository
import net.primal.domain.connections.nostr.NwcService
import net.primal.domain.connections.nostr.model.NwcConnection
import net.primal.domain.nostr.cryptography.utils.unwrapOrThrow
import net.primal.wallet.data.nwc.NwcCapabilities
import net.primal.wallet.data.nwc.builder.NwcWalletResponseBuilder
import net.primal.wallet.data.nwc.manager.NwcBudgetManager
import net.primal.wallet.data.nwc.processor.NwcRequestProcessor

class NwcServiceImpl internal constructor(
    private val dispatchers: DispatcherProvider,
    private val nwcRepository: NwcRepository,
    private val encryptionService: NostrEncryptionService,
    private val requestParser: NwcWalletRequestParser,
    private val requestProcessor: NwcRequestProcessor,
    private val responseBuilder: NwcWalletResponseBuilder,
    private val budgetManager: NwcBudgetManager,
) : NwcService {

    private val scope = CoroutineScope(dispatchers.io() + SupervisorJob())
    private val cache: LruSeenCache<String> = LruSeenCache(maxEntries = MAX_CACHE_SIZE)
    private val retrySendResponseQueue = MutableSharedFlow<PendingNwcResponse>()

    private val clientMutex = Mutex()
    private var nwcClient: NwcWalletClient? = null
    private var clientJob: Job? = null
    private val publishedInfoPubKeys: MutableSet<String> = mutableSetOf()

    override fun initialize(userId: String) {
        Napier.d(tag = TAG) { "NwcService initializing for userId=$userId" }
        startPeriodicCleanup()
        observeConnections(userId)
        observeRetrySendResponseQueue()
    }

    private fun startPeriodicCleanup() {
        scope.launch {
            while (isActive) {
                runCatching {
                    Napier.d(tag = TAG) { "Running cleanupExpiredHolds" }
                    budgetManager.cleanupExpiredHolds()
                }.onFailure {
                    Napier.w(tag = TAG, throwable = it) { "cleanupExpiredHolds failed" }
                }
                delay(CLEANUP_INTERVAL)
            }
        }
    }

    private suspend fun connectToRelay(relayUrl: String) =
        clientMutex.withLock {
            if (nwcClient != null) return@withLock

            val client = NwcWalletClient(
                relayUrl = relayUrl,
                dispatchers = dispatchers,
                requestParser = requestParser,
                onSocketConnectionOpened = { url -> Napier.d(tag = TAG) { "Connected to NWC relay: $url" } },
                onSocketConnectionClosed = { url, _ -> Napier.d(tag = TAG) { "Disconnected from NWC relay: $url" } },
            )

            runCatching { client.connect() }
                .onSuccess {
                    nwcClient = client
                    clientJob = observeClientFlows(client)
                }
                .onFailure {
                    Napier.w(tag = TAG, throwable = it) { "Failed to connect to NWC relay." }
                    client.destroy()
                }
        }

    private suspend fun disconnectFromRelay() =
        clientMutex.withLock {
            clientJob?.cancel()
            clientJob = null
            nwcClient?.destroy()
            nwcClient = null
            publishedInfoPubKeys.clear()
            Napier.d(tag = TAG) { "Disconnected from NWC relay." }
        }

    private fun observeConnections(userId: String) =
        scope.launch {
            Napier.d(tag = TAG) { "Starting to observe connections for userId=$userId" }
            nwcRepository.observeConnections(userId = userId).collect { connections ->
                Napier.d(tag = TAG) { "Connections updated: ${connections.size} connection(s)" }
                if (connections.isNotEmpty()) {
                    connections.forEach { conn ->
                        Napier.d(
                            tag = TAG,
                        ) { "Connection: servicePubKey=${conn.serviceKeyPair.pubKey.take(8)}..., relay=${conn.relay}" }
                    }
                    val relayUrl = connections.first().relay
                    connectToRelay(relayUrl)
                    runCatching { nwcClient?.updateConnections(connections) }
                        .onSuccess {
                            Napier.d(tag = TAG) { "Connections updated on client successfully" }
                        }
                        .onFailure {
                            Napier.w(tag = TAG, throwable = it) { "Failed to update connections." }
                        }
                    ensureInfoEventsArePublished(connections)
                } else {
                    Napier.d(tag = TAG) { "No connections found, disconnecting from relay" }
                    disconnectFromRelay()
                }
            }
        }

    private suspend fun ensureInfoEventsArePublished(connections: List<NwcConnection>) {
        for (connection in connections) {
            val servicePubKey = connection.serviceKeyPair.pubKey

            val (shouldPublish, client) = clientMutex.withLock {
                val client = nwcClient ?: return@withLock false to null
                val alreadyPublished = servicePubKey in publishedInfoPubKeys
                !alreadyPublished to client
            }

            if (!shouldPublish || client == null) continue

            runCatching {
                val signedEvent = signNwcInfoNostrEvent(
                    connection = connection,
                    supportedMethods = NwcCapabilities.supportedMethods,
                    supportedEncryption = NwcCapabilities.supportedEncryption,
                    supportedNotifications = NwcCapabilities.supportedNotifications,
                ).unwrapOrThrow()
                client.publishEvent(signedEvent)
                clientMutex.withLock {
                    publishedInfoPubKeys.add(servicePubKey)
                }
                Napier.d(tag = TAG) { "Published info event for servicePubKey=${servicePubKey.take(8)}..." }
            }.onFailure {
                Napier.w(tag = TAG, throwable = it) { "Failed to publish info event" }
            }
        }
    }

    private fun observeClientFlows(client: NwcWalletClient): Job =
        scope.launch {
            observeIncomingRequests(client)
            observeErrors(client)
        }

    private fun observeIncomingRequests(client: NwcWalletClient) =
        scope.launch {
            client.incomingRequests.collect { request ->
                if (cache.seen(request.eventId)) return@collect
                cache.mark(request.eventId)
                Napier.d(tag = TAG) { "Received NWC request: ${request.eventId}" }
                processRequest(request)
            }
        }

    private fun observeErrors(client: NwcWalletClient) =
        scope.launch {
            client.errors.collect { error ->
                if (cache.seen(error.nostrEvent.id)) return@collect
                cache.mark(error.nostrEvent.id)
                Napier.w(tag = TAG, throwable = error.cause) {
                    "NWC request error for event: ${error.nostrEvent.id}"
                }
                sendErrorResponse(error)
            }
        }

    private fun sendErrorResponse(error: WalletNwcRequestException) =
        scope.launch {
            val responseJson = responseBuilder.buildParsingErrorResponse(
                code = NwcError.INTERNAL,
                message = error.cause?.message ?: "Failed to parse request",
            )

            runCatching {
                val client = nwcClient ?: error("NwcWalletClient is not initialized")
                val signedEvent = signNwcErrorResponseNostrEvent(
                    error = error,
                    responseJson = responseJson,
                    encryptionService = encryptionService,
                ).unwrapOrThrow()

                Napier.d(tag = TAG) {
                    "Publishing error response: id=${signedEvent.id.take(8)}..., " +
                        "eventId=${error.nostrEvent.id.take(8)}..."
                }
                client.publishEvent(signedEvent)
                Napier.d(tag = TAG) { "Error response published successfully" }
            }.onFailure {
                Napier.w(tag = TAG, throwable = it) { "Failed to publish NWC error response" }
            }
        }

    @OptIn(FlowPreview::class)
    private fun observeRetrySendResponseQueue() =
        scope.launch {
            retrySendResponseQueue
                .batchOnInactivity(inactivityTimeout = 3.seconds)
                .collect { batchedResponses ->
                    batchedResponses.forEach { pending ->
                        sendResponse(pending.request, pending.responseJson)
                    }
                }
        }

    private fun processRequest(request: WalletNwcRequest) =
        scope.launch {
            val response = requestProcessor.process(request)
            Napier.d(tag = TAG) { "Sending response for eventId=${request.eventId.take(8)}..." }
            sendResponseOrAddToRetryQueue(request, response)
        }

    private suspend fun sendResponseOrAddToRetryQueue(request: WalletNwcRequest, responseJson: String) {
        sendResponse(request, responseJson).onFailure {
            Napier.d(tag = TAG) { "Adding response to retry queue: ${request.eventId}" }
            retrySendResponseQueue.emit(PendingNwcResponse(request, responseJson))
        }
    }

    private suspend fun sendResponse(request: WalletNwcRequest, responseJson: String): Result<Unit> {
        return runCatching {
            val client = nwcClient ?: error("NwcWalletClient is not initialized")
            val signedEvent = signNwcResponseNostrEvent(
                request = request,
                responseJson = responseJson,
                encryptionService = encryptionService,
            ).unwrapOrThrow()

            Napier.d(
                tag = TAG,
            ) { "Publishing response event: id=${signedEvent.id.take(8)}..., kind=${signedEvent.kind}" }
            client.publishEvent(signedEvent)
            Napier.d(tag = TAG) { "Response published successfully" }
        }.onFailure {
            Napier.w(tag = TAG, throwable = it) { "Failed to publish NWC response" }
        }
    }

    override fun destroy() {
        Napier.d(tag = TAG) { "NwcService stopping." }
        scope.launch {
            disconnectFromRelay()
        }.invokeOnCompletion {
            scope.cancel()
            Napier.d(tag = TAG) { "NwcService stopped." }
        }
    }

    companion object {
        private const val MAX_CACHE_SIZE = 500
        private val CLEANUP_INTERVAL = 30.seconds
        private const val TAG = "NwcServiceImpl"
    }
}

private data class PendingNwcResponse(
    val request: WalletNwcRequest,
    val responseJson: String,
)
