package net.primal.wallet.data.service

import kotlin.uuid.ExperimentalUuidApi
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
import net.primal.domain.wallet.LnInvoiceCreateRequest
import net.primal.domain.wallet.LnInvoiceCreateResult
import net.primal.domain.wallet.NostrWalletConnect
import net.primal.domain.wallet.OnChainAddressResult
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

    @OptIn(ExperimentalUuidApi::class)
    override suspend fun fetchTransactions(
        wallet: Wallet.NWC,
        request: TransactionsRequest,
    ): Result<List<Transaction>> =
        runCatching {
            val client = createNwcApiClient(wallet = wallet)

            client.listTransactions(
                params = ListTransactionsParams(
                    from = request.since,
                    until = request.until,
                    limit = request.limit,
                    unpaid = request.unpaid,
                    type = request.type,
                ),
            ).map { response ->
                val invoices = response.transactions.mapNotNull { it.invoice }

                val zapReceiptsMap = eventRepository.getZapReceipts(invoices = invoices).getOrNull()

                response.transactions.map { transaction ->
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
            }.getOrThrow()
        }.mapFailure { it.toWalletException() }

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

    override suspend fun pay(wallet: Wallet.NWC, request: TxRequest): Result<Unit> =
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
            ).mapFailure { it.toWalletException() }.map { }
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
