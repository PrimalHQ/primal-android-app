package net.primal.wallet.data.service

import kotlin.time.Duration
import kotlin.uuid.Uuid
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.serialization.json.jsonObject
import net.primal.core.lightning.LightningPayHelper
import net.primal.core.networking.nwc.NwcClientFactory
import net.primal.core.networking.nwc.nip47.ListTransactionsParams
import net.primal.core.networking.nwc.nip47.LookupInvoiceResponsePayload
import net.primal.core.networking.nwc.nip47.MakeInvoiceParams
import net.primal.core.networking.nwc.nip47.NwcError
import net.primal.core.networking.nwc.nip47.NwcException
import net.primal.core.networking.nwc.nip47.PayInvoiceParams
import net.primal.core.utils.CurrencyConversionUtils.btcToMSats
import net.primal.core.utils.CurrencyConversionUtils.formatAsString
import net.primal.core.utils.CurrencyConversionUtils.msatsToBtc
import net.primal.core.utils.CurrencyConversionUtils.satsToMSats
import net.primal.core.utils.Result
import net.primal.core.utils.map
import net.primal.core.utils.mapCatching
import net.primal.core.utils.mapFailure
import net.primal.core.utils.runCatching
import net.primal.core.utils.serialization.decodeFromJsonStringOrNull
import net.primal.domain.common.exception.NetworkException
import net.primal.domain.events.EventRepository
import net.primal.domain.nostr.InvoiceType
import net.primal.domain.nostr.NostrEvent
import net.primal.domain.nostr.findFirstProfileId
import net.primal.domain.rates.fees.OnChainTransactionFeeTier
import net.primal.domain.transactions.Transaction
import net.primal.domain.wallet.LightningPaymentResult
import net.primal.domain.wallet.LnInvoiceCreateRequest
import net.primal.domain.wallet.LnInvoiceCreateResult
import net.primal.domain.wallet.NostrWalletConnect
import net.primal.domain.wallet.OnChainAddressResult
import net.primal.domain.wallet.PayResult
import net.primal.domain.wallet.TransactionsPage
import net.primal.domain.wallet.TransactionsRequest
import net.primal.domain.wallet.TxRequest
import net.primal.domain.wallet.TxState
import net.primal.domain.wallet.TxType
import net.primal.domain.wallet.Wallet
import net.primal.domain.wallet.WalletType
import net.primal.domain.wallet.exception.WalletConnectionException
import net.primal.domain.wallet.exception.WalletException
import net.primal.domain.wallet.exception.WalletPaymentException
import net.primal.domain.wallet.model.WalletBalanceResult
import net.primal.wallet.data.repository.mappers.remote.toNostrEntity

internal class NostrWalletServiceImpl(
    private val eventRepository: EventRepository,
    private val lightningPayHelper: LightningPayHelper,
) : WalletService<Wallet.NWC> {

    private companion object {
        private const val MAX_SAME_TIMESTAMP_PAGES = 10
        private const val DEFAULT_PAGE_SIZE = 50
    }

    private fun Throwable.toWalletException(): WalletException {
        return when (this) {
            is WalletException -> this
            is NwcException -> when (this.errorCode) {
                NwcError.INSUFFICIENT_BALANCE -> WalletPaymentException.InsufficientBalance(cause = this)
                NwcError.RATE_LIMITED -> WalletConnectionException.RateLimited(cause = this)
                NwcError.QUOTA_EXCEEDED -> WalletConnectionException.QuotaExceeded(cause = this)
                NwcError.UNAUTHORIZED -> WalletConnectionException.Unauthorized(cause = this)
                NwcError.RESTRICTED -> WalletConnectionException.Unauthorized(cause = this)
                NwcError.NOT_IMPLEMENTED -> WalletPaymentException.OperationNotSupported(
                    operation = this.message ?: "Unknown operation",
                    cause = this,
                )

                NwcError.INTERNAL, NwcError.OTHER -> WalletPaymentException.PaymentFailed(
                    reason = this.message ?: "Unknown error",
                    cause = this,
                )

                else -> WalletPaymentException.PaymentFailed(
                    reason = this.message ?: "Unknown error",
                    cause = this,
                )
            }

            is NetworkException -> WalletException.WalletNetworkException(cause = this)
            else -> WalletPaymentException.PaymentFailed(
                reason = this.message ?: "Unknown error",
                cause = this,
            )
        }
    }

    override suspend fun fetchWalletBalance(wallet: Wallet.NWC): Result<WalletBalanceResult> =
        runCatching {
            val client = createNwcApiClient(wallet = wallet)
            val response = client.getBalance().getOrThrow()

            WalletBalanceResult(
                balanceInBtc = response.balance.msatsToBtc(),
                maxBalanceInBtc = null,
            )
        }.mapFailure { it.toWalletException() }

    override suspend fun subscribeToWalletBalance(wallet: Wallet.NWC): Flow<WalletBalanceResult> {
        return emptyFlow()
    }

    override suspend fun fetchTransactions(
        wallet: Wallet.NWC,
        request: TransactionsRequest,
    ): Result<TransactionsPage> =
        runCatching {
            val limit = request.limit ?: DEFAULT_PAGE_SIZE

            val firstBatch = fetchNwcTransactions(
                wallet = wallet,
                until = request.until,
                from = request.since,
                offset = request.offset,
                limit = limit,
                unpaid = request.unpaid,
                type = request.type,
            )

            val allTransactions: List<Transaction>
            val nextCursor: Long?

            if (firstBatch.size < limit) {
                allTransactions = firstBatch
                nextCursor = null
            } else {
                val minCreatedAt = firstBatch.minOfOrNull { it.createdAt }!!
                val allSameTimestamp = firstBatch.all { it.createdAt == minCreatedAt }

                if (allSameTimestamp) {
                    // Same-timestamp edge case: use offset to exhaust this timestamp
                    val accumulated = firstBatch.toMutableList()
                    var offset = limit
                    while (offset < limit * MAX_SAME_TIMESTAMP_PAGES) {
                        val innerBatch = fetchNwcTransactions(
                            wallet = wallet,
                            // Clamp to exact timestamp: NIP-47 from/until are both INCLUSIVE
                            until = minCreatedAt,
                            from = minCreatedAt,
                            offset = offset,
                            limit = limit,
                            unpaid = request.unpaid,
                            type = request.type,
                        )
                        accumulated.addAll(innerBatch)
                        offset += limit
                        if (innerBatch.size < limit) break
                    }
                    allTransactions = accumulated
                    // NIP-47 until is inclusive, so T-1 to skip the exhausted timestamp
                    nextCursor = minCreatedAt - 1
                } else {
                    allTransactions = firstBatch
                    nextCursor = minCreatedAt
                }
            }

            TransactionsPage(
                transactions = allTransactions,
                nextCursor = nextCursor,
            )
        }.mapFailure { it.toWalletException() }

    private suspend fun fetchNwcTransactions(
        wallet: Wallet.NWC,
        until: Long?,
        from: Long?,
        offset: Int?,
        limit: Int,
        unpaid: Boolean?,
        type: InvoiceType?,
    ): List<Transaction> {
        val client = createNwcApiClient(wallet = wallet)

        val response = client.listTransactions(
            params = ListTransactionsParams(
                from = from,
                until = until,
                limit = limit,
                offset = offset,
                unpaid = unpaid,
                type = type,
            ),
        ).getOrThrow()

        val invoices = response.transactions.mapNotNull { it.invoice }
        val zapReceiptsMap = eventRepository.getZapReceipts(invoices = invoices).getOrNull()

        return response.transactions.map { transaction ->
            val zapRequest = (transaction.metadata?.get("nostr") ?: transaction.metadata?.get("zap_request"))
                ?.jsonObject?.toString()
                ?.decodeFromJsonStringOrNull<NostrEvent>()
                ?: zapReceiptsMap?.get(transaction.invoice)

            val zappedEntity = zapRequest?.toNostrEntity()

            val txType = when (transaction.type) {
                InvoiceType.Incoming -> TxType.DEPOSIT
                InvoiceType.Outgoing -> TxType.WITHDRAW
            }
            val txState = transaction.resolveState()
            val transactionId = transaction.paymentHash ?: transaction.invoice ?: Uuid.random().toString()
            val note = zapRequest?.content ?: transaction.description
            ?: transaction.metadata?.get("comment")?.toString()
            val otherUserId = when (transaction.type) {
                InvoiceType.Incoming -> zapRequest?.pubKey
                InvoiceType.Outgoing -> zapRequest?.tags?.findFirstProfileId()
            }

            if (zappedEntity != null) {
                Transaction.Zap(
                    transactionId = transactionId,
                    walletId = wallet.walletId,
                    walletType = WalletType.NWC,
                    type = txType,
                    state = txState,
                    createdAt = transaction.createdAt,
                    updatedAt = transaction.settledAt ?: transaction.createdAt,
                    completedAt = transaction.settledAt,
                    userId = wallet.userId,
                    note = note,
                    invoice = transaction.invoice,
                    amountInBtc = transaction.amount.msatsToBtc(),
                    amountInUsd = null,
                    exchangeRate = null,
                    totalFeeInBtc = transaction.feesPaid.msatsToBtc().formatAsString(),
                    zappedEntity = zappedEntity,
                    otherUserId = otherUserId,
                    otherLightningAddress = null,
                    zappedByUserId = zapRequest?.pubKey,
                    otherUserProfile = null,
                    preimage = transaction.preimage,
                    paymentHash = transaction.paymentHash,
                )
            } else {
                Transaction.Lightning(
                    transactionId = transactionId,
                    walletId = wallet.walletId,
                    walletType = WalletType.NWC,
                    type = txType,
                    state = txState,
                    createdAt = transaction.createdAt,
                    updatedAt = transaction.settledAt ?: transaction.createdAt,
                    completedAt = transaction.settledAt,
                    userId = wallet.userId,
                    note = note,
                    invoice = transaction.invoice,
                    amountInBtc = transaction.amount.msatsToBtc(),
                    amountInUsd = null,
                    exchangeRate = null,
                    totalFeeInBtc = transaction.feesPaid.msatsToBtc().formatAsString(),
                    otherUserId = otherUserId,
                    otherLightningAddress = null,
                    otherUserProfile = null,
                    preimage = transaction.preimage,
                    paymentHash = transaction.paymentHash,
                )
            }
        }
    }

    override suspend fun createLightningInvoice(
        wallet: Wallet.NWC,
        request: LnInvoiceCreateRequest,
    ): Result<LnInvoiceCreateResult> =
        runCatching {
            val amountInBtc = request.amountInBtc
            requireNotNull(amountInBtc) { "Amount is required for NWC lightning invoices." }

            val client = createNwcApiClient(wallet = wallet)
            client.makeInvoice(
                params = MakeInvoiceParams(
                    amount = amountInBtc.toDouble().btcToMSats().toLong(),
                    description = request.description,
                    descriptionHash = request.descriptionHash,
                    expiry = request.expiry,
                ),
            ).map { result ->
                LnInvoiceCreateResult(
                    invoice = result.invoice ?: throw IllegalArgumentException("Didn't receive invoice in response."),
                    description = result.description,
                )
            }.getOrThrow()
        }.mapFailure { it.toWalletException() }

    override suspend fun createOnChainAddress(wallet: Wallet.NWC): Result<OnChainAddressResult> {
        return Result.failure(
            WalletPaymentException.OperationNotSupported(
                operation = "On-chain address creation is not supported with NWC.",
            ),
        )
    }

    override suspend fun pay(wallet: Wallet.NWC, request: TxRequest): Result<PayResult> =
        runCatching {
            require(request is TxRequest.Lightning) { "Only lightning transactions are supported through NWC wallet." }

            val client = createNwcApiClient(wallet = wallet)

            val amountInMilliSats = request.amountSats.toLong().satsToMSats().toULong()
            val lnInvoice = when (request) {
                is TxRequest.Lightning.LnInvoice -> request.lnInvoice
                is TxRequest.Lightning.LnUrl -> resolveLnInvoice(
                    lnUrl = request.lnUrl,
                    amountInMilliSats = amountInMilliSats,
                    comment = request.noteRecipient,
                ).getOrThrow()
            }

            return client.payInvoice(
                params = PayInvoiceParams(
                    invoice = lnInvoice,
                    amount = amountInMilliSats.toLong(),
                ),
            ).mapFailure { it.toWalletException() }
                .map { PayResult(preimage = it.preimage, feesPaid = it.feesPaid) }
        }

    private suspend fun resolveLnInvoice(
        lnUrl: String,
        amountInMilliSats: ULong,
        comment: String?,
    ): Result<String> =
        runCatching {
            lightningPayHelper.fetchPayRequest(lnUrl = lnUrl)
        }.mapCatching {
            lightningPayHelper.fetchInvoice(
                payRequest = it,
                amountInMilliSats = amountInMilliSats,
                comment = comment?.take(it.commentAllowed) ?: "",
            )
        }.map { it.invoice }

    private fun LookupInvoiceResponsePayload.resolveState(): TxState =
        when {
            settledAt != null -> TxState.SUCCEEDED

            else -> TxState.CREATED
        }

    override suspend fun fetchMiningFees(
        wallet: Wallet.NWC,
        onChainAddress: String,
        amountInBtc: String,
    ): Result<List<OnChainTransactionFeeTier>> {
        return Result.failure(
            WalletPaymentException.OperationNotSupported(
                operation = "Mining fees are not supported for NWC wallets.",
            ),
        )
    }

    override suspend fun awaitLightningPayment(
        wallet: Wallet.NWC,
        invoice: String?,
        timeout: Duration,
    ): Result<LightningPaymentResult> = Result.failure(UnsupportedOperationException("Not supported for NWC wallet"))

    private fun createNwcApiClient(wallet: Wallet.NWC) =
        NwcClientFactory.createNwcApiClient(
            nwcData = NostrWalletConnect(
                lightningAddress = wallet.lightningAddress,
                relays = wallet.relays,
                pubkey = wallet.pubkey,
                keypair = wallet.keypair,
            ),
        )
}
