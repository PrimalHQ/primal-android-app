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
import net.primal.core.utils.CurrencyConversionUtils.msatsToSats
import net.primal.core.utils.CurrencyConversionUtils.satsToMSats
import net.primal.core.utils.CurrencyConversionUtils.toSats
import net.primal.core.utils.batchOnInactivity
import net.primal.core.utils.cache.LruSeenCache
import net.primal.core.utils.coroutines.DispatcherProvider
import net.primal.domain.connections.nostr.NwcRepository
import net.primal.domain.connections.nostr.NwcService
import net.primal.domain.connections.nostr.model.NwcPaymentHoldResult
import net.primal.domain.nostr.cryptography.utils.unwrapOrThrow
import net.primal.domain.wallet.TxRequest
import net.primal.domain.wallet.WalletRepository
import net.primal.wallet.data.nwc.builder.NwcWalletResponseBuilder
import net.primal.wallet.data.nwc.manager.NwcBudgetManager

private const val MAX_CACHE_SIZE = 20
private const val TAG = "NwcServiceImpl"
private const val PAYMENT_HOLD_TIMEOUT_MS = 60_000L

class NwcServiceImpl internal constructor(
    private val dispatchers: DispatcherProvider,
    private val nwcBudgetManager: NwcBudgetManager,
    private val nwcRepository: NwcRepository,
    private val requestParser: NwcWalletRequestParser,
    private val responseBuilder: NwcWalletResponseBuilder,
    private val walletRepository: WalletRepository,
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

            val response = runCatching {
                when (request) {
                    is WalletNwcRequest.GetBalance -> {
                        val walletResult = walletRepository.getWalletById(request.connection.walletId)
                        val wallet = walletResult.getOrNull()
                        val balanceSats = wallet?.balanceInBtc?.toSats()?.toLong() ?: 0L
                        val balanceMsats = balanceSats.satsToMSats()
                        Napier.d(tag = TAG) { "GetBalance: balanceSats=$balanceSats ($balanceMsats msat)" }
                        responseBuilder.buildGetBalanceResponse(
                            request = request,
                            result = GetBalanceResponsePayload(balance = balanceMsats),
                        )
                    }
                    is WalletNwcRequest.PayInvoice -> {
                        processPayInvoice(request)
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

            Napier.d(tag = TAG) { "Sending response for eventId=${request.eventId.take(8)}..." }
            sendResponseOrAddToRetryQueue(request, response)
        }

    private suspend fun processPayInvoice(request: WalletNwcRequest.PayInvoice): String {
        val connection = request.connection
        val connectionId = connection.secretPubKey
        val walletId = connection.walletId
        val invoice = request.params.invoice

        // Get amount from request params, or parse from invoice if not provided
        val amountSats = request.params.amount?.msatsToSats()
            ?: parseInvoiceAmountSats(userId = connection.userId, invoice = invoice)

        Napier.d(tag = TAG) { "PayInvoice: invoice=${invoice.take(20)}..., amountSats=$amountSats" }

        // 1. Check and enforce budget if configured
        var holdId: String? = null
        if (amountSats > 0 && nwcBudgetManager.hasBudgetLimit(connectionId)) {
            when (
                val holdResult = nwcBudgetManager.placeHold(
                    connectionId = connectionId,
                    amountSats = amountSats,
                    requestId = request.eventId,
                    timeoutMs = PAYMENT_HOLD_TIMEOUT_MS,
                )
            ) {
                is NwcPaymentHoldResult.Placed -> {
                    holdId = holdResult.holdId
                    Napier.d(tag = TAG) {
                        "PayInvoice: budget hold placed, holdId=$holdId, remaining=${holdResult.remainingBudget}"
                    }
                }

                is NwcPaymentHoldResult.InsufficientBudget -> {
                    Napier.w(tag = TAG) {
                        "PayInvoice: insufficient budget, " +
                            "requested=${holdResult.requested}, available=${holdResult.available}"
                    }
                    return responseBuilder.buildErrorResponse(
                        request = request,
                        code = NwcError.QUOTA_EXCEEDED,
                        message = "Daily budget exceeded. Requested: ${holdResult.requested} sats, " +
                            "available: ${holdResult.available} sats",
                    )
                }
            }
        }

        // 2. Execute payment
        val txRequest = TxRequest.Lightning.LnInvoice(
            amountSats = amountSats.toString(),
            noteRecipient = null,
            noteSelf = null,
            lnInvoice = invoice,
        )

        val paymentResult = walletRepository.pay(walletId = walletId, request = txRequest)

        // 3. Handle result and manage budget hold
        return if (paymentResult.isSuccess) {
            holdId?.let { id -> nwcBudgetManager.commitHold(id, amountSats) }
            Napier.d(tag = TAG) { "PayInvoice: payment successful" }
            responseBuilder.buildPayInvoiceResponse(
                request = request,
                result = PayInvoiceResponsePayload(preimage = null),
            )
        } else {
            val exception = paymentResult.exceptionOrNull()
            holdId?.let { id -> nwcBudgetManager.releaseHold(id) }
            Napier.e(tag = TAG, throwable = exception) { "PayInvoice: payment failed" }
            responseBuilder.buildErrorResponse(
                request = request,
                code = NwcError.INTERNAL,
                message = exception?.message ?: "Payment failed",
            )
        }
    }

    private suspend fun parseInvoiceAmountSats(userId: String, invoice: String): Long {
        return runCatching {
            val parseResult = walletRepository.parseLnInvoice(userId = userId, lnbc = invoice)
            (parseResult.amountMilliSats?.toLong() ?: 0L) / 1000L
        }.getOrElse {
            Napier.w(tag = TAG, throwable = it) { "Failed to parse invoice amount" }
            0L
        }
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
