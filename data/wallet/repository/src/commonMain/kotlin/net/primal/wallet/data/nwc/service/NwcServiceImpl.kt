package net.primal.wallet.data.nwc.service

import io.github.aakira.napier.Napier
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import net.primal.core.networking.nwc.nip47.GetBalanceResponsePayload
import net.primal.core.networking.nwc.nip47.NwcError
import net.primal.core.networking.nwc.nip47.NwcResponseContent
import net.primal.core.networking.nwc.nip47.PayInvoiceResponsePayload
import net.primal.core.networking.nwc.wallet.NwcWalletClient
import net.primal.core.networking.nwc.wallet.NwcWalletRequestParser
import net.primal.core.networking.nwc.wallet.model.WalletNwcRequest
import net.primal.core.networking.nwc.wallet.signNwcResponseNostrEvent
import net.primal.core.utils.batchOnInactivity
import net.primal.core.utils.cache.LruSeenCache
import net.primal.core.utils.coroutines.DispatcherProvider
import net.primal.domain.connections.nostr.NwcRepository
import net.primal.domain.connections.nostr.NwcService
import net.primal.domain.nostr.cryptography.utils.unwrapOrThrow
import net.primal.wallet.data.nwc.builder.NwcWalletResponseBuilder
import net.primal.wallet.data.nwc.manager.NwcBudgetManager

private const val MAX_CACHE_SIZE = 20
private const val TAG = "NwcServiceImpl"

class NwcServiceImpl internal constructor(
    private val dispatchers: DispatcherProvider,
    private val nwcBudgetManager: NwcBudgetManager,
    private val nwcRepository: NwcRepository,
    private val requestParser: NwcWalletRequestParser,
    private val responseBuilder: NwcWalletResponseBuilder,
) : NwcService {

    private val scope = CoroutineScope(dispatchers.io() + SupervisorJob())
    private val cache: LruSeenCache<String> = LruSeenCache(maxEntries = MAX_CACHE_SIZE)
    private val retrySendResponseQueue = MutableSharedFlow<PendingNwcResponse>()

    private val clientMutex = Mutex()
    private var nwcClient: NwcWalletClient? = null
    private var clientJob: Job? = null

    override fun initialize(userId: String) {
        Napier.d(tag = TAG) { "NwcService initializing..." }
        observeConnections(userId)
        observeRetrySendResponseQueue()
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
            Napier.d(tag = TAG) { "Disconnected from NWC relay." }
        }

    private fun observeConnections(userId: String) =
        scope.launch {
            nwcRepository.observeConnections(userId = userId).collect { connections ->
                Napier.d(tag = TAG) { "Connections updated: ${connections.size} connection(s)" }
                if (connections.isNotEmpty()) {
                    val relayUrl = connections.first().relay
                    connectToRelay(relayUrl)
                    runCatching { nwcClient?.updateConnections(connections) }
                        .onFailure {
                            Napier.w(tag = TAG, throwable = it) { "Failed to update connections." }
                        }
                } else {
                    disconnectFromRelay()
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
                Napier.d(tag = TAG) { "Received NWC request: ${request.eventId}" }
                processRequest(request)
                cache.mark(request.eventId)
            }
        }

    private fun observeErrors(client: NwcWalletClient) =
        scope.launch {
            client.errors.collect { error ->
                if (cache.seen(error.nostrEvent.id)) return@collect
                Napier.w(tag = TAG, throwable = error.cause) {
                    "NWC request error for event: ${error.nostrEvent.id}"
                }
                cache.mark(error.nostrEvent.id)
            }
        }

    @OptIn(FlowPreview::class)
    private fun observeRetrySendResponseQueue() =
        scope.launch {
            retrySendResponseQueue
                .batchOnInactivity(inactivityTimeout = 3.seconds)
                .collect { batchedResponses ->
                    batchedResponses.forEach { pending ->
                        sendResponse(pending.request, pending.content)
                    }
                }
        }

    private fun processRequest(request: WalletNwcRequest) =
        scope.launch {
            // 1. Budget & Permission Logic
            // check permissions here.
            // val canSpend = nwcBudgetManager.canSpend(...)

            // 2. Build Response
            val response = runCatching {
                when (request) {
                    is WalletNwcRequest.GetBalance -> {
                        // Mock balance logic
                        val balance = 0L
                        responseBuilder.buildSuccessResponse(
                            request = request,
                            result = GetBalanceResponsePayload(balance = balance * 1000),
                        )
                    }
                    is WalletNwcRequest.PayInvoice -> {
                        // Mock payment logic
                        val preimage = "mock_preimage"
                        responseBuilder.buildSuccessResponse(
                            request = request,
                            result = PayInvoiceResponsePayload(preimage = preimage),
                        )
                    }
                    else -> {
                        responseBuilder.buildErrorResponse(
                            request = request,
                            code = NwcError.NOT_IMPLEMENTED,
                            message = "Method not implemented.",
                        )
                    }
                }
            }.getOrElse { e ->
                Napier.e(tag = TAG, throwable = e) { "Error processing request ${request.eventId}" }
                responseBuilder.buildErrorResponse(
                    request = request,
                    code = NwcError.INTERNAL,
                    message = e.message ?: "Internal error",
                )
            }

            // 3. Send Response
            sendResponseOrAddToRetryQueue(request, response)
        }

    private suspend fun sendResponseOrAddToRetryQueue(
        request: WalletNwcRequest,
        responseContent: NwcResponseContent<out Any?>,
    ) {
        sendResponse(request, responseContent).onFailure {
            Napier.d(tag = TAG) { "Adding response to retry queue: ${request.eventId}" }
            retrySendResponseQueue.emit(PendingNwcResponse(request, responseContent))
        }
    }

    private suspend fun sendResponse(
        request: WalletNwcRequest,
        responseContent: NwcResponseContent<out Any?>,
    ): Result<Unit> {
        return runCatching {
            val client = nwcClient ?: error("NwcWalletClient is not initialized")
            val signedEvent = signNwcResponseNostrEvent(
                request = request,
                response = responseContent,
            ).unwrapOrThrow()

            client.publishEvent(signedEvent)
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
}

private data class PendingNwcResponse(
    val request: WalletNwcRequest,
    val content: NwcResponseContent<out Any?>,
)
