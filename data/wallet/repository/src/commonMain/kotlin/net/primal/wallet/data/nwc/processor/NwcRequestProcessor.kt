package net.primal.wallet.data.nwc.processor

import io.github.aakira.napier.Napier
import kotlin.math.min
import kotlin.time.Clock
import kotlin.uuid.Uuid
import net.primal.core.networking.nwc.nip47.GetBalanceResponsePayload
import net.primal.core.networking.nwc.nip47.GetInfoResponsePayload
import net.primal.core.networking.nwc.nip47.ListTransactionsResponsePayload
import net.primal.core.networking.nwc.nip47.LookupInvoiceResponsePayload
import net.primal.core.networking.nwc.nip47.MakeInvoiceResponsePayload
import net.primal.core.networking.nwc.nip47.NwcError
import net.primal.core.networking.nwc.nip47.PayInvoiceResponsePayload
import net.primal.core.networking.nwc.wallet.model.WalletNwcRequest
import net.primal.core.utils.CurrencyConversionUtils.btcToMSats
import net.primal.core.utils.CurrencyConversionUtils.formatAsString
import net.primal.core.utils.CurrencyConversionUtils.msatsToSats
import net.primal.core.utils.CurrencyConversionUtils.satsToMSats
import net.primal.core.utils.CurrencyConversionUtils.toBtc
import net.primal.domain.connections.nostr.model.NwcPaymentHoldResult
import net.primal.domain.nostr.InvoiceType
import net.primal.domain.wallet.NwcInvoice
import net.primal.domain.wallet.NwcInvoiceState
import net.primal.domain.wallet.TxRequest
import net.primal.domain.wallet.WalletRepository
import net.primal.domain.wallet.nwc.model.NwcRequestState
import net.primal.wallet.data.nwc.NwcCapabilities
import net.primal.wallet.data.nwc.builder.NwcWalletResponseBuilder
import net.primal.wallet.data.nwc.manager.NwcBudgetManager
import net.primal.wallet.data.nwc.mapper.resolveNwcErrorCode
import net.primal.wallet.data.nwc.mapper.toNwcTransaction
import net.primal.wallet.data.nwc.mapper.toTxType
import net.primal.wallet.data.repository.InternalNwcLogRepository

class NwcRequestProcessor internal constructor(
    private val walletRepository: WalletRepository,
    private val nwcBudgetManager: NwcBudgetManager,
    private val responseBuilder: NwcWalletResponseBuilder,
    private val nwcLogRepository: InternalNwcLogRepository,
) {

    suspend fun process(request: WalletNwcRequest): String {
        Napier.d(
            tag = TAG,
        ) { "Processing request: ${request::class.simpleName}, eventId=${request.eventId.take(8)}..." }

        val requestedAt = Clock.System.now().epochSeconds

        nwcLogRepository.logRequest(
            request = request,
            requestedAt = requestedAt,
        )

        return runCatching {
            when (request) {
                is WalletNwcRequest.GetBalance -> processGetBalance(request)
                is WalletNwcRequest.PayInvoice -> processPayInvoice(request)
                is WalletNwcRequest.GetInfo -> processGetInfo(request)
                is WalletNwcRequest.ListTransactions -> processListTransactions(request)
                is WalletNwcRequest.MakeInvoice -> processMakeInvoice(request)
                is WalletNwcRequest.LookupInvoice -> processLookupInvoice(request)
                else -> {
                    Napier.d(tag = TAG) { "Unsupported method: ${request::class.simpleName}" }
                    responseBuilder.buildErrorResponse(
                        request = request,
                        code = NwcError.NOT_IMPLEMENTED,
                        message = "Method not implemented.",
                    )
                }
            }
        }.fold(
            onSuccess = { responseJson ->
                val nwcError = responseBuilder.parseNwcError(responseJson)
                nwcLogRepository.updateLogWithResponse(
                    eventId = request.eventId,
                    responsePayload = responseJson,
                    requestState = if (nwcError != null) NwcRequestState.Error else NwcRequestState.Success,
                    completedAt = Clock.System.now().epochSeconds,
                    errorCode = nwcError?.code,
                    errorMessage = nwcError?.message,
                )
                responseJson
            },
            onFailure = { e ->
                Napier.e(tag = TAG, throwable = e) { "Error processing request ${request.eventId}" }
                val errorCode = e.resolveNwcErrorCode()
                val errorMessage = e.message ?: "Internal error"
                val responseJson = responseBuilder.buildErrorResponse(
                    request = request,
                    code = errorCode,
                    message = errorMessage,
                )
                nwcLogRepository.updateLogWithResponse(
                    eventId = request.eventId,
                    responsePayload = responseJson,
                    requestState = NwcRequestState.Error,
                    completedAt = Clock.System.now().epochSeconds,
                    errorCode = errorCode,
                    errorMessage = errorMessage,
                )
                responseJson
            },
        )
    }

    private suspend fun processGetBalance(request: WalletNwcRequest.GetBalance): String {
        val wallet = walletRepository.getWalletById(request.connection.walletId).getOrNull()
        val balanceMsats = wallet?.balanceInBtc?.btcToMSats()?.toLong() ?: 0L
        val availableBudgetSats = nwcBudgetManager.getAvailableBudgetSats(request.connection.secretPubKey)
        val effectiveBalanceMsats = availableBudgetSats?.let { min(balanceMsats, it.satsToMSats()) } ?: balanceMsats

        val availableBudgetLog = availableBudgetSats?.toString() ?: "none"
        Napier.d(tag = TAG) {
            "GetBalance: balanceMsats=$balanceMsats, availableBudgetSats=$availableBudgetLog, " +
                "effectiveBalanceMsats=$effectiveBalanceMsats"
        }
        return responseBuilder.buildGetBalanceResponse(
            request = request,
            result = GetBalanceResponsePayload(balance = effectiveBalanceMsats),
        )
    }

    private suspend fun processPayInvoice(request: WalletNwcRequest.PayInvoice): String {
        val connection = request.connection
        val connectionId = connection.secretPubKey
        val walletId = connection.walletId
        val invoice = request.params.invoice

        val amountSats = request.params.amount?.msatsToSats()
            ?: parseInvoiceAmountSats(userId = connection.userId, invoice = invoice)

        Napier.d(tag = TAG) { "PayInvoice: invoice=${invoice.take(20)}..., amountSats=$amountSats" }

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

        val txRequest = TxRequest.Lightning.LnInvoice(
            amountSats = amountSats.toString(),
            noteRecipient = null,
            noteSelf = null,
            lnInvoice = invoice,
            idempotencyKey = Uuid.random().toString(),
        )

        val paymentResult = walletRepository.pay(walletId = walletId, request = txRequest)

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
                code = exception?.resolveNwcErrorCode() ?: NwcError.INTERNAL,
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

    private fun processGetInfo(request: WalletNwcRequest.GetInfo): String {
        Napier.d(tag = TAG) { "GetInfo: returning supported methods" }
        return responseBuilder.buildGetInfoResponse(
            request = request,
            result = GetInfoResponsePayload(
                alias = "Primal Wallet",
                methods = NwcCapabilities.supportedMethods,
                pubkey = request.connection.serviceKeyPair.pubKey,
                network = NwcCapabilities.NETWORK,
            ),
        )
    }

    private suspend fun processListTransactions(request: WalletNwcRequest.ListTransactions): String {
        val params = request.params
        val walletId = request.connection.walletId
        val limit = params.limit ?: DEFAULT_TRANSACTIONS_LIMIT
        val offset = params.offset ?: 0
        val txType = params.type?.toTxType()

        Napier.d(tag = TAG) {
            "ListTransactions: walletId=${walletId.take(8)}..., limit=$limit, " +
                "from=${params.from}, until=${params.until}, type=${params.type}"
        }

        val transactions = walletRepository.queryTransactions(
            walletId = walletId,
            type = txType,
            limit = limit,
            offset = offset,
            from = params.from,
            until = params.until,
        )

        Napier.d(tag = TAG) { "ListTransactions: fetched ${transactions.size} transactions from DB" }

        val nwcTransactions = transactions.map { tx -> tx.toNwcTransaction() }.toList()

        Napier.d(tag = TAG) { "ListTransactions: returning ${nwcTransactions.size} transactions" }

        return responseBuilder.buildListTransactionsResponse(
            request = request,
            result = ListTransactionsResponsePayload(transactions = nwcTransactions),
        )
    }

    private suspend fun processMakeInvoice(request: WalletNwcRequest.MakeInvoice): String {
        val params = request.params
        val walletId = request.connection.walletId
        val amountMsats = params.amount
        val amountSats = amountMsats.msatsToSats()
        val amountBtcFormatted = amountSats.toBtc().formatAsString()

        Napier.d(tag = TAG) {
            "MakeInvoice: walletId=${walletId.take(8)}..., " +
                "amount=$amountMsats msats ($amountSats sats, $amountBtcFormatted BTC), " +
                "description=${params.description}, descriptionHash=${params.descriptionHash}"
        }

        // First verify the wallet exists and log its type
        val walletResult = walletRepository.getWalletById(walletId)
        if (walletResult.isFailure) {
            Napier.e(tag = TAG) { "MakeInvoice: wallet not found for walletId=$walletId" }
            return responseBuilder.buildErrorResponse(
                request = request,
                code = NwcError.INTERNAL,
                message = "Wallet not found. Please reconnect the NWC connection.",
            )
        }

        val wallet = walletResult.getOrThrow()
        Napier.d(tag = TAG) { "MakeInvoice: wallet type=${wallet::class.simpleName}, creating invoice..." }

        val result = walletRepository.createLightningInvoice(
            walletId = walletId,
            amountInBtc = amountBtcFormatted,
            comment = params.description,
            expiry = params.expiry,
        )

        return if (result.isSuccess) {
            val invoiceResult = result.getOrThrow()
            val invoice = invoiceResult.invoice

            Napier.d(tag = TAG) { "MakeInvoice: invoice created: ${invoice.take(50)}..." }

            val parseResult = runCatching {
                walletRepository.parseLnInvoice(
                    userId = request.connection.userId,
                    lnbc = invoice,
                )
            }.getOrNull()

            val nowSeconds = Clock.System.now().epochSeconds
            val expiresAtSeconds = parseResult?.expiry?.let { nowSeconds + it }
                ?: params.expiry?.let { nowSeconds + it }

            Napier.d(tag = TAG) {
                "MakeInvoice: success! paymentHash=${parseResult?.paymentHash?.take(16)}, " +
                    "expiry=${parseResult?.expiry}s, expiresAt=$expiresAtSeconds"
            }

            runCatching {
                walletRepository.persistNwcInvoice(
                    NwcInvoice(
                        paymentHash = parseResult?.paymentHash,
                        walletId = walletId,
                        connectionId = request.connection.secretPubKey,
                        invoice = invoice,
                        description = params.description ?: invoiceResult.description,
                        descriptionHash = params.descriptionHash,
                        amountMsats = amountMsats,
                        createdAt = nowSeconds,
                        expiresAt = expiresAtSeconds ?: (nowSeconds + DEFAULT_INVOICE_EXPIRY_SECONDS),
                        settledAt = null,
                        preimage = null,
                        state = NwcInvoiceState.PENDING,
                    ),
                )
                Napier.d(tag = TAG) { "MakeInvoice: persisted NwcInvoice for lookup" }
            }.onFailure { e ->
                Napier.e(tag = TAG, throwable = e) { "MakeInvoice: failed to persist NwcInvoice" }
            }

            responseBuilder.buildMakeInvoiceResponse(
                request = request,
                result = MakeInvoiceResponsePayload(
                    type = "incoming",
                    invoice = invoice,
                    description = params.description ?: invoiceResult.description,
                    descriptionHash = params.descriptionHash,
                    paymentHash = parseResult?.paymentHash ?: "",
                    amount = amountMsats,
                    feesPaid = 0,
                    createdAt = nowSeconds,
                    expiresAt = expiresAtSeconds,
                    metadata = null,
                    state = "pending",
                ),
            )
        } else {
            val exception = result.exceptionOrNull()
            val errorMessage = exception?.message ?: "Failed to create invoice"
            Napier.e(tag = TAG, throwable = exception) { "MakeInvoice: $errorMessage" }
            responseBuilder.buildErrorResponse(
                request = request,
                code = exception?.resolveNwcErrorCode() ?: NwcError.INTERNAL,
                message = errorMessage,
            )
        }
    }

    private suspend fun processLookupInvoice(request: WalletNwcRequest.LookupInvoice): String {
        val params = request.params
        val invoice = params.invoice
        val paymentHash = params.paymentHash
        val walletId = request.connection.walletId

        Napier.d(tag = TAG) {
            "LookupInvoice: walletId=${walletId.take(8)}..., " +
                "invoice=${invoice?.take(30)}..., " +
                "paymentHash=$paymentHash"
        }

        if (invoice == null && paymentHash == null) {
            Napier.w(tag = TAG) { "LookupInvoice: both invoice and paymentHash are null" }
            return responseBuilder.buildErrorResponse(
                request = request,
                code = NwcError.OTHER,
                message = "Either invoice or payment_hash is required",
            )
        }

        val nwcInvoice = when {
            invoice != null -> walletRepository.findNwcInvoiceByInvoice(invoice)
            paymentHash != null -> walletRepository.findNwcInvoiceByPaymentHash(paymentHash)
            else -> null
        }

        if (nwcInvoice != null) {
            Napier.d(tag = TAG) { "LookupInvoice: found NwcInvoice, state=${nwcInvoice.state}" }
            return responseBuilder.buildLookupInvoiceResponse(
                request = request,
                result = LookupInvoiceResponsePayload(
                    type = InvoiceType.Incoming,
                    invoice = nwcInvoice.invoice,
                    description = nwcInvoice.description,
                    descriptionHash = nwcInvoice.descriptionHash,
                    paymentHash = nwcInvoice.paymentHash ?: "",
                    preimage = nwcInvoice.preimage,
                    amount = nwcInvoice.amountMsats,
                    feesPaid = 0,
                    createdAt = nwcInvoice.createdAt,
                    expiresAt = nwcInvoice.expiresAt,
                    settledAt = nwcInvoice.settledAt,
                    state = nwcInvoice.state.name.lowercase(),
                ),
            )
        }

        Napier.d(tag = TAG) { "LookupInvoice: NwcInvoice not found, searching transactions..." }

        val matchingTransaction = when {
            invoice != null -> walletRepository.findTransactionByInvoice(invoice)
            paymentHash != null -> walletRepository.findTransactionByPaymentHash(paymentHash)
            else -> null
        }

        return if (matchingTransaction != null) {
            Napier.d(tag = TAG) { "LookupInvoice: found matching transaction" }
            responseBuilder.buildLookupInvoiceResponse(
                request = request,
                result = matchingTransaction.toNwcTransaction(),
            )
        } else {
            Napier.w(tag = TAG) { "LookupInvoice: invoice not found" }
            responseBuilder.buildErrorResponse(
                request = request,
                code = NwcError.NOT_FOUND,
                message = "Invoice not found",
            )
        }
    }

    companion object {
        private const val PAYMENT_HOLD_TIMEOUT_MS = 60_000L
        private const val DEFAULT_TRANSACTIONS_LIMIT = 50
        private const val DEFAULT_INVOICE_EXPIRY_SECONDS = 3600L

        private const val TAG = "NwcRequestProcessor"
    }
}
