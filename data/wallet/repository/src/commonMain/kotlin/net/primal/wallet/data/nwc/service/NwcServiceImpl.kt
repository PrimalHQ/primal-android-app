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
import net.primal.core.networking.nwc.nip47.GetInfoResponsePayload
import net.primal.core.networking.nwc.nip47.ListTransactionsResponsePayload
import net.primal.core.networking.nwc.nip47.NwcError
import net.primal.core.networking.nwc.nip47.NwcMethod
import net.primal.core.networking.nwc.nip47.PayInvoiceResponsePayload
import net.primal.core.networking.nwc.wallet.NwcWalletClient
import net.primal.core.networking.nwc.wallet.NwcWalletRequestParser
import net.primal.core.networking.nwc.wallet.model.WalletNwcRequest
import net.primal.core.networking.nwc.wallet.signNwcResponseNostrEvent
import net.primal.core.utils.CurrencyConversionUtils.toSats
import net.primal.core.utils.batchOnInactivity
import net.primal.core.utils.cache.LruSeenCache
import net.primal.core.utils.coroutines.DispatcherProvider
import net.primal.domain.account.WalletAccountRepository
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
    private val walletAccountRepository: WalletAccountRepository,
) : NwcService {

    private val scope = CoroutineScope(dispatchers.io() + SupervisorJob())
    private val cache: LruSeenCache<String> = LruSeenCache(maxEntries = MAX_CACHE_SIZE)
    private val retrySendResponseQueue = MutableSharedFlow<PendingNwcResponse>()

    private val clientMutex = Mutex()
    private var nwcClient: NwcWalletClient? = null
    private var clientJob: Job? = null

    override fun initialize(userId: String) {
        Napier.d(tag = TAG) { "NwcService initializing for userId=$userId" }
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
                } else {
                    Napier.d(tag = TAG) { "No connections found, disconnecting from relay" }
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
                        sendResponse(pending.request, pending.responseJson)
                    }
                }
        }

    private fun processRequest(request: WalletNwcRequest) =
        scope.launch {
            Napier.d(
                tag = TAG,
            ) { "Processing request: ${request::class.simpleName}, eventId=${request.eventId.take(8)}..." }

            // 1. Budget & Permission Logic
            // check permissions here.
            // val canSpend = nwcBudgetManager.canSpend(...)

            // 2. Build Response
            val response = runCatching {
                when (request) {
                    is WalletNwcRequest.GetBalance -> {
                        val wallet = walletAccountRepository.getActiveWallet(request.connection.userId)
                        val balanceSats = wallet?.balanceInBtc?.toSats()?.toLong() ?: 0L
                        Napier.d(tag = TAG) { "GetBalance: balanceSats=$balanceSats (${balanceSats * 1000} msat)" }
                        responseBuilder.buildGetBalanceResponse(
                            request = request,
                            result = GetBalanceResponsePayload(balance = balanceSats * 1000),
                        )
                    }
                    is WalletNwcRequest.PayInvoice -> {
                        // TODO: Implement real payment via WalletRepository
                        val preimage = "mock_preimage"
                        Napier.d(tag = TAG) { "PayInvoice: returning mock preimage" }
                        responseBuilder.buildPayInvoiceResponse(
                            request = request,
                            result = PayInvoiceResponsePayload(preimage = preimage),
                        )
                    }
                    is WalletNwcRequest.GetInfo -> {
                        Napier.d(tag = TAG) { "GetInfo: returning supported methods" }
                        responseBuilder.buildGetInfoResponse(
                            request = request,
                            result = GetInfoResponsePayload(
                                alias = "Primal Wallet",
                                methods = listOf(
                                    NwcMethod.GetInfo.value,
                                    NwcMethod.GetBalance.value,
                                    NwcMethod.PayInvoice.value,
                                    NwcMethod.MakeInvoice.value,
                                    NwcMethod.LookupInvoice.value,
                                    NwcMethod.ListTransactions.value,
                                ),
                            ),
                        )
                    }
                    is WalletNwcRequest.ListTransactions -> {
                        // TODO: Implement real transaction history
                        Napier.d(tag = TAG) { "ListTransactions: returning empty list" }
                        responseBuilder.buildListTransactionsResponse(
                            request = request,
                            result = ListTransactionsResponsePayload(transactions = emptyList()),
                        )
                    }
                    is WalletNwcRequest.MakeInvoice -> {
                        Napier.d(tag = TAG) { "MakeInvoice: not implemented" }
                        responseBuilder.buildErrorResponse(
                            request = request,
                            code = NwcError.NOT_IMPLEMENTED,
                            message = "make_invoice is not yet supported",
                        )
                    }
                    is WalletNwcRequest.LookupInvoice -> {
                        Napier.d(tag = TAG) { "LookupInvoice: not implemented" }
                        responseBuilder.buildErrorResponse(
                            request = request,
                            code = NwcError.NOT_IMPLEMENTED,
                            message = "lookup_invoice is not yet supported",
                        )
                    }
                    else -> {
                        Napier.d(tag = TAG) { "Unsupported method: ${request::class.simpleName}" }
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
}

private data class PendingNwcResponse(
    val request: WalletNwcRequest,
    val responseJson: String,
)
